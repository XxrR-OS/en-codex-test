package com.english.platform.vo;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String avatar;
    private Integer level;
    private Integer totalScore;
    private String token;
}
