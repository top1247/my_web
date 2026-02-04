package com.example.mywebsite.controller;

import com.example.mywebsite.dto.ApiResponse;
import com.example.mywebsite.entity.AliyunKey;
import com.example.mywebsite.entity.Bill;
import com.example.mywebsite.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
public class MainController {

    private final UserService userService;
    private final AliyunKeyService aliyunKeyService;
    private final BillService billService;
    private final AliyunApiService aliyunApiService;
    private final OperationLogService operationLogService;

    @Autowired
    public MainController(UserService userService, AliyunKeyService aliyunKeyService,
                          BillService billService, AliyunApiService aliyunApiService,
                          OperationLogService operationLogService) {
        this.userService = userService;
        this.aliyunKeyService = aliyunKeyService;
        this.billService = billService;
        this.aliyunApiService = aliyunApiService;
        this.operationLogService = operationLogService;
    }

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }
        model.addAttribute("username", username);
        model.addAttribute("user_type", session.getAttribute("user_type"));
        return "home";
    }

    @GetMapping("/aliyun/keys")
    public String aliyunKeys(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }

        Long userId = (Long) session.getAttribute("user_id");
        List<AliyunKey> keys = aliyunKeyService.findByUserId(userId);

        model.addAttribute("username", username);
        model.addAttribute("user_type", session.getAttribute("user_type"));
        model.addAttribute("keys", keys);
        return "aliyun_keys";
    }

    @PostMapping("/aliyun/keys/add")
    public String addAliyunKey(@RequestParam String alias,
                                @RequestParam String accessKeyId,
                                @RequestParam String accessKeySecret,
                                HttpSession session,
                                HttpServletRequest request,
                                Model model) {
        String username = (String) session.getAttribute("username");
        Long userId = (Long) session.getAttribute("user_id");

        if (aliyunKeyService.existsByAliasAndUserId(alias, userId)) {
            model.addAttribute("error", "该别名已存在！");
            return "redirect:/aliyun/keys";
        }

        if (!aliyunApiService.testConnection(accessKeyId, accessKeySecret)) {
            operationLogService.logOperation(userId, username, "添加密钥", "添加密钥失败：" + alias, "failed", getClientIp(request));
            model.addAttribute("error", "密钥无效，请检查后重试！");
            return "redirect:/aliyun/keys";
        }

        aliyunKeyService.save(userId, alias, accessKeyId, accessKeySecret);
        operationLogService.logOperation(userId, username, "添加密钥", "添加密钥成功：" + alias);
        model.addAttribute("message", "密钥添加成功！");
        return "redirect:/aliyun/keys";
    }

    @PostMapping("/aliyun/keys/delete/{keyId}")
    public String deleteAliyunKey(@PathVariable Long keyId,
                                   HttpSession session,
                                   HttpServletRequest request,
                                   Model model) {
        String username = (String) session.getAttribute("username");
        Long userId = (Long) session.getAttribute("user_id");

        aliyunKeyService.findByIdAndUserId(keyId, userId).ifPresent(key -> {
            aliyunKeyService.deleteByIdAndUserId(keyId, userId);
            operationLogService.logOperation(userId, username, "删除密钥", "删除密钥：" + key.getAlias());
            model.addAttribute("message", "密钥已删除！");
        });

        return "redirect:/aliyun/keys";
    }

    @GetMapping("/aliyun/bills")
    public String aliyunBills(HttpSession session,
                               @RequestParam(value = "page", defaultValue = "1") int page,
                               @RequestParam(value = "keyAlias", required = false) String keyAlias,
                               @RequestParam(value = "billingCycle", required = false) String billingCycle,
                               @RequestParam(value = "productName", required = false) String productName,
                               @RequestParam(value = "instanceId", required = false) String instanceId,
                               @RequestParam(value = "dateRange", required = false) String dateRange,
                               Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }

        Long userId = (Long) session.getAttribute("user_id");
        int pageSize = 50;

        List<Bill> bills = billService.findByUserIdWithPagination(userId, page, pageSize).getContent();
        long total = billService.countByUserId(userId);
        int totalPages = (int) Math.ceil((double) total / pageSize);

        // 从 AliyunKey 表获取保存的密钥列表
        List<AliyunKey> userKeys = aliyunKeyService.findByUserId(userId);
        List<String> keyAliases = userKeys.stream().map(AliyunKey::getAlias).toList();

        // 从账单表获取产品名称列表
        List<String> productNames = billService.findDistinctProductNamesByUserId(userId);

        model.addAttribute("username", username);
        model.addAttribute("user_type", session.getAttribute("user_type"));
        model.addAttribute("bills", bills);
        model.addAttribute("keyAliases", keyAliases);
        model.addAttribute("productNames", productNames);
        model.addAttribute("page", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("total", total);
        return "aliyun_bills";
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/aliyun/bills/fetch")
    @ResponseBody
    public ApiResponse<?> fetchBills(@RequestParam String keyAlias,
                                      @RequestParam String dateMode,
                                      @RequestParam String billingCycle,
                                      @RequestParam(required = false) String billingDate,
                                      HttpSession session,
                                      HttpServletRequest request) {
        String username = (String) session.getAttribute("username");
        Long userId = (Long) session.getAttribute("user_id");

        if (keyAlias == null || billingCycle == null || billingCycle.isEmpty()) {
            return ApiResponse.error("参数不完整");
        }

        return aliyunKeyService.findByAliasAndUserId(keyAlias, userId)
            .<ApiResponse<?>>map(key -> {
                String accessKeyId = key.getAccessKeyId();
                String accessKeySecret = aliyunKeyService.decryptSecret(key);

                try {
                    List<Map<String, Object>> billsData;
                    if ("daily".equals(dateMode) && billingDate != null && !billingDate.isEmpty()) {
                        billsData = aliyunApiService.getDailyBill(accessKeyId, accessKeySecret, billingCycle, billingDate, true);
                    } else {
                        billsData = aliyunApiService.getMonthlyBill(accessKeyId, accessKeySecret, billingCycle, true);
                    }

                    int count = billService.saveBillsFromApi(userId, keyAlias, billingCycle, billsData);

                    operationLogService.logOperation(userId, username, "获取账单",
                        "获取账单成功：" + keyAlias + "，" + billingCycle + "，新增" + count + "条");

                    String message = "成功获取并保存 " + count + " 条账单记录";
                    return ApiResponse.success(message, count);
                } catch (Exception e) {
                    operationLogService.logOperation(userId, username, "获取账单",
                        "获取账单失败：" + keyAlias + "，" + e.getMessage(), "failed", getClientIp(request));
                    return ApiResponse.error("获取账单失败: " + e.getMessage());
                }
            })
            .orElse(ApiResponse.error("密钥不存在"));
    }

    @GetMapping("/aliyun/bills/export")
    public ResponseEntity<byte[]> exportBills(@RequestParam(value = "keyAlias", required = false) String keyAlias,
                                               @RequestParam(value = "billingCycle", required = false) String billingCycle,
                                               HttpSession session) {
        String username = (String) session.getAttribute("username");
        Long userId = (Long) session.getAttribute("user_id");

        List<Bill> bills = billService.findByUserId(userId);

        if (keyAlias != null && !keyAlias.isEmpty()) {
            bills.removeIf(bill -> !keyAlias.equals(bill.getKeyAlias()));
        }
        if (billingCycle != null && !billingCycle.isEmpty()) {
            bills.removeIf(bill -> !billingCycle.equals(bill.getBillingCycle()));
        }

        if (bills.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("账单明细");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"账号别名", "账期", "账单日期", "产品名称", "实例ID", "消费金额", "币种", "创建时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (Bill bill : bills) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(bill.getKeyAlias());
                row.createCell(1).setCellValue(bill.getBillingCycle());
                row.createCell(2).setCellValue(bill.getBillingDate() != null ? bill.getBillingDate() : "");
                row.createCell(3).setCellValue(bill.getProductName() != null ? bill.getProductName() : "");
                row.createCell(4).setCellValue(bill.getInstanceId() != null ? bill.getInstanceId() : "");
                Cell amountCell = row.createCell(5);
                amountCell.setCellValue(bill.getPaymentAmount() != null ? bill.getPaymentAmount() : 0.0);
                row.createCell(6).setCellValue(bill.getCurrency() != null ? bill.getCurrency() : "");
                row.createCell(7).setCellValue(bill.getCreatedAt() != null ? bill.getCreatedAt().toString() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            operationLogService.logOperation(userId, username, "导出账单", "导出账单成功：共" + bills.size() + "条记录");

            String filename = "aliyun_bills_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(outputStream.toByteArray());
        } catch (Exception e) {
            operationLogService.logOperation(userId, username, "导出账单", "导出账单失败：" + e.getMessage(), "failed");
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/aliyun/bills/delete")
    @ResponseBody
    public ApiResponse<Integer> deleteBills(@RequestParam String billIds, HttpSession session, HttpServletRequest request) {
        String username = (String) session.getAttribute("username");
        Long userId = (Long) session.getAttribute("user_id");

        if (billIds == null || billIds.trim().isEmpty()) {
            return ApiResponse.error("请选择要删除的账单");
        }

        try {
            String[] idArray = billIds.split(",");
            List<Long> ids = new ArrayList<>();
            for (String id : idArray) {
                ids.add(Long.parseLong(id.trim()));
            }

            int deletedCount = 0;
            for (Long id : ids) {
                billService.deleteByIdAndUserId(id, userId);
                deletedCount++;
            }

            operationLogService.logOperation(userId, username, "删除账单", "批量删除账单：" + deletedCount + "条");
            return ApiResponse.success("成功删除 " + deletedCount + " 条账单", deletedCount);
        } catch (Exception e) {
            return ApiResponse.error("删除失败: " + e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
