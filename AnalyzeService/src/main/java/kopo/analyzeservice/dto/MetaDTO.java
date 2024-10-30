package kopo.analyzeservice.dto;

import lombok.Builder;

import java.util.Date;

@Builder
public record MetaDTO(
        String bucketName,
        String downloadFilePath,
        String objectName,
        String userId,
        String date
) {
}
