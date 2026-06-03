package com.english.platform.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.english.platform.dto.LoginDTO;
import com.english.platform.dto.RegisterDTO;
import com.english.platform.entity.User;
import com.english.platform.exception.BusinessException;
import com.english.platform.mapper.UserMapper;
import com.english.platform.service.UserService;
import com.english.platform.util.JwtUtil;
import com.english.platform.util.UserContext;
import com.english.platform.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserVO login(LoginDTO dto) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, dto.getUsername())
                        .eq(User::getStatus, 1)
        );
        if (user == null) throw new BusinessException("用户不存在或已被禁用");
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        vo.setToken(token);
        return vo;
    }

    @Override
    public void register(RegisterDTO dto) {
        dto.setUsername(dto.getUsername() == null ? null : dto.getUsername().trim());
        dto.setNickname(dto.getNickname() == null ? null : dto.getNickname().trim());
        dto.setEmail(dto.getEmail() == null ? null : dto.getEmail().trim());
        if (dto.getEmail() != null && dto.getEmail().isBlank()) {
            dto.setEmail(null);
        }

        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (count > 0) throw new BusinessException("用户名已存在");
        if (dto.getEmail() != null) {
            Long emailCount = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getEmail, dto.getEmail()));
            if (emailCount > 0) throw new BusinessException("邮箱已被注册");
        }

        User user = new User();
        BeanUtils.copyProperties(dto, user);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setStatus(1);
        user.setTotalScore(0);
        if (user.getLevel() == null) user.setLevel(1);
        if (user.getNickname() == null || user.getNickname().isBlank()) {
            user.setNickname(dto.getUsername());
        }
        userMapper.insert(user);
    }

    @Override
    public UserVO getCurrentUser() {
        Long userId = UserContext.getUserId();
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(401, "用户不存在");
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }

    @Override
    public void updateProfile(User update) {
        Long userId = UserContext.getUserId();
        User user = new User();
        user.setId(userId);
        if (update.getNickname() != null) user.setNickname(update.getNickname());
        if (update.getEmail() != null) user.setEmail(update.getEmail());
        if (update.getLevel() != null) user.setLevel(update.getLevel());
        if (update.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(update.getPassword()));
        }
        userMapper.updateById(user);
    }
}
