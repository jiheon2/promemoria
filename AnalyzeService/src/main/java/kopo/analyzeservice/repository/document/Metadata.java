package kopo.analyzeservice.repository.document;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Data
@Document(indexName = "metadata_index")
public class Metadata {
    @Id
    private String id;
    private String bucketName;
    private String downloadFilePath;
    private String objectName;
    private String userId;
    private String uploadIdentifier;
}
