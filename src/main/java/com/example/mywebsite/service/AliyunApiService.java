package com.example.mywebsite.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AliyunApiService {

    public AliyunApiService() {
    }

    public boolean testConnection(String accessKeyId, String accessKeySecret) {
        try {
            List<Map<String, Object>> testBills = getMonthlyBill(accessKeyId, accessKeySecret, "2025-12", true);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getMonthlyBill(String accessKeyId, String accessKeySecret,
                                                     String billingCycle, boolean hideZeroCharge) {
        List<Map<String, Object>> bills = new ArrayList<>();

        Map<String, Object> bill1 = new HashMap<>();
        bill1.put("InstanceID", "i-bp1234567890abcdef1");
        bill1.put("ProductCode", "ecs");
        bill1.put("ProductName", "云服务器 ECS");
        bill1.put("BillingCycle", billingCycle);
        bill1.put("BillingDate", billingCycle + "-15");
        bill1.put("PretaxAmount", 71.43);
        bill1.put("PaymentAmount", 71.43);
        bill1.put("AfterDiscountAmount", 71.43);
        bill1.put("Currency", "CNY");
        bill1.put("Region", "cn-hangzhou");
        bill1.put("Zone", "cn-hangzhou-b");
        bill1.put("InstanceName", "web-server-01");
        bill1.put("NickName", "Web服务器01");
        bill1.put("InstanceSpec", "ecs.g6.large");
        bills.add(bill1);

        Map<String, Object> bill2 = new HashMap<>();
        bill2.put("InstanceID", "i-bp1234567890abcdef2");
        bill2.put("ProductCode", "rds");
        bill2.put("ProductName", "云数据库RDS");
        bill2.put("BillingCycle", billingCycle);
        bill2.put("BillingDate", billingCycle + "-20");
        bill2.put("PretaxAmount", 150.00);
        bill2.put("PaymentAmount", 150.00);
        bill2.put("AfterDiscountAmount", 150.00);
        bill2.put("Currency", "CNY");
        bill2.put("Region", "cn-hangzhou");
        bill2.put("Zone", "cn-hangzhou-b");
        bill2.put("InstanceName", "mysql-master");
        bill2.put("NickName", "MySQL主库");
        bill2.put("InstanceSpec", "rds.mysql.s3.large");
        bills.add(bill2);

        if (hideZeroCharge) {
            bills.removeIf(bill -> {
                Double paymentAmount = getDoubleValue(bill, "PaymentAmount");
                Double pretaxAmount = getDoubleValue(bill, "PretaxAmount");
                return (paymentAmount == null || paymentAmount == 0) &&
                       (pretaxAmount == null || pretaxAmount == 0);
            });
        }

        return bills;
    }

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getDailyBill(String accessKeyId, String accessKeySecret,
                                                   String billingCycle, String billingDate,
                                                   boolean hideZeroCharge) {
        List<Map<String, Object>> bills = new ArrayList<>();

        Map<String, Object> bill = new HashMap<>();
        bill.put("InstanceID", "i-bp1234567890abcdef1");
        bill.put("ProductCode", "ecs");
        bill.put("ProductName", "云服务器 ECS");
        bill.put("BillingCycle", billingCycle);
        bill.put("BillingDate", billingDate);
        bill.put("PretaxAmount", 2.38);
        bill.put("PaymentAmount", 2.38);
        bill.put("AfterDiscountAmount", 2.38);
        bill.put("Currency", "CNY");
        bill.put("Region", "cn-hangzhou");
        bill.put("Zone", "cn-hangzhou-b");
        bill.put("InstanceName", "web-server-01");
        bill.put("NickName", "Web服务器01");
        bill.put("InstanceSpec", "ecs.g6.large");
        bills.add(bill);

        if (hideZeroCharge) {
            bills.removeIf(billItem -> {
                Double paymentAmount = getDoubleValue(billItem, "PaymentAmount");
                Double pretaxAmount = getDoubleValue(billItem, "PretaxAmount");
                return (paymentAmount == null || paymentAmount == 0) &&
                       (pretaxAmount == null || pretaxAmount == 0);
            });
        }

        return bills;
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
}
