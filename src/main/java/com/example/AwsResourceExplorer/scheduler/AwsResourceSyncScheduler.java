package com.example.AwsResourceExplorer.scheduler;

import com.example.AwsResourceExplorer.model.AwsResource;
import com.example.AwsResourceExplorer.service.AwsResourceExplorerService;
import com.example.AwsResourceExplorer.service.AwsResourceManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AwsResourceSyncScheduler {
    private final AwsResourceManager awsResourceManager;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AwsResourceExplorerService.class);

    public AwsResourceSyncScheduler(AwsResourceManager awsResourceManager) {
        this.awsResourceManager = awsResourceManager;
    }

    @Scheduled(cron = "${aws.sync.cron}")
    public void runScheduledSync(){
        log.info("Starting scheduled AWS resource sync...");
        try {
            List<AwsResource> updated = awsResourceManager.updateResources("*");
            log.info("AWS resource sync completed successfully.");
        } catch (Exception e) {
            log.error("Error during AWS resource sync: {}", e.getMessage(), e);
        }

    }

}
