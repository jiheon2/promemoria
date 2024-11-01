package kopo.frontservice.controller;

import jakarta.servlet.http.HttpSession;
import kopo.frontservice.chat.ChatHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping(value = "/video")
public class VideoController {

    // 화상 채팅 입장
    @GetMapping("/createRoom")
    public Map<String, String> createRoom(@RequestParam("roomName") String roomName, @RequestParam("userName") String userName) {
        log.info(this.getClass().getName() + "[controller] createRoom Start!");

        log.info("채팅방 이름(개설자 아이디) : {}", roomName);
        log.info("참여자 이름 : {}", userName);

        log.info(this.getClass().getName() + "[controller] createRoom End!");

        return Map.of(
                "roomName", roomName
        );
    }

    @RequestMapping(value = "/getChatRoomList")
    public Set<String> getChatRoomList() {

        log.info(this.getClass().getName() + ".roomList Start!");

        log.info("현재 열려있는 방 : " + ChatHandler.roomInfo.keySet());

        log.info(this.getClass().getName() + ".roomList Ends!");

        return ChatHandler.roomInfo.keySet();
    }
//
//    // 채팅방을 만든 유저 데이터셋
//    private Set<String> chatRoomUserSet = new HashSet<>();
//
//    // 생성자의 ID를 roomId로 사용하는 예제 데이터셋 추가 메서드
//    private void createChatRoom(String creatorId) {
//        chatRoomUserSet.add(creatorId); // 중복 방지
//    }
//
//    @GetMapping("/getChatRoomList")
//    public List<String> getChatRoomList() {
//        return new ArrayList<>(chatRoomUserSet);
//    }

}
