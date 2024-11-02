package kopo.analyzeservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

// 최상위 AnalyzeDTO 클래스
@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record AnalyzeDTO(
        Results results // 결과
) {
    // Results 레코드: eye, lip, tilt 필드 포함
    @Builder
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public static record Results(
            Prediction eye, // 눈
            Prediction lip, // 입술
            Prediction tilt // 기울기
    ) {}

    // Prediction 레코드: predicted_class과 probabilities 필드 포함
    @Builder
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    public static record Prediction(
            @JsonProperty("predicted_class") int predictedClass, // 예측된 클래스
            List<List<Double>> probabilities // 확률 배열
    ) {}
}
