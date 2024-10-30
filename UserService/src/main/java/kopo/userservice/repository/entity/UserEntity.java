package kopo.userservice.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_info")
@DynamicUpdate
@DynamicInsert
@Entity
public class UserEntity {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_pw")
    private String userPw;

    @Column(name = "user_age")
    private String userAge;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "user_gender")
    private String userGender;

    @Column(name = "post_number")
    private String postNumber;

    @Column(name = "user_address1")
    private String userAddress1;

    @Column(name = "user_address2")
    private String userAddress2;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "user_email")
    private String userEmail;

    @Column(name = "is_kakao")
    private Boolean isKakao;

    @Column(name = "roles")
    private String roles;
}
