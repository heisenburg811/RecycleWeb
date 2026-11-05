package com.recycle.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recycle.common.exception.BizException;
import com.recycle.user.domain.User;
import com.recycle.user.dto.LoginReq;
import com.recycle.user.dto.RegisterReq;
import com.recycle.user.mapper.UserMapper;
import com.recycle.user.security.JwtUtil;
import com.recycle.user.vo.LoginVO;
import com.recycle.user.vo.UserVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserMapper userMapper, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterReq req) {
        boolean exists = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getPhone, req.phone())) > 0;
        if (exists) {
            throw new BizException("该手机号已注册，请直接登录");
        }

        User user = new User();
        user.setPhone(req.phone());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setNickname(StringUtils.hasText(req.nickname())
                ? req.nickname()
                : "用户" + req.phone().substring(7));
        user.setRole("both");
        user.setCreditScore(100);
        user.setStatus(1);
        userMapper.insert(user);

        return issue(user);
    }

    public LoginVO login(LoginReq req) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPhone, req.phone()));
        // 用户不存在与密码错误返回同一句提示，避免手机号被枚举
        if (user == null || !passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new BizException("手机号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BizException("账号已被禁用，请联系客服");
        }
        return issue(user);
    }

    public UserVO me(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return UserVO.of(user);
    }

    private LoginVO issue(User user) {
        String token = jwtUtil.generate(user.getId(), user.getPhone(), user.getNickname());
        return new LoginVO(token, jwtUtil.getExpireSeconds(), UserVO.of(user));
    }
}
