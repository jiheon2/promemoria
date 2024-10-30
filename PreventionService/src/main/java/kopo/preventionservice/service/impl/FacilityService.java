package kopo.preventionservice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.preventionservice.dto.FacilityDTO;
import kopo.preventionservice.repository.FacilityRepository;
import kopo.preventionservice.repository.entity.FacilityEntity;
import kopo.preventionservice.service.FacilityInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class FacilityService implements FacilityInterface {

    private final FacilityRepository facilityRepository;

    @Override
    public List<FacilityDTO> getCenterName(String centerName) {

        log.info("센터명으로 시설 조회 실행 : {}", this.getClass().getName());

        log.info("centerName : {}", centerName);

        // 센터명으로 시설 조회
        List<FacilityEntity> rList = facilityRepository.findByCenterName(centerName);

        // Entity to DTO
        List<FacilityDTO> fList = new ObjectMapper().convertValue(rList,
                new TypeReference<List<FacilityDTO>>() {
                });

        return fList;
    }

    @Override
    public List<FacilityDTO> getCenterTypeList(String centerType) {

        log.info("센터유형으로 시설 조회 실행 : {}", this.getClass().getName());

        log.info("centerType : {}", centerType);

        // 센터명으로 시설 조회
        List<FacilityEntity> rList = facilityRepository.findByCenterType(centerType);

        // Entity to DTO
        List<FacilityDTO> fList = new ObjectMapper().convertValue(rList,
                new TypeReference<List<FacilityDTO>>() {
                });

        return fList;
    }
}

