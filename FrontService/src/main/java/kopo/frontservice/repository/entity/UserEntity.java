package kopo.frontservice.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.annotation.Id;

@Getter
@NoArgsConstructor(force = true) // final 필드가 있는 경우 초기화 못 하기 때문에 컴파일 에러 발생함 그래서 추가하는 것(기본생성자 + final 필드까지 초기화)
@AllArgsConstructor
@Table(name="USER_INFO")
@DynamicInsert
@DynamicUpdate
@Builder
@Cacheable
@ToString
@Entity
public class UserEntity {

    @Id // PK 컬럼으로 설정
    @Column(name = "USER_ID", length = 100, nullable = false)
    private String userId;

    @NonNull
    @Column(name = "USER_NAME", length = 100, nullable = false)
    private String userName;

    @NonNull
    @Column(name = "EMAIL", length = 500, nullable = false)
    private String email;

    @NonNull
    @Column(name = "USER_AGE", nullable = false)
    private int age;

    @NonNull
    @Column(name = "USER_GENDER", length = 30, nullable = false)
    private String gender;

}
