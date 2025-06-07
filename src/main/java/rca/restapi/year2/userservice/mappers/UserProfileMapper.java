package rca.restapi.year2.userservice.mappers;

import org.mapstruct.Mapper;
import rca.restapi.year2.userservice.dtos.UserProfileResponse;
import rca.restapi.year2.userservice.model.UserProfile;

@Mapper( componentModel = "spring" )
public interface UserProfileMapper {
    UserProfileResponse toResponse(UserProfile userProfile);
}
