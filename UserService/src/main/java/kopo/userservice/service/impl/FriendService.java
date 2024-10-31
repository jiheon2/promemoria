package kopo.userservice.service.impl;

import kopo.userservice.dto.FriendDTO;
import kopo.userservice.dto.UserDTO;
import kopo.userservice.repository.FriendRepository;
import kopo.userservice.repository.UserRepository;
import kopo.userservice.repository.entity.FriendEntity;
import kopo.userservice.repository.entity.FriendId;
import kopo.userservice.repository.entity.UserEntity;
import kopo.userservice.service.FriendInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class FriendService implements FriendInterface {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    @Override
    public void insertMyFriend(FriendDTO pDTO) throws Exception {
        FriendId friendId = new FriendId(pDTO.userId(), pDTO.friendId());
        FriendEntity entity = FriendEntity.builder().id(friendId).build();
        friendRepository.save(entity);
    }


    @Override
    public List<FriendDTO> getMyFriends(String userId) throws Exception {

        List<FriendEntity> result = friendRepository.findByIdUserId(userId);
        List<FriendDTO> arr = new ArrayList<>();

        for(FriendEntity friendEntity : result) {
            arr.add(FriendDTO.builder().userId(friendEntity.getId().getUserId())
                    .friendId(friendEntity.getId().getFollowedUser())
                    .build());
        }

        log.info(arr.toString());

        return arr;
    }

    @Transactional
    @Override
    public void deleteMyFriend(FriendDTO pDTO) throws Exception {
        FriendId friendId = new FriendId(pDTO.userId(), pDTO.friendId());
//        FriendEntity entity = FriendEntity.builder().id(friendId).build();
        friendRepository.deleteById(friendId);
    }

    @Override
    public List<UserDTO> findFriends(String friendId) throws Exception {
        List<UserEntity> entity = userRepository.findByUserIdStartingWith(friendId);

        List<UserDTO> dto = new ArrayList<>();

        for(int i = 0; i < entity.size(); i++) {
            dto.add(UserDTO.builder().userId(entity.get(i).getUserId()).build());
        }

        return dto;
    }

    @Override
    public List<UserDTO> recommendFriends(String userId) throws Exception {
        UserEntity entity = userRepository.findByUserId(userId);
        int minAge = entity.getUserAge() - 5;
        int maxAge = entity.getUserAge() + 5;

        log.info("현재 유저의 나이 - 5 : " + minAge);
        log.info("현재 유저의 나이 + 5 : " + maxAge);

        List<UserEntity> result = userRepository.findByUserGenderAndUserAgeBetween(entity.getUserGender(), minAge, maxAge);

        List<UserDTO> dto = new ArrayList<>();

        for(int i = 0; i < result.size(); i++) {
            dto.add(UserDTO.builder().userId(result.get(i).getUserId()).userAge(result.get(i).getUserAge())
                    .userGender(result.get(i).getUserGender()).build());
        }

        return dto;
    }
}
