package rca.restapi.year2.userservice.dtos;

import lombok.Data;

@Data
public class UserCreatedEvent {
    private String uuid;
    private String username;
    private String email;
}
