package kopo.analyzeservice.controller;

import kopo.analyzeservice.controller.response.CommonResponse;
import kopo.analyzeservice.service.AnalyzeInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = {"http://localhost:12000"},
        allowedHeaders = {"POST", "GET", "FEIGN"},
        allowCredentials = "true")
@Slf4j
@RequestMapping(value = "/analyze/v1")
@RequiredArgsConstructor
@RestController
public class AnalyzeController {

    private final AnalyzeInterface analyzeInterface;

    @PostMapping(value = "getAnalyzeList")
    public ResponseEntity<CommonResponse> getAnalyzeList(@RequestParam("userId") String userId) throws Exception {
        log.info("getAnalyzeList 실행 : {}", this.getClass().getName());
        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), analyzeInterface.getAnalyzeList(userId)));
    }

    @PostMapping(value = "getAnalyzeInfo")
    public ResponseEntity<CommonResponse> getAnalyzeInfo(@RequestParam("userId") String userId,
                                                         @RequestParam("date") String date) throws Exception {
        log.info("getAnalyzeInfo 실행 : {}", this.getClass().getName());
        return ResponseEntity.ok(CommonResponse.of(HttpStatus.OK, HttpStatus.OK.series().name(), analyzeInterface.getAnalyzeData(userId, date)));
    }
}
