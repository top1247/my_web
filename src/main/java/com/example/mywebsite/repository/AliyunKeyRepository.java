package com.example.mywebsite.repository;

import com.example.mywebsite.entity.AliyunKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AliyunKeyRepository extends JpaRepository<AliyunKey, Long> {

    List<AliyunKey> findByUserId(Long userId);

    Optional<AliyunKey> findByIdAndUserId(Long id, Long userId);

    Optional<AliyunKey> findByAliasAndUserId(String alias, Long userId);

    List<AliyunKey> findAllByUserId(Long userId);

    void deleteByIdAndUserId(Long id, Long userId);
}
