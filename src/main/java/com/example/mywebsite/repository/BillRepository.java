package com.example.mywebsite.repository;

import com.example.mywebsite.entity.Bill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findByUserId(Long userId);

    Page<Bill> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT DISTINCT b.keyAlias FROM Bill b WHERE b.userId = :userId")
    List<String> findDistinctKeyAliasesByUserId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT b.productName FROM Bill b WHERE b.userId = :userId AND b.productName IS NOT NULL AND b.productName != ''")
    List<String> findDistinctProductNamesByUserId(@Param("userId") Long userId);

    Optional<Bill> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT b FROM Bill b WHERE b.userId = :userId AND b.id IN :ids")
    List<Bill> findByUserIdAndIdIn(@Param("userId") Long userId, @Param("ids") List<Long> ids);

    long countByUserId(Long userId);

    void deleteByIdAndUserId(Long id, Long userId);

    @Query("SELECT b FROM Bill b WHERE b.userId = :userId AND b.keyAlias = :keyAlias AND b.billingCycle = :billingCycle AND b.billingDate = :billingDate AND b.instanceId = :instanceId AND b.productCode = :productCode")
    Optional<Bill> findExistingBill(@Param("userId") Long userId,
                                     @Param("keyAlias") String keyAlias,
                                     @Param("billingCycle") String billingCycle,
                                     @Param("billingDate") String billingDate,
                                     @Param("instanceId") String instanceId,
                                     @Param("productCode") String productCode);
}
