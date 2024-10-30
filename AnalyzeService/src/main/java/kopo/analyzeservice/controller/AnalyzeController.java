package kopo.analyzeservice.controller;

import kopo.analyzeservice.controller.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequestMapping(value = "/analyze/v1")
@RequiredArgsConstructor
@RestController
public class AnalyzeController {

    @PostMapping(value = "/analyzeDataPipeline")
    public ResponseEntity<CommonResponse> getAnalyzeData() throws Exception {

        // 분석 서비스에서 특정 토픽이 발행되면 토픽 구독
        // 영상 메타데이터를 엘라스틱서치에서 조회
        // 조회한 메타데이터로 영상 데이터 불러오기
        // 영상 데이터를 OpenFeign으로 모델에 전송 후 리턴값 받기
        // 리턴 값을 분석 결과로 만들어 엘라스틱 서치에 저장

        return null;
    }

    @PostMapping(value = "getAnalyzeList")
    public ResponseEntity<CommonResponse> getAnalyzeList() throws Exception {


        return null;
    }

    @PostMapping(value = "getAnalyzeInfo")
    public ResponseEntity<CommonResponse> getAnalyzeInfo() throws Exception {


        return null;
    }
}
