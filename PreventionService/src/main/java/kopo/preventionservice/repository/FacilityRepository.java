package kopo.preventionservice.repository;


import kopo.preventionservice.repository.entity.FacilityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FacilityRepository extends JpaRepository<FacilityEntity, Long> {

    // 센터명으로 시설 조회
    @Query("SELECT f FROM FacilityEntity f WHERE f.centerName LIKE %:centerName%")
    List<FacilityEntity> findByCenterName(String centerName);

    // 센터유형으로 시설 조회
    List<FacilityEntity> findByCenterType(String centerType);
}
