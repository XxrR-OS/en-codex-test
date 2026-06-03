package com.english.platform.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterDTO {
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 20, message = "用户名4-20位")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 30, message = "密码6-30位")
    private String password;

    private String nickname;

    @Email(message = "邮箱格式不正确")
    private String email;

    /** 英语等级 1-初级 2-中级 3-高级 */
    private Integer level = 1;
}
