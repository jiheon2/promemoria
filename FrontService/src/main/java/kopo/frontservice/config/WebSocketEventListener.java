package kopo.frontservice.config;

import kopo.frontservice.chat.ChatHandler;
import kopo.frontservice.dto.CommonResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
public class WebSocketEventListener {

    @Autowired
    private GlobalVariables globalVariables;


    @EventListener
    public void handleWebsocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        Map<String, List<String>> nativeHeaders = getNativeHeaders(event);
        String roomName = nativeHeaders.get("roomName").get(0);
        String camKey = nativeHeaders.get("camKey").get(0);

        //전역 함수에서 checkRoomId map을 가져와, 해당 세션 Id에 대한 룸 Id 가 있는지 확인
        if(!globalVariables.getCheckRoomId().containsKey(sessionId)){
            //없다면 추가 해준다.
            globalVariables.getCheckRoomId().put(sessionId, roomName);

        }

        //전역 함수에서 checkRoomIdCount map 를 가져와, 해당 룸 Id에 대한 유저수가 있는지 확인
        if(globalVariables.getCheckRoomIdCount().containsKey(roomName)){
            //있다면 유저수를 +1 해준다.
            globalVariables.getCheckRoomIdCount().put(roomName, globalVariables.getCheckRoomIdCount().get(roomName)+1);
        }
        else{
            //아니면 1로 추가해준다.
            globalVariables.getCheckRoomIdCount().put(roomName, 1);
        }

        //전역 함수에서 checkCamKey map 를 가져와, 해당 세션 Id에 대한 camKey가 있는지 확인
        if(!globalVariables.getCheckCamKey().containsKey(sessionId)){
            //없다면 추가해준다.
            globalVariables.getCheckCamKey().put(sessionId, camKey);
        }

        log.info("\n웹소켓 접속 : "+ sessionId + "\n"
                + "룸 이름 : "+ roomName + "\n"
                + "룸 인원 : "+ globalVariables.getCheckRoomIdCount().get(roomName));
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event){
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        String roomId = globalVariables.getCheckRoomId().get(sessionId);

        //전역 함수에서 checkRoomIdCount map 을 가져와 해당 룸이 있는지 확인
        if(globalVariables.getCheckRoomIdCount().containsKey(roomId)){
            if(globalVariables.getCheckRoomIdCount().get(roomId) - 1 <= 0){
                //만약 해당 roomId의 유저가 0 이하라면 삭제한다.
                globalVariables.getCheckRoomIdCount().remove(roomId);

                // roomInfo에서 해당 방의 정보를 삭제
                ChatHandler.roomInfo.remove(roomId);
            } else{
                //아니면 해당 roomId의 유저를 -1 해준다.
                globalVariables.getCheckRoomIdCount().put(roomId, globalVariables.getCheckRoomIdCount().get(roomId) - 1);
            }
        }

        //전역 함수에 roomCheckWaitingClient map 을 가져와 해당 룸이 있는지 확인 한다.
        if(globalVariables.getRoomCheckWaitingClient().containsKey(roomId)){
            Map<String , String> returnMap = new HashMap<>();

            returnMap.put("camKey", globalVariables.getCheckCamKey().get(sessionId));
            returnMap.put("roomCount", String.valueOf(globalVariables.getCheckRoomIdCount().get(roomId)));

            //해당 roomCheckWaitingClient 에서 DeferredResult 에 setResult를 보내어서 해당되는 /poll/leave/room/{roomId} api에 신호를 보낸다.
            globalVariables.getRoomCheckWaitingClient().get(roomId).setResult(
                    new ResponseEntity<>(CommonResp.builder()
                            .data(returnMap)
                            .status_code(HttpStatus.OK.value())
                            .result(CommonResp.ResultType.SUCCESS)
                            .build(),
                            HttpStatus.OK)
            );
        }



        log.info("\n웹소켓 끊김 : " + sessionId + "\n"
                +"룸 ID : " + roomId + "\n"
                +"룸 인원 : " + globalVariables.getCheckRoomIdCount().get(roomId) );
    }


    //SessionConnectedEvent 에서 NativeHeader 찾기 메서드
    private Map<String, List<String>> getNativeHeaders(SessionConnectedEvent event){
        //messageHeaders 를 추출
        MessageHeaders headers = event.getMessage().getHeaders();
        //simpConnectMessage 를 추출
        GenericMessage<?> simpConnectMessage = (GenericMessage<?>) headers.get("simpConnectMessage");
        //simpConnectMessage 의 MessageHeader 를 추출
        MessageHeaders simpHeaders = Objects.requireNonNull(simpConnectMessage).getHeaders();

        //Map<String, List<String>>로 nativeHeader를 추출하여 리턴한다.
        return (Map<String, List<String>>) simpHeaders.get("nativeHeaders");
    }
}
