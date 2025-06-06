package rca.restapi.year2.userservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import rca.restapi.year2.userservice.dtos.UserCreatedEvent;

@Service
public class KafkaUserConsumer {
    @Autowired
    private UserProfileService userProfileService;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(KafkaUserConsumer.class);

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "user.created", groupId = "user-service-group")
    @Transactional
    public void consumeUserCreated(String message) {
        try{
            UserCreatedEvent event = objectMapper.readValue(message, UserCreatedEvent.class);
            log.info("Received user.created event: {}", event);
            userProfileService.createUserProfile(event);
            log.info("User profile created for UUID: {}", event.getUuid());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
