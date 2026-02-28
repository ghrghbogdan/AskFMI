package com.example.askfmi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AiService {

    @Value("${ai.api.base-url}")
    private String aiApiBaseUrl;

    private final RestTemplate restTemplate;

    public AiService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * send query to ai api and get response
     * calls POST /query endpoint with {"query": "text"}
     * returns the "answer" field from response
     */
    public String query(String question) {
        try {
            // prepare request payload matching ai api format
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("query", question);

            // set json content type header
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            // call ai api endpoint
            String url = aiApiBaseUrl + "/query";
            Map<String, String> response = restTemplate.postForObject(url, request, Map.class);

            // extract answer from response
            if (response != null && response.containsKey("answer")) {
                return response.get("answer");
            }

            return "Nu am putut obține un răspuns de la serviciul AI.";

        } catch (Exception e) {
            // log error and return fallback message
            System.err.println("Error calling AI API: " + e.getMessage());
            return "Serviciul AI este momentan indisponibil. Vă rugăm să încercați mai târziu.";
        }
    }
}
