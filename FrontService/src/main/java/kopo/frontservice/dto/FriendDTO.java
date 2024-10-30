package kopo.frontservice.dto;

import lombok.Builder;

@Builder
public record FriendDTO(
        String userId,
        String friendId
) {
}
