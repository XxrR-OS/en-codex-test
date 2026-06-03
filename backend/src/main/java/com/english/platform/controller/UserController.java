package com.english.platform.controller;

import com.english.platform.common.Result;
import com.english.platform.dto.LoginDTO;
import com.english.platform.dto.RegisterDTO;
import com.english.platform.entity.User;
import com.english.platform.service.UserService;
import com.english.platform.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户模块接口
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 用户登录
     * POST /api/user/login
     */
    @PostMapping("/login")
    public Result<UserVO> login(@RequestBody @Valid LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    /**
     * 用户注册
     * POST /api/user/register
     */
    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    /**
     * 获取当前用户信息（需登录）
     * GET /api/user/info
     */
    @GetMapping("/info")
    public Result<UserVO> getUserInfo() {
        return Result.success(userService.getCurrentUser());
    }

    /**
     * 更新用户资料（需登录）
     * PUT /api/user/profile
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestBody User user) {
        userService.updateProfile(user);
        return Result.success();
    }
}
