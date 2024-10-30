package kopo.frontservice.service;

import kopo.frontservice.dto.FriendDTO;
import kopo.frontservice.dto.UserDTO;

import java.util.List;

public interface IFriendService {

    void insertMyFriend(FriendDTO pDTO) throws Exception;
    List<FriendDTO> getMyFriends(String userId) throws Exception;
    void deleteMyFriend(FriendDTO pDTO) throws Exception;
    List<UserDTO> findFriends(String friendId) throws Exception;
    List<UserDTO> recommendFriends(String userId) throws Exception;
}
