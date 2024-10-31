package kopo.userservice.repository;


import kopo.userservice.repository.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {

    // 회원 정보 조회
    UserEntity findByUserId(String userId);

    // 아이디 찾기
    UserEntity findByUserNameAndUserEmail(String userName, String userEmail);

    // 비밀번호 찾기
    UserEntity findByUserIdAndUserEmail(String userId, String userEmail);

    // 아이디 중복 체크
    boolean existsByUserId(String userId);

    // 이메일 중복 체크
    boolean existsByUserEmail(String userEmail);

    // 회원 탈퇴
    void deleteByUserId(String userId);

    List<UserEntity> findByUserIdStartingWith(String friendId) throws Exception;
    List<UserEntity> findByUserGenderAndUserAgeBetween(String gender, int minAge, int maxAge);
}
