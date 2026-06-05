package com.bct.bct_godfather.manga;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;





@Component
public class MangaDexClient {

    @Value("${mangadex.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate;

    public MangaDexClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public JsonNode searchManga(String title) {
        String url = baseUrl + "/manga?title=" + URLEncoder.encode(title, StandardCharsets.UTF_8)
           + "&limit=5&availableTranslatedLanguage[]=en";

        JsonNode response = restTemplate.getForObject(url, JsonNode.class);
        return response != null ? response.get("data") : null;
    }

    public JsonNode getChapterInfo(String chapterId) {
        String url = baseUrl + "/chapter/" + chapterId;
        return restTemplate.getForObject(url, JsonNode.class);
    }

    public List<String> getChapterPagesSaver(String chapterId) {
    try {
        String url = baseUrl + "/at-home/server/" + chapterId;
        JsonNode response = restTemplate.getForObject(url, JsonNode.class);

        if (response == null) return List.of();

        String host = response.get("baseUrl").asText();
        String hash = response.get("chapter").get("dataSaver").asText();
        JsonNode pages = response.get("chapter").get("dataSaver");

        // fix: get hash from chapter.hash, dataSaver is the page list
        hash = response.get("chapter").get("hash").asText();

        List<String> urls = new ArrayList<>();
        for (JsonNode page : pages) {
            urls.add(host + "/data-saver/" + hash + "/" + page.asText());
        }
        return urls;
    } catch (HttpClientErrorException.NotFound e) {
        throw new RuntimeException("Chapter not found.");
    }
}
    public JsonNode getChapters(String mangaId, int limit) {
        String url = baseUrl + "/manga/" + mangaId + "/feed"
           + "?translatedLanguage[]=en"
           + "&order[chapter]=desc"
           + "&limit=" + limit
           + "&includes[]=scanlation_group";

        JsonNode response = restTemplate.getForObject(url, JsonNode.class);
        return response != null ? response.get("data") : null;
    }

    public List<String> getChapterPages(String chapterId) {
        String url = baseUrl + "/at-home/server/" + chapterId;
        JsonNode response = restTemplate.getForObject(url, JsonNode.class);

        if (response == null) return List.of();

        String host = response.get("baseUrl").asText();
        String hash = response.get("chapter").get("hash").asText();
        JsonNode pages = response.get("chapter").get("data");

        List<String> urls = new ArrayList<>();
        for (JsonNode page : pages) {
            urls.add(host + "/data/" + hash + "/" + page.asText());
        }
        return urls;
    }

    public byte[] downloadPage(String imageUrl) {
        return restTemplate.getForObject(imageUrl, byte[].class);
    }
}