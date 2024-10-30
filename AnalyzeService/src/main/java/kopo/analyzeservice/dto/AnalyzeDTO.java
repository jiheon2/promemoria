package kopo.analyzeservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;
import java.util.List;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record AnalyzeDTO(
        Result eye, // 눈
        Result lip, // 입술
        Result tilt, // 기울기
        String userId, // 회원 아이디
        String name, // 회원 이름
        String date, // 분석 날짜
        String videoUrl, // 영상 원본 URL
        String finalStatus // 최종 이상 여부
) {
    @Builder
    @Getter
    public record Result(
            Status status, // 상태
            List<Accuracy> accuracy // 정확도
    ) {
    }
    @Builder
    @Getter
    public record Accuracy(
            String accurate, // 정확한 정도
            String inaccurate // 정확하지 않은 정도
    ) {
    }

    public enum Status {
        NORMAL, // 정상(0)
        ABNORMAL // 비정상(1)
    }
}
