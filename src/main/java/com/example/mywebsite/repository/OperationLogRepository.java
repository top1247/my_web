package com.example.mywebsite.repository;

import com.example.mywebsite.entity.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OperationLogRepository extends JpaRepository<OperationLog, Long> {

    List<OperationLog> findByUserId(Long userId);

    @Query("SELECT DISTINCT o.operationType FROM OperationLog o ORDER BY o.operationType")
    List<String> findDistinctOperationTypes();

    @Query("SELECT o FROM OperationLog o ORDER BY o.createdAt DESC")
    List<OperationLog> findAllOrdered();

    @Query("SELECT o FROM OperationLog o WHERE " +
           "(:operationType IS NULL OR :operationType = '' OR o.operationType = :operationType) AND " +
           "(:keyword IS NULL OR :keyword = '' OR o.username LIKE %:keyword% OR o.operationDesc LIKE %:keyword%) " +
           "ORDER BY o.createdAt DESC")
    List<OperationLog> findWithFilters(
            @Param("operationType") String operationType,
            @Param("keyword") String keyword);

    long count();
}
