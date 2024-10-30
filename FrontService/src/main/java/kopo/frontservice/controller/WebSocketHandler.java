package kopo.frontservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Slf4j
@RequiredArgsConstructor
@RestController
// WebSocket을 통해 클라이언트와 서버 간의 실시간 통신을 처리하는 역할
public class WebSocketHandler extends TextWebSocketHandler {

    // offer : 정보를 주고 받기 위한 webSocket
    // camKey : 요청하는 각 캠의 key, roomId : 룸 아이디
    @MessageMapping("/peer/offer/{camKey}/{roomId}")
    @SendTo("/videoCall/peer/offer/{camKey}/{roomId}")
    public String PeerHandleOffer(@Payload String offer,
                                  @DestinationVariable(value = "camKey") String camKey,
                                  SimpMessageHeaderAccessor headerAccessor) {

        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        log.info("[USER-ID] {}", userId);
        log.info("[OFFER] {} : {}", camKey, offer);

        return offer;
    }

    // iceCandidate : 정보를 주고 받기 위한 webSocket
    @MessageMapping("/peer/iceCandidate/{camKey}/{roomId}")
    @SendTo("/videoCall/peer/iceCandidate/{camKey}/{roomId}")
    public String PeerHandleIceCandidate(@Payload String candidate,
                                         @DestinationVariable(value= "camKey") String camKey,
                                         SimpMessageHeaderAccessor headerAccessor) {

        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        log.info("[USER-ID] {}", userId);
        log.info("[ICECANDIDATE] {} : {}", camKey, candidate);

        return candidate;
    }

    @MessageMapping("/peer/answer/{camKey}/{roomId}")
    @SendTo("/videoCall/peer/answer/{camKey}/{roomId}")
    public String PeerHandelAnswer(@Payload String answer,
                                   @DestinationVariable(value = "camKey") String camKey,
                                   SimpMessageHeaderAccessor headerAccessor) {
        String userId = (String) headerAccessor.getSessionAttributes().get("userId");
        log.info("[USER-ID] {}", userId);
        log.info("[ANSWER] {} : {}", camKey, answer);

        return answer;
    }

    // camKey를 받기 위해 신호를 보내는 webSocket(새로운 참가자 방에 입장, 키를 전송하여 다른 참가자들이 자신을 알 수 있게 하기 위함)
    @MessageMapping("/call/key")
    @SendTo("/videoCall/call/key")
    public String callKey(@Payload String message, SimpMessageHeaderAccessor headerAccessor) {
        log.info("[KEY] {}", message);

        return message;
    }

    // 자신의 camKey를 모든 연결된 세션에 보내는 webSocket(다른 참가자들의 키 수신)
    @MessageMapping("/send/key")
    @SendTo("/videoCall/send/key")
    public String sendKey(@Payload String message, SimpMessageHeaderAccessor headerAccessor) {
        return message;
    }


}
