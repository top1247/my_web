package com.example.mywebsite.service;

import com.example.mywebsite.entity.AliyunKey;
import com.example.mywebsite.repository.AliyunKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AliyunKeyService {

    private final AliyunKeyRepository aliyunKeyRepository;
    private final EncryptionService encryptionService;

    @Autowired
    public AliyunKeyService(AliyunKeyRepository aliyunKeyRepository, EncryptionService encryptionService) {
        this.aliyunKeyRepository = aliyunKeyRepository;
        this.encryptionService = encryptionService;
    }

    public List<AliyunKey> findByUserId(Long userId) {
        return aliyunKeyRepository.findByUserId(userId);
    }

    public Optional<AliyunKey> findByIdAndUserId(Long id, Long userId) {
        return aliyunKeyRepository.findByIdAndUserId(id, userId);
    }

    public Optional<AliyunKey> findByAliasAndUserId(String alias, Long userId) {
        return aliyunKeyRepository.findByAliasAndUserId(alias, userId);
    }

    @Transactional
    public AliyunKey save(Long userId, String alias, String accessKeyId, String accessKeySecret) {
        AliyunKey key = new AliyunKey();
        key.setUserId(userId);
        key.setAlias(alias);
        key.setAccessKeyId(accessKeyId);
        key.setAccessKeySecret(encryptionService.encrypt(accessKeySecret));
        return aliyunKeyRepository.save(key);
    }

    @Transactional
    public void deleteByIdAndUserId(Long id, Long userId) {
        aliyunKeyRepository.deleteByIdAndUserId(id, userId);
    }

    public String decryptSecret(AliyunKey key) {
        return encryptionService.decrypt(key.getAccessKeySecret());
    }

    public boolean existsByAliasAndUserId(String alias, Long userId) {
        return findByAliasAndUserId(alias, userId).isPresent();
    }
}
