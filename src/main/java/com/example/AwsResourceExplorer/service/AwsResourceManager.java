package com.example.AwsResourceExplorer.service;

import com.example.AwsResourceExplorer.model.AwsResource;
import com.example.AwsResourceExplorer.repository.AwsResourceRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.resourceexplorer2.model.Resource;


import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AwsResourceManager {

    private final AwsResourceExplorerService explorerService;
    private final AwsResourceSaveService saveService;
    private final AwsResourceRepository repository;

    public AwsResourceManager(AwsResourceExplorerService explorerService,
                              AwsResourceSaveService saveService,
                              AwsResourceRepository repository) {
        this.explorerService = explorerService;
        this.saveService = saveService;
        this.repository = repository;
    }

    @Transactional
    public List<AwsResource> updateResources(String query) {
        List<Resource> resources = explorerService.listAllResourcesFromActiveRegions(query);
        Set<String> newArns = resources.stream()
                .map(Resource::arn)
                .collect(Collectors.toSet());
        Set<String> existingArns = new HashSet<>(repository.findAllArns());
        Set<String> deletedArns = new HashSet<>(existingArns);
        deletedArns.removeAll(newArns);
        if (!deletedArns.isEmpty()) {
            repository.markInactiveByArns(deletedArns);
        }

        return saveService.saveResources(resources);
    }
}
