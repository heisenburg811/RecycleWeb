package com.recycle.user.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户。密码只存 BCrypt 摘要，永不返回给前端。
 */
@Data
@TableName("app_user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String phone;

    private String nickname;

    private String avatar;

    /** BCrypt 摘要 */
    private String password;

    /** buyer / seller / both / admin */
    private String role;

    /** 预留给二期信用分 */
    private Integer creditScore;

    /** 1 正常 / 0 封禁 */
    private Integer status;

    private LocalDateTime createdAt;
}
