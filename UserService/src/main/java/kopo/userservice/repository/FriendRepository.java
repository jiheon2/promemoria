package kopo.userservice.repository;

import kopo.userservice.repository.entity.FriendEntity;
import kopo.userservice.repository.entity.FriendId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendRepository extends JpaRepository<FriendEntity, String> {
    List<FriendEntity> findByIdUserId(String userId) throws Exception;
    void deleteById(FriendId friendId) throws Exception;
}
