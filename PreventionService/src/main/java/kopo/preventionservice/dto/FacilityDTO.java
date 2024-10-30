package kopo.preventionservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record FacilityDTO(
        String seq,         // 순번
        String centerName,  // 센터명
        String centerType,  // 센터유형
        String addr,        // 주소
        String latitude,    // 위도
        String longitude,   // 경도
        String taskContent, // 업무내용
        String contactTel   // 운영기관전화번호
) {}
