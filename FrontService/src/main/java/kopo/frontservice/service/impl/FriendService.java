package kopo.frontservice.service.impl;

import jakarta.transaction.Transactional;
import kopo.frontservice.dto.FriendDTO;
import kopo.frontservice.dto.UserDTO;
import kopo.frontservice.repository.IFriendRepository;
import kopo.frontservice.repository.IUserRepository;
import kopo.frontservice.repository.entity.FriendEntity;
import kopo.frontservice.repository.entity.FriendId;
import kopo.frontservice.repository.entity.UserEntity;
import kopo.frontservice.service.IFriendService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class FriendService implements IFriendService {

    private final IFriendRepository friendRepository;
    private final IUserRepository userRepository;

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
        int minAge = entity.getAge() - 5;
        int maxAge = entity.getAge() + 5;

        log.info("현재 유저의 나이 - 5 : " + minAge);
        log.info("현재 유저의 나이 + 5 : " + maxAge);

        List<UserEntity> result = userRepository.findByGenderAndAgeBetween(entity.getGender(), minAge, maxAge);

        List<UserDTO> dto = new ArrayList<>();

        for(int i = 0; i < result.size(); i++) {
            dto.add(UserDTO.builder().userId(result.get(i).getUserId()).userAge(result.get(i).getAge())
                    .userGender(result.get(i).getGender()).build());
        }

        return dto;
    }
}
