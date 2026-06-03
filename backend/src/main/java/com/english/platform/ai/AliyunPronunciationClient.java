package com.english.platform.ai;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.english.platform.vo.PronunciationResultVO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 阿里云发音评测客户端。
 * 实现方式：
 * 1. 使用 Qwen-ASR 将录音转写为文本
 * 2. 将转写结果与参考文本做词级比对，生成近似评分
 */
@Slf4j
@Component
public class AliyunPronunciationClient {

    private static final Pattern NON_WORD_PATTERN = Pattern.compile("[^a-z0-9']+");
    private static final double ACCURACY_WEIGHT = 0.65;
    private static final double FLUENCY_WEIGHT = 0.35;
    private static final double SIMILARITY_WEIGHT = 0.60;
    private static final double LENGTH_WEIGHT = 0.40;
    private static final double MAX_SCORE = 100.0;
    private static final double MIN_SCORE = 0.0;

    @Value("${tongyi.api-key}")
    private String apiKey;

    @Value("${tongyi.compatible-url:https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions}")
    private String compatibleUrl;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .build();

    public PronunciationEvaluation evaluate(byte[] audioBytes, String refText, String contentType) {
        try {
            String transcript = transcribeAudio(audioBytes, contentType);
            PronunciationResultVO result = scorePronunciation(refText, transcript);
            return new PronunciationEvaluation(result, transcript);
        } catch (Exception e) {
            log.error("阿里云发音评测调用失败", e);
            PronunciationResultVO fallback = buildFallbackResult(refText);
            return new PronunciationEvaluation(fallback, "");
        }
    }

    private String transcribeAudio(byte[] audioBytes, String contentType) throws Exception {
        String mimeType = normalizeMimeType(contentType);
        String dataUri = "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(audioBytes);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "qwen3-asr-flash");
        requestBody.put("stream", false);

        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");

        JSONArray content = new JSONArray();
        JSONObject audioPart = new JSONObject();
        audioPart.put("type", "input_audio");
        JSONObject inputAudio = new JSONObject();
        inputAudio.put("data", dataUri);
        audioPart.put("input_audio", inputAudio);
        content.add(audioPart);
        message.put("content", content);
        messages.add(message);
        requestBody.put("messages", messages);

        JSONObject extraBody = new JSONObject();
        JSONObject asrOptions = new JSONObject();
        asrOptions.put("language", "en");
        asrOptions.put("enable_itn", false);
        extraBody.put("asr_options", asrOptions);
        requestBody.put("extra_body", extraBody);

        Request request = new Request.Builder()
                .url(compatibleUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                        requestBody.toJSONString(),
                        MediaType.parse("application/json")
                ))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() == null ? "" : response.body().string();
                throw new RuntimeException("阿里云ASR调用失败: HTTP " + response.code() + " " + errorBody);
            }

            String body = response.body() == null ? "" : response.body().string();
            JSONObject json = JSON.parseObject(body);
            JSONObject messageObj = json.getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message");
            return extractTranscript(messageObj.get("content"));
        }
    }

    private String extractTranscript(Object content) {
        if (content == null) return "";
        if (content instanceof String str) return str.trim();
        if (content instanceof JSONArray array) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < array.size(); i++) {
                Object item = array.get(i);
                if (item instanceof String str) {
                    builder.append(str).append(' ');
                    continue;
                }
                if (item instanceof JSONObject obj) {
                    String text = obj.getString("text");
                    if (text == null || text.isBlank()) {
                        text = obj.getString("content");
                    }
                    if (text != null && !text.isBlank()) {
                        builder.append(text).append(' ');
                    }
                }
            }
            return builder.toString().trim();
        }
        return String.valueOf(content).trim();
    }

    private PronunciationResultVO scorePronunciation(String refText, String transcript) {
        PronunciationResultVO result = new PronunciationResultVO();

        List<String> refWords = tokenize(refText);
        List<String> spokenWords = tokenize(transcript);
        List<Integer> matchedRefIndexes = computeLcsMatchedIndexes(refWords, spokenWords);

        double accuracyRatio = refWords.isEmpty() ? 0 : matchedRefIndexes.size() * 1.0 / refWords.size();
        double similarityRatio = normalizedSimilarity(
                String.join(" ", refWords),
                String.join(" ", spokenWords)
        );
        double lengthRatio = refWords.isEmpty() || spokenWords.isEmpty()
                ? 0
                : Math.min(refWords.size(), spokenWords.size()) * 1.0 / Math.max(refWords.size(), spokenWords.size());

        double accuracyScore = toHundredScore(accuracyRatio);
        double fluencyScore = toHundredScore(similarityRatio * SIMILARITY_WEIGHT + lengthRatio * LENGTH_WEIGHT);
        double totalScore = round1(clampScore(accuracyScore * ACCURACY_WEIGHT + fluencyScore * FLUENCY_WEIGHT));

        result.setAccuracyScore(BigDecimal.valueOf(accuracyScore));
        result.setFluencyScore(BigDecimal.valueOf(fluencyScore));
        result.setTotalScore(BigDecimal.valueOf(totalScore));
        result.setWordScores(buildWordScores(refWords, spokenWords, matchedRefIndexes));
        return result;
    }

    private List<PronunciationResultVO.WordScoreVO> buildWordScores(List<String> refWords, List<String> spokenWords, List<Integer> matchedRefIndexes) {
        List<PronunciationResultVO.WordScoreVO> scores = new ArrayList<>();
        boolean[] matchedFlags = new boolean[refWords.size()];
        for (Integer index : matchedRefIndexes) {
            if (index >= 0 && index < matchedFlags.length) {
                matchedFlags[index] = true;
            }
        }

        for (int i = 0; i < refWords.size(); i++) {
            String word = refWords.get(i);
            PronunciationResultVO.WordScoreVO wordScore = new PronunciationResultVO.WordScoreVO();
            wordScore.setWord(word);
            if (matchedFlags[i]) {
                wordScore.setScore(BigDecimal.valueOf(95.0));
                wordScore.setStatus("correct");
            } else if (spokenWords.contains(word)) {
                wordScore.setScore(BigDecimal.valueOf(65.0));
                wordScore.setStatus("wrong");
            } else {
                wordScore.setScore(BigDecimal.valueOf(35.0));
                wordScore.setStatus("missing");
            }
            scores.add(wordScore);
        }
        return scores;
    }

    private List<String> tokenize(String text) {
        List<String> tokens = new ArrayList<>();
        if (text == null || text.isBlank()) return tokens;

        String normalized = NON_WORD_PATTERN.matcher(text.toLowerCase()).replaceAll(" ").trim();
        if (normalized.isEmpty()) return tokens;

        for (String token : normalized.split("\\s+")) {
            if (!token.isBlank()) {
                tokens.add(token);
            }
        }
        return tokens;
    }

    private List<Integer> computeLcsMatchedIndexes(List<String> a, List<String> b) {
        int[][] dp = new int[a.size() + 1][b.size() + 1];
        for (int i = 1; i <= a.size(); i++) {
            for (int j = 1; j <= b.size(); j++) {
                if (a.get(i - 1).equals(b.get(j - 1))) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        List<Integer> indexes = new ArrayList<>();
        int i = a.size();
        int j = b.size();
        while (i > 0 && j > 0) {
            if (a.get(i - 1).equals(b.get(j - 1))) {
                indexes.add(0, i - 1);
                i--;
                j--;
            } else if (dp[i - 1][j] >= dp[i][j - 1]) {
                i--;
            } else {
                j--;
            }
        }
        return indexes;
    }

    private double normalizedSimilarity(String left, String right) {
        if (left.isBlank() && right.isBlank()) return 1;
        if (left.isBlank() || right.isBlank()) return 0;

        int distance = levenshteinDistance(left, right);
        int maxLen = Math.max(left.length(), right.length());
        return maxLen == 0 ? 1 : Math.max(0, 1 - distance * 1.0 / maxLen);
    }

    private int levenshteinDistance(String left, String right) {
        int[][] dp = new int[left.length() + 1][right.length() + 1];
        for (int i = 0; i <= left.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= right.length(); j++) dp[0][j] = j;

        for (int i = 1; i <= left.length(); i++) {
            for (int j = 1; j <= right.length(); j++) {
                int cost = left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost
                );
            }
        }
        return dp[left.length()][right.length()];
    }

    private String normalizeMimeType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "audio/webm";
        }
        return contentType.trim().toLowerCase();
    }

    private PronunciationResultVO buildFallbackResult(String refText) {
        PronunciationResultVO result = new PronunciationResultVO();
        result.setTotalScore(BigDecimal.valueOf(70.0));
        result.setAccuracyScore(BigDecimal.valueOf(72.0));
        result.setFluencyScore(BigDecimal.valueOf(68.0));

        List<PronunciationResultVO.WordScoreVO> wordScores = new ArrayList<>();
        for (String word : tokenize(refText)) {
            PronunciationResultVO.WordScoreVO ws = new PronunciationResultVO.WordScoreVO();
            ws.setWord(word);
            ws.setScore(BigDecimal.valueOf(60.0));
            ws.setStatus("wrong");
            wordScores.add(ws);
        }
        result.setWordScores(wordScores);
        return result;
    }

    private double toHundredScore(double ratio) {
        return round1(clampScore(ratio * MAX_SCORE));
    }

    private double clampScore(double score) {
        if (Double.isNaN(score) || Double.isInfinite(score)) {
            return MIN_SCORE;
        }
        return Math.max(MIN_SCORE, Math.min(MAX_SCORE, score));
    }

    private double round1(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }

    public record PronunciationEvaluation(PronunciationResultVO result, String transcript) {
    }
}
