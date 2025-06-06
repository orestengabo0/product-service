package rca.restapi.year2.userservice.client;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;


@Service
public class AuthClient {
    private final WebClient webClient;
    public AuthClient(WebClient.Builder builder) {
        this.webClient = builder.baseUrl("http://auth-service").build();
    }
    public boolean userExists(UUID uuid) {
        System.out.println("Calling auth-service with UUID: " + uuid);
        try {
            Boolean result = this.webClient.get()
                    .uri("/api/auth/validate/{uuid}", uuid)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();

            System.out.println("Result from auth-service: " + result);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            System.err.println("Exception during WebClient call: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}
