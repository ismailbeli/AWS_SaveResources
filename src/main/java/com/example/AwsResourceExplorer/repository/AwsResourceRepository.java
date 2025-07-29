package com.example.AwsResourceExplorer.repository;

import com.example.AwsResourceExplorer.model.AwsResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AwsResourceRepository extends JpaRepository<AwsResource, String> {
    @Query("SELECT r.identifier FROM AwsResource r ")
    List<String> findAllArns();

    @Modifying
    @Transactional
    @Query("UPDATE AwsResource r SET r.status = 'INACTIVE' WHERE r.identifier IN :arns")
    void markInactiveByArns(@Param("arns") Collection<String> arns);

}
