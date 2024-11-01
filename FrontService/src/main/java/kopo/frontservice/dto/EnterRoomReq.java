package kopo.frontservice.dto;

import lombok.Data;

@Data
public class EnterRoomReq {
    private String roomId;
    private String camKey;
}
