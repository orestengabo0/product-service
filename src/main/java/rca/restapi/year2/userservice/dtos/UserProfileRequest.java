package rca.restapi.year2.userservice.dtos;


import lombok.Data;

@Data
public class UserProfileRequest {
    private String username;
    private String email;
}
