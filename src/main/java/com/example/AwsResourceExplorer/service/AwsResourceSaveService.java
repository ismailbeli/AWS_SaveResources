package com.example.AwsResourceExplorer.service;

import com.example.AwsResourceExplorer.model.AwsResource;
import com.example.AwsResourceExplorer.repository.AwsResourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.services.resourceexplorer2.model.Resource;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AwsResourceSaveService {

    @Autowired
    private AwsResourceRepository repository;
    public List<AwsResource> saveResources(List<Resource> awsResources) {
        List<AwsResource> entities = awsResources.stream().map(r -> {
            AwsResource res = new AwsResource();
            res.setIdentifier(r.arn());       // burası önemli
            res.setResourceType(r.resourceType());
            res.setService(r.service());
            res.setRegion(r.region());
            res.setOwnerAccount(r.owningAccountId());
            res.setTags(String.valueOf(Collections.emptyMap()));  // boş bir map atıyoruz, null değil
            res.setApplication(String.valueOf(Collections.emptyMap())); // ya da başka logic
            res.setStatus(AwsResource.Status.ACTIVE); // yeni eklenen kaynaklar aktif olarak işaretleniyor
            res.setUpdateDate(LocalDateTime.now());
            return res;
        }).collect(Collectors.toList());

       return repository.saveAll(entities);
    }
    /* identifierName'ı ARN'den çıkartmak için kullanılabilir
    private String extractIdentifierName(String arn) {
        if (arn == null || arn.isEmpty()) return "";

        String[] parts = arn.split(":");
        if (parts.length < 6) return arn;

        String resourcePart = parts[5];

        if (resourcePart.contains("/")) {
            String[] resourceParts = resourcePart.split("/");
            return resourceParts[resourceParts.length - 1];
        } else if (resourcePart.contains(":")) {
            String[] resourceParts = resourcePart.split(":");
            return resourceParts[resourceParts.length - 1];
        } else {
            return resourcePart;
        }
    }
    */
}
