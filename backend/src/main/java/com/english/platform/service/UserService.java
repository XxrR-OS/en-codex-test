package com.english.platform.service;

import com.english.platform.dto.LoginDTO;
import com.english.platform.dto.RegisterDTO;
import com.english.platform.entity.User;
import com.english.platform.vo.UserVO;

public interface UserService {
    /** 用户登录，返回含 Token 的 VO */
    UserVO login(LoginDTO dto);
    /** 用户注册 */
    void register(RegisterDTO dto);
    /** 获取当前登录用户信息 */
    UserVO getCurrentUser();
    /** 更新用户资料 */
    void updateProfile(User user);
}
