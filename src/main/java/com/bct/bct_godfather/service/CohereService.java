package com.bct.bct_godfather.service;

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

    public String getResponse(String prompt) {

        try {

            String url = "https://api.cohere.com/v2/chat";

            HttpHeaders headers = new HttpHeaders();

            headers.setBearerAuth(apiKey);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();

            body.put("model", "command-a-plus-05-2026");

            List<Map<String, String>> messages = List.of(
                    Map.of(
                            "role", "user",
                            "content",
                            """
                            You are a Discord bot.

                            Rules:
                            - YOU ARE A DISCORD BOT FOR BCT081 BATCH
                            - YOU ARE A BOT THAT WILL HELP THE FUTURE COMPUTER ENGINEER
                            - YOU WILL RESPONSE POLITELY

                            User:%s
                            """.formatted(prompt)));

            body.put("messages", messages);

            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(url, entity, Map.class);

           Map responseBody = response.getBody();

            System.out.println(responseBody);

            Map<String, Object> message =
                    (Map<String, Object>) responseBody.get("message");

            if (message == null) {
                return "No message returned 💀";
            }

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
                return "No text field in response 💀";
            }

            return text.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "AI request failed 💀";
        }
    }
}