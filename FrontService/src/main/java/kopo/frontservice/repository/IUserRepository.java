package kopo.frontservice.repository;

import kopo.frontservice.repository.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IUserRepository extends JpaRepository<UserEntity, String> {

    UserEntity findByUserId(String userId) throws Exception;
    List<UserEntity> findByUserIdStartingWith(String friendId) throws Exception;
    List<UserEntity> findByGenderAndAgeBetween(String gender, int minAge, int maxAge);

}
