package com.english.platform.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EssayDTO {
    @NotBlank(message = "作文题目不能为空")
    private String topic;
    @NotBlank(message = "作文内容不能为空")
    private String content;
}
