package kopo.preventionservice.repository.entity;

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
@Table(name = "facility")
@DynamicUpdate
@DynamicInsert
@Entity
public class FacilityEntity {

    @Id
    @Column(name = "SEQ")
    private String seq;

    @Column(name = "CENTER_NAME")
    private String centerName;

    @Column(name = "CENTER_TYPE")
    private String centerType;

    @Column(name = "ADDR")
    private String addr;

    @Column(name = "LATITUDE")
    private String latitude;

    @Column(name = "LONGITUDE")
    private String longitude;

    @Column(name = "TASK_CONTENT")
    private String taskContent;

    @Column(name = "CONTACT_TEL")
    private String contactTel;
}
