package com.english.platform.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 通义千问 API 客户端
 * 文档：https://help.aliyun.com/zh/dashscope/developer-reference/api-details
 */
@Slf4j
@Component
public class TongyiAiClient {

    @Value("${tongyi.api-key}")
    private String apiKey;

    @Value("${tongyi.base-url}")
    private String baseUrl;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    /**
     * 调用通义千问生成文本
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt   用户输入
     * @return AI生成的文本
     */
    public String chat(String systemPrompt, String userPrompt) {
        // 构建请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "qwen-turbo");

        JSONObject input = new JSONObject();
        JSONArray messages = new JSONArray();

        JSONObject systemMsg = new JSONObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);

        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", userPrompt);
        messages.add(userMsg);

        input.put("messages", messages);
        requestBody.put("input", input);

        JSONObject parameters = new JSONObject();
        parameters.put("result_format", "message");
        parameters.put("max_tokens", 2000);
        parameters.put("temperature", 0.7);
        requestBody.put("parameters", parameters);

        Request request = new Request.Builder()
                .url(baseUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                        requestBody.toJSONString(),
                        MediaType.parse("application/json")
                ))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("通义千问API调用失败: HTTP {}", response.code());
                throw new RuntimeException("AI服务调用失败: " + response.code());
            }
            String body = response.body().string();
            JSONObject result = JSON.parseObject(body);
            // 解析返回格式
            return result.getJSONObject("output")
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");
        } catch (Exception e) {
            log.error("通义千问API调用异常", e);
            throw new RuntimeException("AI服务暂时不可用，请稍后重试");
        }
    }

    /**
     * AI智能出题：根据知识点和难度生成题目
     *
     * @param knowledgeName 知识点名称
     * @param difficulty    难度 1-易 2-中 3-难
     * @param count         出题数量
     * @return JSON格式的题目列表字符串
     */
    public String generateQuestions(String knowledgeName, int difficulty, int count) {
        String difficultyStr = difficulty == 1 ? "简单" : difficulty == 2 ? "中等" : "困难";
        String systemPrompt = """
                你是一位专业的英语教师，擅长出英语练习题。
                请根据要求生成高质量的英语练习题，返回严格的JSON格式。
                """;
        String userPrompt = String.format("""
                请生成%d道关于"%s"的英语单选题，难度为%s。
                返回JSON数组格式，每道题包含：
                {
                  "title": "题目内容（英文）",
                  "options": [
                    {"key": "A", "value": "选项A"},
                    {"key": "B", "value": "选项B"},
                    {"key": "C", "value": "选项C"},
                    {"key": "D", "value": "选项D"}
                  ],
                  "answer": "正确答案字母如A",
                  "analysis": "题目解析（中文）"
                }
                只返回JSON数组，不要有其他内容。
                """, count, knowledgeName, difficultyStr);

        return chat(systemPrompt, userPrompt);
    }

    /**
     * AI英语作文批改
     *
     * @param topic   作文题目
     * @param content 作文内容
     * @return JSON格式批改结果
     */
    public String correctEssay(String topic, String content) {
        String systemPrompt = """
                你是一位专业的英语作文批改老师，能够给出全面、专业的作文批改反馈。
                请从语法、内容、结构、词汇四个维度评分（各满分100分），并给出修改建议。
                """;
        String userPrompt = String.format("""
                请批改以下英语作文：

                题目：%s

                作文内容：
                %s

                请返回JSON格式：
                {
                  "totalScore": 总分(四维度平均),
                  "grammarScore": 语法分(0-100),
                  "contentScore": 内容分(0-100),
                  "structureScore": 结构分(0-100),
                  "vocabularyScore": 词汇分(0-100),
                  "feedback": "详细批改反馈（中文，指出具体错误和优点）",
                  "correction": "改写后的优化版本（英文）"
                }
                只返回JSON，不要有其他内容。
                """, topic, content);

        return chat(systemPrompt, userPrompt);
    }

    /**
     * 生成发音评测的AI反馈建议
     */
    public String generatePronunciationFeedback(String refText, String transcript, double totalScore, double accuracyScore, double fluencyScore) {
        String userPrompt = String.format("""
                用户进行了英语发音评测。
                参考文本："%s"
                语音识别结果："%s"
                总分：%.1f/100，准确度：%.1f/100，流利度：%.1f/100

                请用中文给出简洁的发音建议（2-3句话），重点指出需要改进的方面。
                """, refText, transcript, totalScore, accuracyScore, fluencyScore);

        return chat("你是专业英语发音教练。", userPrompt);
    }
}
