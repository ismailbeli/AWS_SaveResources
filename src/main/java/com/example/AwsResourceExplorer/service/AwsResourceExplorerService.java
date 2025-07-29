package com.example.AwsResourceExplorer.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.costexplorer.AWSCostExplorer;
import com.amazonaws.services.costexplorer.AWSCostExplorerClientBuilder;
import com.amazonaws.services.costexplorer.model.*;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.resourceexplorer2.ResourceExplorer2Client;
import software.amazon.awssdk.services.resourceexplorer2.model.*;
import software.amazon.awssdk.services.resourceexplorer2.paginators.SearchIterable;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class AwsResourceExplorerService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AwsResourceExplorerService.class);

    @Value("${aws.accessKeyId}")
    private String accessKey;

    @Value("${aws.secretKey}")
    private String secretKey;

    @Value("${aws.region}")
    private String defaultRegion;

    private AwsBasicCredentials credentials;  // v2 Resource Explorer için
    private BasicAWSCredentials legacyCredentials; // v1 Cost Explorer için

    @PostConstruct
    public void init() {
        credentials = AwsBasicCredentials.create(accessKey, secretKey);
        legacyCredentials = new BasicAWSCredentials(accessKey, secretKey);
    }

    public List<Resource> listAllResourcesFromActiveRegions(String baseQuery) {
        List<String> regionsWithCost = getRegionsFromCostExplorer();
        log.info("Number of regions used based on Cost Explorer: {}", regionsWithCost.size());
        if (regionsWithCost.isEmpty()) {
            log.warn("No active regions detected via Cost Explorer. You may want to scan all regions or use a predefined list.");
            return Collections.emptyList();
        }

        List<Resource> allResources = Collections.synchronizedList(new ArrayList()); // method ile liste operasyonları synchronize hale geldi. Aynı zamanda bir thread add yapıyorsa diğerleri get veya remove yaparken bekleyecek.
        Map<String, Integer> regionResourceCount = new ConcurrentHashMap<>();

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(regionsWithCost.size(), 3));


        // Aggregator'ın olduğu default region'dan client oluşturuyoruz
        try (ResourceExplorer2Client client = ResourceExplorer2Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(defaultRegion)) // aggregator index bölgesi
                .build()) {

            List<Future<?>> futures = new ArrayList<>(); // işlemlerin tamamlanma bilgisini tutar ve hata takibi yapar. //Region sayısı kadar task tutacak.


            for (String regionName : regionsWithCost) {
                futures.add(executor.submit(() -> {
                    try {
                        String queryWithRegion = baseQuery + " region:\"" + regionName + "\"";
                        SearchRequest request = SearchRequest.builder()
                                .queryString(queryWithRegion)
                                .build();

                        SearchIterable response = client.searchPaginator(request);

                        int count = 0;
                        for (SearchResponse page : response) {
                            allResources.addAll(page.resources());
                            count += page.resources().size();
                        }

                        regionResourceCount.put(regionName, count);
                        log.info("Found {} resources in region {}", regionName, count);
                    } catch (Exception e) {
                        log.error("Error while fetching resources for region {}: {}", regionName, e.getMessage());
                    }

            }));
            }

            for (Future<?> future : futures) {
                try {
                    future.get(); // Tüm işlemlerin tamamlanmasını bekler. Null dönüş yapar.
                } catch (Exception e) {
                    log.error("Error while waiting for futures: {}", e.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Error occurred while querying resources using aggregator region: {}", e.getMessage());
        } finally {
            executor.shutdown();
        }

        regionResourceCount.forEach((region, count) -> log.info("Region {} has a total of {} resources", region, count));
        int totalCount = regionResourceCount.values().stream().mapToInt(Integer::intValue).sum();
        log.info("Total number of resources across all regions: {}", totalCount);
        return allResources;
    }



    private List<String> getRegionsFromCostExplorer() {
        Set<String> activeRegions = new HashSet<>(); //tekarı önlemek için kullandım.

        try {
            AWSCostExplorer client = AWSCostExplorerClientBuilder.standard()
                    .withRegion(defaultRegion)  // Cost Explorer sadece bazı bölgelerde çalışır
                    .withCredentials(new AWSStaticCredentialsProvider(legacyCredentials))
                    .build();

            LocalDate end = LocalDate.now();
            LocalDate start = end.minusDays(30);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            GetCostAndUsageRequest request = new GetCostAndUsageRequest()
                    .withTimePeriod(new DateInterval()
                            .withStart(start.format(formatter))
                            .withEnd(end.format(formatter)))
                    .withGranularity("MONTHLY")
                    .withMetrics("UnblendedCost")
                    .withGroupBy(new GroupDefinition()
                            .withType("DIMENSION")
                            .withKey("REGION"));

            GetCostAndUsageResult result = client.getCostAndUsage(request);

            for (ResultByTime timeResult : result.getResultsByTime()) {
                for (Group group : timeResult.getGroups()) {
                    String region = group.getKeys().get(0);
                    String amount = group.getMetrics().get("UnblendedCost").getAmount();
                    if (Double.parseDouble(amount) > 0) {
                        activeRegions.add(region);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Failed to retrieve region data from Cost Explorer: {}", e.getMessage());
        }
        log.info("Active regions identified from Cost Explorer: {}", activeRegions);
        return new ArrayList<>(activeRegions);

    }
}
