package kopo.frontservice.dto;

import lombok.Builder;

@Builder
public record MsgDTO(
        String msg
) {
}
