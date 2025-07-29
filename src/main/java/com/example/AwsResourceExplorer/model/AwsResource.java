package com.example.AwsResourceExplorer.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Generated;

import java.time.LocalDateTime;

@Entity
@Table(name = "aws_resource")
public class AwsResource {

    @Id
    private String identifier;  // ARN veya başka unique identifier

    private String service;
    private String resourceType;
    private String region;
    private String ownerAccount;
    private String application;
    private String tags;  // JSON string olabilir
    @Enumerated(EnumType.STRING)
    private Status status;
    public enum Status {
        ACTIVE,
        INACTIVE
    }
    private LocalDateTime updateDate;

    public LocalDateTime getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(LocalDateTime updateDate) {
        this.updateDate = updateDate;
    }
// Getter-Setter

    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getService() {
        return service;
    }
    public void setService(String service) {
        this.service = service;
    }

    public String getResourceType() {
        return resourceType;
    }
    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getRegion() {
        return region;
    }
    public void setRegion(String region) {
        this.region = region;
    }

    public String getOwnerAccount() {
        return ownerAccount;
    }
    public void setOwnerAccount(String ownerAccount) {
        this.ownerAccount = ownerAccount;
    }

    public String getApplication() {
        return application;
    }
    public void setApplication(String application) {
        this.application = application;
    }

    public String getTags() {
        return tags;
    }
    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }


}
