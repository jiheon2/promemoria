package kopo.analyzeservice.service;

import kopo.analyzeservice.dto.AnalyzeDTO;
import kopo.analyzeservice.dto.MetaDTO;
import kopo.analyzeservice.dto.MsgDTO;

import java.util.Date;
import java.util.List;

public interface AnalyzeInterface {

    // Elasticsearch에 분석 결과 저장
    MsgDTO saveAnalyzeData(String kafkaObjectName);

    // Elasticsearch에서 분석 데이터 리스트 조회
    List<AnalyzeDTO> getAnalyzeList(String userId);

    // Elasticsearch에서 분석 데이터 상세 조회
    List<AnalyzeDTO> getAnalyzeData(String userId, Date date);

    // Elasticsearch에서 메타데이터 조회하기
    List<MetaDTO> getMetaList(String objectName);

    // OpenFeign을 사용하여 AI 모델에 영상 데이터 분석 요청
    List<AnalyzeDTO> analyzeData(List<MetaDTO> metaList);
}
