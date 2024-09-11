package org.ugonna.springboot.keycloak.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.ugonna.springboot.keycloak.dto.LoginRequest;
import org.ugonna.springboot.keycloak.dto.LoginResponse;

@Service
public class LoginService {

    @Value("${app.keycloak.login.url}")
    private String loginUrl;
    @Value("${app.keycloak.client-secret}")
    private String clientSecret;
    @Value("${app.keycloak.grant-type}")
    private String grantType;
    @Value("${app.keycloak.client-id}")
    private String clientId;

    @Autowired
    private RestTemplate restTemplate;

    public ResponseEntity<LoginResponse> login(LoginRequest request) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", request.getUsername());
        map.add("password", request.getPassword());
        map.add("client_id", clientId);
        map.add("client_secret", clientSecret);
        map.add("grant_type", grantType);

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(map, headers);
        ResponseEntity<LoginResponse> responseEntity = restTemplate.postForEntity(loginUrl, httpEntity, LoginResponse.class);

        // Extract the body from the response entity
        LoginResponse loginResponse = responseEntity.getBody();
        if (loginResponse != null) {
            // Modify the loginResponse object
            loginResponse.setUsername(request.getUsername());
        }

        // Return a new ResponseEntity with the modified loginResponse
        return ResponseEntity.status(HttpStatus.OK).body(loginResponse);
    }
}
