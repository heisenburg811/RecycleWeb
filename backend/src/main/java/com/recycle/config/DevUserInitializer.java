package com.recycle.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recycle.user.domain.User;
import com.recycle.user.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 开发环境演示账号：手机号 13800000001 / 13800000002，密码统一 123456。
 *
 * <p>密码在运行期用 BCrypt 加密写入，不在 SQL 里硬编码摘要。
 * <p>不指定 id：H2 自增序列分配，首次启动即得到 1 和 2，与 data.sql 里的 seller_id 对应。
 * 显式写 id 会让序列停在 1，后续注册直接撞主键。
 */
@Component
@Profile("dev")
public class DevUserInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevUserInitializer.class);
    private static final String DEFAULT_PASSWORD = "123456";

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public DevUserInitializer(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        createIfAbsent("13800000001", "旧物新生");
        createIfAbsent("13800000002", "搬家急出");
    }

    private void createIfAbsent(String phone, String nickname) {
        boolean exists = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getPhone, phone)) > 0;
        if (exists) {
            return;
        }
        User user = new User();
        user.setPhone(phone);
        user.setNickname(nickname);
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setRole("both");
        user.setCreditScore(100);
        user.setStatus(1);
        userMapper.insert(user);
        log.info("已创建演示账号 {} / {} (id={})", phone, DEFAULT_PASSWORD, user.getId());
    }
}
