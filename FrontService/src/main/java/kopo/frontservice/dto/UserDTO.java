package kopo.frontservice.dto;

import lombok.Builder;

@Builder
public record UserDTO(
        String userId,
        int userAge,
        String userGender
) {
}
