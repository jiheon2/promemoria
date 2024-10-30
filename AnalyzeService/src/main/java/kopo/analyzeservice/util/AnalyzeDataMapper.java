package kopo.analyzeservice.util;

import kopo.analyzeservice.dto.AnalyzeDTO;
import kopo.analyzeservice.repository.document.AnalyzeData;

import java.util.List;
import java.util.stream.Collectors;

public class AnalyzeDataMapper {

    public static AnalyzeDTO toDTO(AnalyzeData data) {
        if (data == null) {
            return null;
        }

        return AnalyzeDTO.builder()
                .eye(toResultDTO(data.getEye()))
                .lip(toResultDTO(data.getLip()))
                .tilt(toResultDTO(data.getTilt()))
                .userId(data.getUserId())
                .name(data.getUserName())
                .date(data.getDate())
                .videoUrl(data.getVideoUrl())
                .finalStatus(data.getFinalStatus())
                .build();
    }

    public static List<AnalyzeDTO> toDTOList(List<AnalyzeData> dataList) {
        if (dataList == null) {
            return null;
        }

        return dataList.stream()
                .map(AnalyzeDataMapper::toDTO)
                .collect(Collectors.toList());
    }


    private static AnalyzeDTO.Result toResultDTO(AnalyzeData.Result result) {
        if (result == null) {
            return null;
        }

        return AnalyzeDTO.Result.builder()
                .status(result.getStatus())
                .accuracy(toAccuracyDTOList(result.getAccuracy()))
                .build();
    }

    private static List<AnalyzeDTO.Accuracy> toAccuracyDTOList(List<AnalyzeData.Accuracy> accuracies) {
        if (accuracies == null) {
            return null;
        }

        return accuracies.stream()
                .map(AnalyzeDataMapper::toAccuracyDTO)
                .collect(Collectors.toList());
    }

    private static AnalyzeDTO.Accuracy toAccuracyDTO(AnalyzeData.Accuracy accuracy) {
        if (accuracy == null) {
            return null;
        }

        return AnalyzeDTO.Accuracy.builder()
                .accurate(accuracy.getAccurate())
                .inaccurate(accuracy.getInaccurate())
                .build();
    }
}
