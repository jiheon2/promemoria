package kopo.userservice.service;

import kopo.userservice.dto.FriendDTO;
import kopo.userservice.dto.UserDTO;

import java.util.List;

public interface FriendInterface {

    void insertMyFriend(FriendDTO pDTO) throws Exception;
    List<FriendDTO> getMyFriends(String userId) throws Exception;
    void deleteMyFriend(FriendDTO pDTO) throws Exception;
    List<UserDTO> findFriends(String friendId) throws Exception;
    List<UserDTO> recommendFriends(String userId) throws Exception;

}

