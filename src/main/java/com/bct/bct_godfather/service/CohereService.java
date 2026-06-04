package com.bct.bct_godfather.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CohereService {

    @Value("${cohere.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    // channelId -> message history
    private final Map<String, List<Map<String, String>>> chatHistory = new HashMap<>();

    private static final int MAX_MESSAGES = 20;

    public String getResponse(String channelId, String prompt) {

        try {
            String url = "https://api.cohere.com/v2/chat";

           
            chatHistory.putIfAbsent(channelId, new ArrayList<>());
            List<Map<String, String>> history = chatHistory.get(channelId);

            
            history.add(Map.of(
                    "role", "user",
                    "content", prompt
            ));

           
            if (history.size() > MAX_MESSAGES) {
                history = history.subList(history.size() - MAX_MESSAGES, history.size());
                chatHistory.put(channelId, history);
            }

            
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            List<Map<String, String>> messages = new ArrayList<>();

            // system prompt (important)
            messages.add(Map.of(
                    "role", "system",
                    "content",
                    """
                    You are a Discord bot for BCT081 students.
                    Be helpful, polite, and concise.
                    """
            ));

            // add history
            messages.addAll(history);

            Map<String, Object> body = new HashMap<>();
            body.put("model", "command-a-plus-05-2026");
            body.put("messages", messages);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(url, entity, Map.class);

            Map responseBody = response.getBody();

            if (responseBody == null || !responseBody.containsKey("message")) {
                return "No response from AI 💀";
            }

            Map<String, Object> message =
                    (Map<String, Object>) responseBody.get("message");

            List<Map<String, Object>> content =
                    (List<Map<String, Object>>) message.get("content");

            if (content == null || content.isEmpty()) {
                return "Empty AI response 💀";
            }

            String text = null;

            for (Map<String, Object> part : content) {
                if ("text".equals(part.get("type"))) {
                    text = part.get("text").toString();
                    break;
                }
            }

            if (text == null) {
                return "No text response 💀";
            }

            
            history.add(Map.of(
                    "role", "assistant",
                    "content", text
            ));

            return text;

        } catch (Exception e) {
            e.printStackTrace();
            return "AI request failed 💀";
        }
    }
}