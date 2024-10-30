package kopo.analyzeservice.repository;

import kopo.analyzeservice.repository.document.AnalyzeData;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface AnalyzeRepository extends ElasticsearchRepository<AnalyzeData, String> {

    // UserId를 기준으로 모든 분석 결과를 조회
    List<AnalyzeData> findAllByUserId(String userId);

    // UserId와 날짜를 기준으로 분석 결과 상세 조회
    List<AnalyzeData> findAllByUserIdAndDate(String userId, Date date);
}
