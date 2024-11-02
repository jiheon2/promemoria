package kopo.analyzeservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record MetaDTO(
        String bucketName,
        String downloadFilePath,
        String objectName,
        String userId,
        String uploadIdentifier
) {
}
