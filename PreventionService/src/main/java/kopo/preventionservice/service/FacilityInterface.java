package kopo.preventionservice.service;

import kopo.preventionservice.dto.FacilityDTO;

import java.util.List;

public interface FacilityInterface {

    // 센터명으로 시설 조회
    List<FacilityDTO> getCenterName(String centerName);

    // 센터유형으로 시설 조회
    List<FacilityDTO> getCenterTypeList(String centerType);
}
