package kopo.analyzeservice.util;

import kopo.analyzeservice.dto.AnalyzeDTO;
import kopo.analyzeservice.repository.document.AnalyzeData;

import java.util.List;
import java.util.stream.Collectors;

public class AnalyzeDataMapper {

    public static AnalyzeData toDocument(String userId, String date, String videoUrl, String objectName, AnalyzeDTO analyzeDTO) {
        return AnalyzeData.builder()
                .userId(userId)
                .date(date)
                .videoUrl(videoUrl)
                .objectName(objectName)
                .analyzeResult(analyzeDTO)
                .build();
    }

    public static AnalyzeDTO toDTO(AnalyzeData analyzeData) {
        return analyzeData.getAnalyzeResult();
    }

    public static List<AnalyzeDTO> toDTOList(List<AnalyzeData> analyzeDataList) {
        return analyzeDataList.stream()
                .map(AnalyzeDataMapper::toDTO)
                .collect(Collectors.toList());
    }
}
