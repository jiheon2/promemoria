package kopo.userservice.controller;


import kopo.userservice.dto.FriendDTO;
import kopo.userservice.dto.UserDTO;
import kopo.userservice.service.impl.FriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"https://www.promemoriapm.kr", "https://user.promemoriapm.kr"},
        allowedHeaders = {"POST", "GET", "FEIGN"},
        allowCredentials = "true")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/friend/v1")
@RestController
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/insertMyFriend")
    public void insertMyFriend(@RequestHeader("X-User-Id") String userId,
                               @RequestParam String friendId) throws Exception {
        friendService.insertMyFriend(FriendDTO.builder().userId(userId).friendId(friendId).build());
    }

    @GetMapping("/getMyFriends")
    public List<FriendDTO> getMyFriends(@RequestHeader("X-User-Id") String userId) throws Exception {
        return friendService.getMyFriends(userId);
    }

    @PostMapping("/deleteMyFriend")
    public void deleteMyFriend(@RequestHeader("X-User-Id") String userId,
                               @RequestParam String friendId) throws Exception {
        friendService.deleteMyFriend(FriendDTO.builder().userId(userId).friendId(friendId).build());
    }

    @PostMapping("/findFriends")
    public List<UserDTO> findFriends(@RequestHeader("X-User-Id") String userId,
                                     @RequestParam String friendId) throws Exception {
        return friendService.findFriends(friendId);
    }

    @GetMapping("/recommendFriends")
    public List<UserDTO> recommendFriends(@RequestHeader("X-User-Id") String userId) throws Exception {
        return friendService.recommendFriends(userId);
    }

}
