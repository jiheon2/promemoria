package kopo.userservice.dto;

import lombok.Builder;

@Builder
public record FriendDTO(
        String userId,
        String friendId
) {
}
