package com.example.mywebsite.service;

import com.example.mywebsite.entity.Bill;
import com.example.mywebsite.repository.BillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BillService {

    private final BillRepository billRepository;

    @Autowired
    public BillService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    public List<Bill> findByUserId(Long userId) {
        return billRepository.findByUserId(userId);
    }

    public Page<Bill> findByUserIdWithPagination(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "billingCycle", "billingDate"));
        return billRepository.findByUserId(userId, pageable);
    }

    public List<String> findDistinctKeyAliasesByUserId(Long userId) {
        return billRepository.findDistinctKeyAliasesByUserId(userId);
    }

    public List<String> findDistinctProductNamesByUserId(Long userId) {
        return billRepository.findDistinctProductNamesByUserId(userId);
    }

    public List<Bill> findByUserIdAndFilters(Long userId, String keyAlias, String billingCycle,
                                              String productName, String instanceId,
                                              String startDate, String endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "billingCycle", "billingDate"));
        Page<Bill> billPage = billRepository.findByUserId(userId, pageable);
        return billPage.getContent();
    }

    public long countByUserId(Long userId) {
        return billRepository.countByUserId(userId);
    }

    public List<Bill> findByUserIdAndIdIn(Long userId, List<Long> ids) {
        return billRepository.findByUserIdAndIdIn(userId, ids);
    }

    @Transactional
    public Bill save(Bill bill) {
        return billRepository.save(bill);
    }

    @Transactional
    public void deleteByIdAndUserId(Long id, Long userId) {
        billRepository.deleteByIdAndUserId(id, userId);
    }

    @Transactional
    public int saveBillsFromApi(Long userId, String keyAlias, String billingCycle,
                                 List<Map<String, Object>> billsData) {
        int count = 0;
        int updatedCount = 0;

        for (Map<String, Object> billData : billsData) {
            String instanceId = getStringValue(billData, "InstanceID", getStringValue(billData, "ResourceId", ""));
            String productCode = getStringValue(billData, "ProductCode", "");
            String billingDate = getStringValue(billData, "BillingDate", "");
            String productName = getStringValue(billData, "ProductName", "");

            double paymentAmount = calculatePaymentAmount(billData);
            if (paymentAmount == 0) {
                continue;
            }

            Optional<Bill> existingBill = billRepository.findExistingBill(userId, keyAlias, billingCycle, billingDate, instanceId, productCode);

            if (existingBill.isPresent()) {
                Bill bill = existingBill.get();
                updateBillFromData(bill, billData, billingDate, paymentAmount);
                billRepository.save(bill);
                updatedCount++;
            } else {
                Bill bill = createBillFromData(userId, keyAlias, billingCycle, billingDate, billData, paymentAmount);
                billRepository.save(bill);
                count++;
            }
        }

        return count;
    }

    private Optional<Bill> findExistingBill(Long userId, String keyAlias, String billingCycle,
                                             String billingDate, String instanceId, String productCode) {
        return billRepository.findExistingBill(userId, keyAlias, billingCycle, billingDate, instanceId, productCode);
    }

    private void updateBillFromData(Bill bill, Map<String, Object> data, String billingDate, double paymentAmount) {
        bill.setProductName(getStringValue(data, "ProductName", ""));
        bill.setInstanceName(getNickNameOrInstanceName(data));
        bill.setRegion(getStringValue(data, "Region", ""));
        bill.setZone(getStringValue(data, "Zone", ""));
        bill.setInstanceConfig(getInstanceConfig(data));
        bill.setResourceTags(getResourceTags(data));
        bill.setPretaxAmount(getDoubleValue(data, "PretaxAmount"));
        bill.setPaymentAmount(paymentAmount);
        bill.setCurrency(getStringValue(data, "Currency", "CNY"));
        bill.setRawData(data.toString());
    }

    private Bill createBillFromData(Long userId, String keyAlias, String billingCycle,
                                     String billingDate, Map<String, Object> data, double paymentAmount) {
        Bill bill = new Bill();
        bill.setUserId(userId);
        bill.setKeyAlias(keyAlias);
        bill.setBillingCycle(billingCycle);
        bill.setBillingDate(billingDate);
        bill.setProductCode(getStringValue(data, "ProductCode", ""));
        bill.setProductName(getStringValue(data, "ProductName", ""));
        bill.setInstanceId(getStringValue(data, "InstanceID", getStringValue(data, "ResourceId", "")));
        bill.setInstanceName(getNickNameOrInstanceName(data));
        bill.setRegion(getStringValue(data, "Region", ""));
        bill.setZone(getStringValue(data, "Zone", ""));
        bill.setInstanceConfig(getInstanceConfig(data));
        bill.setResourceTags(getResourceTags(data));
        bill.setPretaxAmount(getDoubleValue(data, "PretaxAmount"));
        bill.setPaymentAmount(paymentAmount);
        bill.setCurrency(getStringValue(data, "Currency", "CNY"));
        bill.setRawData(data.toString());
        return bill;
    }

    private double calculatePaymentAmount(Map<String, Object> data) {
        double paymentAmount = getDoubleValue(data, "PaymentAmount");
        if (paymentAmount > 0) {
            return paymentAmount;
        }

        double pretaxAmount = getDoubleValue(data, "PretaxAmount");
        if (pretaxAmount > 0) {
            return pretaxAmount;
        }

        double afterDiscountAmount = getDoubleValue(data, "AfterDiscountAmount");
        if (afterDiscountAmount > 0) {
            return afterDiscountAmount;
        }

        return 0.0;
    }

    private String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : "";
    }

    private String getStringValue(Map<String, Object> data, String key, String defaultValue) {
        String value = getStringValue(data, key);
        return value.isEmpty() ? defaultValue : value;
    }

    private Double getDoubleValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private String getNickNameOrInstanceName(Map<String, Object> data) {
        String nickName = getStringValue(data, "NickName", "");
        if (!nickName.isEmpty()) {
            return nickName;
        }
        String instanceName = getStringValue(data, "InstanceName", "");
        if (!instanceName.isEmpty()) {
            return instanceName;
        }
        return getStringValue(data, "Item", "");
    }

    private String getInstanceConfig(Map<String, Object> data) {
        String instanceSpec = getStringValue(data, "InstanceSpec", "");
        if (!instanceSpec.isEmpty()) {
            return instanceSpec;
        }
        String instanceConfig = getStringValue(data, "InstanceConfig", "");
        if (!instanceConfig.isEmpty()) {
            return instanceConfig;
        }
        return getStringValue(data, "SubscriptionType", "");
    }

    private String getResourceTags(Map<String, Object> data) {
        Object tag = data.get("Tag");
        if (tag == null) {
            return "";
        }
        if (tag instanceof String) {
            return (String) tag;
        }
        return tag.toString();
    }
}
