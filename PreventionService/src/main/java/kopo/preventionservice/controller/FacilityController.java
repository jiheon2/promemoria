package kopo.preventionservice.controller;

import kopo.preventionservice.controller.response.CommonResponse;
import kopo.preventionservice.dto.FacilityDTO;
import kopo.preventionservice.service.FacilityInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"https://www.promemoriapm.kr", "https://prevention.promemoriapm.kr"},
        allowedHeaders =  "*",
        methods = {RequestMethod.GET, RequestMethod.OPTIONS, RequestMethod.POST},
        allowCredentials = "true")
@Slf4j
@RequestMapping(value = "/facility/v1")
@RequiredArgsConstructor
@RestController
public class FacilityController {

    private final FacilityInterface facilityInterface;

    @PostMapping(value = "getCenterName")
    public ResponseEntity<CommonResponse> getCenterName(@RequestParam("centerName") String centerName) {
        log.info("getCenterName 실행 : {}", this.getClass().getName());

        List<FacilityDTO> responses = facilityInterface.getCenterName(centerName);

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), responses));
    }

    @PostMapping(value = "getCenterTypeList")
    public ResponseEntity<CommonResponse> getCenterTypeList(@RequestParam("centerType") String centerType) {
        log.info("getCenterTypeList 실행 : {}", this.getClass().getName());

        List<FacilityDTO> responses = facilityInterface.getCenterTypeList(centerType);

        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), responses));
    }
}
