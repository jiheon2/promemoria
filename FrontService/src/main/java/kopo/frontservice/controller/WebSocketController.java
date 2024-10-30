package kopo.frontservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
public class WebSocketController {

    @GetMapping("/createRoom")
    public void createRoom(@RequestHeader("X-User-Id") String creatorId) {
        // creatorId를 roomId로 사용하여 방 생성 로직 처리
        createChatRoom(creatorId);
        log.info("새로 생성된 방의 생성자: {}", creatorId);
    }

    // 채팅방을 만든 유저 데이터셋
    private Set<String> chatRoomUserSet = new HashSet<>();

    // 생성자의 ID를 roomId로 사용하는 예제 데이터셋 추가 메서드
    private void createChatRoom(String creatorId) {
        chatRoomUserSet.add(creatorId); // 중복 방지
    }

    @GetMapping("/getChatRoomList")
    public List<String> getChatRoomList() {
        return new ArrayList<>(chatRoomUserSet);
    }

}
