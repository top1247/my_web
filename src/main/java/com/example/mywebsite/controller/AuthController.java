package com.example.mywebsite.controller;

import com.example.mywebsite.entity.User;
import com.example.mywebsite.repository.UserRepository;
import com.example.mywebsite.service.OperationLogService;
import com.example.mywebsite.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@Controller
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final OperationLogService operationLogService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthController(UserService userService, UserRepository userRepository,
                          OperationLogService operationLogService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.operationLogService = operationLogService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        HttpSession session,
                        Model model) {
        if (SecurityContextHolder.getContext().getAuthentication() != null &&
            SecurityContextHolder.getContext().getAuthentication().isAuthenticated() &&
            !"anonymousUser".equals(SecurityContextHolder.getContext().getAuthentication().getPrincipal())) {
            return "redirect:/";
        }

        if (error != null) {
            model.addAttribute("error", "用户名或密码错误！");
        }
        if (logout != null) {
            model.addAttribute("message", "已退出登录");
        }
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String registerPost(@RequestParam String username,
                               @RequestParam String password,
                               @RequestParam String confirmPassword,
                               HttpServletRequest request,
                               Model model) {
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            model.addAttribute("error", "用户名和密码不能为空！");
            return "register";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "两次输入的密码不一致！");
            return "register";
        }

        if (password.length() < 6) {
            model.addAttribute("error", "密码长度至少6位！");
            return "register";
        }

        if (userService.existsByUsername(username)) {
            model.addAttribute("error", "用户名已存在！");
            return "register";
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setUserType("normal");
        user.setStatus("active");
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);

        operationLogService.logOperation(null, username, "注册", "用户 " + username + " 注册成功", "success", getClientIp(request));
        model.addAttribute("message", "注册成功！请登录");
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : null;

        if (username != null && !"anonymousUser".equals(username)) {
            userRepository.findByUsername(username).ifPresent(user ->
                operationLogService.logOperation(user.getId(), username, "登出", "退出登录")
            );
        }

        session.invalidate();
        SecurityContextHolder.clearContext();
        return "redirect:/login";
    }

    @GetMapping("/login-success")
    public String loginSuccess(HttpSession session, HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        return userRepository.findByUsername(username)
            .map(user -> {
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);

                session.setAttribute("user_id", user.getId());
                session.setAttribute("username", user.getUsername());
                session.setAttribute("user_type", user.getUserType());

                operationLogService.logOperation(user.getId(), username, "登录", "登录成功", "success", getClientIp(request));
                return "redirect:/";
            })
            .orElse("redirect:/login");
    }

    @GetMapping("/login-failure")
    public String loginFailure(Model model) {
        model.addAttribute("error", "用户名或密码错误！");
        return "login";
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
