package kopo.userservice.repository.entity;

import jakarta.persistence.Cacheable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@NoArgsConstructor(force = true) // final 필드가 있는 경우 초기화 못 하기 때문에 컴파일 에러 발생함 그래서 추가하는 것(기본생성자 + final 필드까지 초기화)
@AllArgsConstructor
@Table(name="FRIEND")
@DynamicInsert
@DynamicUpdate
@Builder
@Cacheable
@ToString
@Entity
public class FriendEntity {

    @EmbeddedId
    private FriendId id;

}
