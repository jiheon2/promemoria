package kopo.frontservice.repository;

import kopo.frontservice.repository.entity.FriendEntity;
import kopo.frontservice.repository.entity.FriendId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IFriendRepository extends JpaRepository<FriendEntity, String> {

    List<FriendEntity> findByIdUserId(String userId) throws Exception;
    void deleteById(FriendId friendId) throws Exception;
}
