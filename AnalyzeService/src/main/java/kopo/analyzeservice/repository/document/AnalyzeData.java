package kopo.analyzeservice.repository.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import kopo.analyzeservice.dto.AnalyzeDTO;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Document(indexName = "analyze_index")
public class AnalyzeData {

    @Id
    private String id;

    private String userId;            // 회원 아이디
    private String date;              // 분석 날짜
    private String videoUrl;          // 영상 URL
    private String objectName;        // 오브젝트 네임
    private AnalyzeDTO analyzeResult; // 분석 결과

}
