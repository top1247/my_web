package com.example.mywebsite.controller;

import com.example.mywebsite.entity.OperationLog;
import com.example.mywebsite.entity.User;
import com.example.mywebsite.service.OperationLogService;
import com.example.mywebsite.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AdminController {

    private final UserService userService;
    private final OperationLogService operationLogService;

    @Autowired
    public AdminController(UserService userService, OperationLogService operationLogService) {
        this.userService = userService;
        this.operationLogService = operationLogService;
    }

    @GetMapping("/admin/users")
    public String adminUsers(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        Long userId = (Long) session.getAttribute("user_id");
        String userType = (String) session.getAttribute("user_type");

        if (username == null || !"admin".equals(userType)) {
            return "redirect:/";
        }

        List<User> users = userService.findAll();

        model.addAttribute("username", username);
        model.addAttribute("user_type", userType);
        model.addAttribute("users", users);
        model.addAttribute("currentUserId", userId);
        return "admin_users";
    }

    @PostMapping("/admin/users/toggle_status/{userId}")
    public String toggleUserStatus(@PathVariable Long userId,
                                    HttpSession session,
                                    Model model) {
        String username = (String) session.getAttribute("username");
        Long currentUserId = (Long) session.getAttribute("user_id");
        String userType = (String) session.getAttribute("user_type");

        if (!"admin".equals(userType)) {
            return "redirect:/";
        }

        if (userId.equals(currentUserId)) {
            model.addAttribute("error", "不能修改自己的状态！");
            return "redirect:/admin/users";
        }

        userService.findById(userId).ifPresent(user -> {
            userService.toggleStatus(userId);
            String newStatus = "active".equals(user.getStatus()) ? "disabled" : "active";
            operationLogService.logOperation(currentUserId, username, "修改用户状态",
                "将用户 " + user.getUsername() + " 状态改为 " + newStatus);
            model.addAttribute("message", "用户状态已更新为：" + newStatus);
        });

        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/reset_password/{userId}")
    public String resetPassword(@PathVariable Long userId,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 Model model) {
        String username = (String) session.getAttribute("username");
        String userType = (String) session.getAttribute("user_type");

        if (!"admin".equals(userType)) {
            return "redirect:/";
        }

        if (newPassword == null || newPassword.isEmpty() || confirmPassword == null || confirmPassword.isEmpty()) {
            model.addAttribute("error", "密码不能为空！");
            return "redirect:/admin/users";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("error", "两次输入的密码不一致！");
            return "redirect:/admin/users";
        }

        if (newPassword.length() < 6) {
            model.addAttribute("error", "密码长度至少6位！");
            return "redirect:/admin/users";
        }

        userService.findById(userId).ifPresent(user -> {
            userService.resetPassword(userId, newPassword);
            operationLogService.logOperation(null, username, "重置密码",
                "重置用户 " + user.getUsername() + " 的密码");
            model.addAttribute("message", "密码重置成功！");
        });

        return "redirect:/admin/users";
    }

    @GetMapping("/admin/logs")
    public String adminLogs(HttpSession session,
                            @RequestParam(value = "operationType", required = false) String operationType,
                            @RequestParam(value = "startDate", required = false) String startDate,
                            @RequestParam(value = "endDate", required = false) String endDate,
                            @RequestParam(value = "keyword", required = false) String keyword,
                            Model model) {
        String username = (String) session.getAttribute("username");
        String userType = (String) session.getAttribute("user_type");

        if (username == null || !"admin".equals(userType)) {
            return "redirect:/";
        }

        List<OperationLog> logs = operationLogService.findWithFilters(operationType, startDate, endDate, keyword);
        List<String> operationTypes = operationLogService.findDistinctOperationTypes();

        if (logs.size() > 500) {
            logs = logs.subList(0, 500);
        }

        model.addAttribute("username", username);
        model.addAttribute("user_type", userType);
        model.addAttribute("logs", logs);
        model.addAttribute("operationTypes", operationTypes);
        return "admin_logs";
    }
}
