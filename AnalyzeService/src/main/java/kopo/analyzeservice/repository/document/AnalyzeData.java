package kopo.analyzeservice.repository.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import kopo.analyzeservice.dto.AnalyzeDTO;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.Date;
import java.util.List;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Data
@Document(indexName = "analyze_index")
public class AnalyzeData {
    private Result eye;  // 눈
    private Result lip;  // 입술
    private Result tilt; // 기울기
    private String userId; // 회원 아이디
    private Date date; // 분석 날짜
    private String userName; // 회원 이름
    private String videoUrl; // 영상 URL
    private String finalStatus; // 최종 이상 여부

    @Builder
    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Result {
        private AnalyzeDTO.Status status;  // 상태
        private List<Accuracy> accuracy;   // 정확도
    }

    @Builder
    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Accuracy {
        private String accurate;     // 정확한 정도
        private String inaccurate;   // 정확하지 않은 정도
    }
}
