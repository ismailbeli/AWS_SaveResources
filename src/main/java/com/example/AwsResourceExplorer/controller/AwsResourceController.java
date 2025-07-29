package com.example.AwsResourceExplorer.controller;

import com.example.AwsResourceExplorer.model.AwsResource;
import com.example.AwsResourceExplorer.service.AwsResourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/aws-resources")
public class AwsResourceController {

    private final AwsResourceManager resourceManager;

    @Autowired
    public AwsResourceController(AwsResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    // Hem GET hem POST destekler (örneğin tarayıcıdan çağırmak için)
    @RequestMapping(value = "/sync", method = {RequestMethod.GET, RequestMethod.POST})
    public List<AwsResource> syncResources(@RequestParam(defaultValue = "*") String query) {
        try {
            return resourceManager.updateResources(query);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); // Hata durumunda boş liste döner
        }
    }
}
