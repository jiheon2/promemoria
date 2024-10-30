package kopo.frontservice.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class FriendId implements Serializable {
    @Column(name = "USER_ID")
    private String userId;

    @Column(name = "FOLLOWED_USER")
    private String followedUser;

    public FriendId() {}

    public FriendId(String userId, String followedUser) {
        this.userId = userId;
        this.followedUser = followedUser;
    }

    // 이거 없으면 각각의 값에 접근 못 함
    public String getUserId() {
        return userId;
    }

    public String getFollowedUser() {
        return followedUser;
    }
}


