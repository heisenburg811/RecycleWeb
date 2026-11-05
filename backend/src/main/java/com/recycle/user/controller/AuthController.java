package com.recycle.user.controller;

import com.recycle.common.api.R;
import com.recycle.user.dto.LoginReq;
import com.recycle.user.dto.RefreshReq;
import com.recycle.user.dto.RegisterReq;
import com.recycle.user.security.LoginUser;
import com.recycle.user.service.AuthService;
import com.recycle.user.vo.LoginVO;
import com.recycle.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证", description = "注册 / 登录 / 刷新 / 登出 / 当前用户")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "注册", description = "手机号 + 密码，成功直接返回一对令牌")
    @PostMapping("/register")
    public R<LoginVO> register(@Valid @RequestBody RegisterReq req) {
        return R.ok(authService.register(req));
    }

    @Operation(summary = "登录")
    @PostMapping("/login")
    public R<LoginVO> login(@Valid @RequestBody LoginReq req) {
        return R.ok(authService.login(req));
    }

    @Operation(summary = "刷新令牌",
            description = "用 refreshToken 换新的 accessToken + refreshToken，旧的立即作废（轮换）")
    @PostMapping("/refresh")
    public R<LoginVO> refresh(@Valid @RequestBody RefreshReq req) {
        return R.ok(authService.refresh(req.refreshToken()));
    }

    @Operation(summary = "登出", description = "撤销 refreshToken，accessToken 到期自然失效")
    @PostMapping("/logout")
    public R<Void> logout(@RequestBody(required = false) RefreshReq req) {
        authService.logout(req == null ? null : req.refreshToken());
        return R.ok();
    }

    @Operation(summary = "当前登录用户")
    @GetMapping("/me")
    public R<UserVO> me(@AuthenticationPrincipal LoginUser user) {
        return R.ok(authService.me(user.id()));
    }
}
