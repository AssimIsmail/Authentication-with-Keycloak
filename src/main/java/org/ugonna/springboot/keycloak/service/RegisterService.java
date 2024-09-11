package org.ugonna.springboot.keycloak.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.ugonna.springboot.keycloak.dto.RegisterRequest;

import java.util.HashMap;
import java.util.Map;

@Service
public class RegisterService {

    @Value("${app.keycloak.admin.token.url}")
    private String adminTokenUrl;

    @Value("${app.keycloak.admin.client-id}")
    private String adminClientId;

    @Value("${app.keycloak.admin.client-secret}")
    private String adminClientSecret;

    @Value("${app.keycloak.register.url}")
    private String registerUrl;

    private final RestTemplate restTemplate;

    public RegisterService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<String> register(RegisterRequest request) {
        // Étape 1: Obtenir le jeton d'accès admin
        String token = getAdminAccessToken();

        // Étape 2: Enregistrer l'utilisateur
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);

        // Créer le payload pour la demande de création d'utilisateur
        Map<String, Object> userPayload = new HashMap<>();
        userPayload.put("username", request.getUsername());
        userPayload.put("email", request.getEmail());
        userPayload.put("firstName", request.getFirstName());
        userPayload.put("lastName", request.getLastName());
        userPayload.put("enabled", true); // Activer l'utilisateur

        // Ajouter le mot de passe
        Map<String, String> credentials = new HashMap<>();
        credentials.put("type", "password");
        credentials.put("value", request.getPassword());
        credentials.put("temporary", "false");

        userPayload.put("credentials", new Map[]{credentials});

        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(userPayload, headers);
        return restTemplate.postForEntity(registerUrl, httpEntity, String.class);
    }

    private String getAdminAccessToken() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("grant_type", "client_credentials");
            map.add("client_id", adminClientId);
            map.add("client_secret", adminClientSecret);

            HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(adminTokenUrl, httpEntity, String.class);

            // Check for HTTP status and response body
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                String responseBody = responseEntity.getBody();
                if (responseBody != null) {
                    // Parse the response to extract the access token
                    String accessToken = parseAccessToken(responseBody);
                    if (accessToken != null) {
                        return accessToken;
                    } else {
                        throw new RuntimeException("Access token not found in response");
                    }
                } else {
                    throw new RuntimeException("Response body is null");
                }
            } else {
                throw new RuntimeException("Failed to obtain token. Status code: " + responseEntity.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Exception during token request", e);
        }
    }

    private String parseAccessToken(String responseBody) {
        try {
            // Use a JSON library to parse the response body
            com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode jsonNode = objectMapper.readTree(responseBody);
            return jsonNode.path("access_token").asText();
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Error parsing token response", e);
        }
    }
}
