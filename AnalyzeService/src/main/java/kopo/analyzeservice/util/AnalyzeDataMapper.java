package kopo.analyzeservice.util;

import kopo.analyzeservice.dto.AnalyzeDTO;
import kopo.analyzeservice.repository.document.AnalyzeData;

import java.util.List;
import java.util.stream.Collectors;

public class AnalyzeDataMapper {

    // AnalyzeData를 AnalyzeDTO로 변환하는 메서드
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
                .objectName(data.getObjectName())
                .build();
    }

    // AnalyzeData 리스트를 AnalyzeDTO 리스트로 변환하는 메서드
    public static List<AnalyzeDTO> toDTOList(List<AnalyzeData> dataList) {
        if (dataList == null) {
            return null;
        }

        return dataList.stream()
                .map(AnalyzeDataMapper::toDTO)
                .collect(Collectors.toList());
    }

    // AnalyzeData.Result를 AnalyzeDTO.Result로 변환하는 메서드
    private static AnalyzeDTO.Result toResultDTO(AnalyzeData.Result result) {
        if (result == null) {
            return null;
        }

        return AnalyzeDTO.Result.builder()
                .status(result.getStatus())
                .accuracy(toAccuracyDTOList(result.getAccuracy()))
                .build();
    }

    // AnalyzeData.Accuracy 리스트를 AnalyzeDTO.Accuracy 리스트로 변환하는 메서드
    private static List<AnalyzeDTO.Accuracy> toAccuracyDTOList(List<AnalyzeData.Accuracy> accuracies) {
        if (accuracies == null) {
            return null;
        }

        return accuracies.stream()
                .map(AnalyzeDataMapper::toAccuracyDTO)
                .collect(Collectors.toList());
    }

    // AnalyzeData.Accuracy를 AnalyzeDTO.Accuracy로 변환하는 메서드
    private static AnalyzeDTO.Accuracy toAccuracyDTO(AnalyzeData.Accuracy accuracy) {
        if (accuracy == null) {
            return null;
        }

        return AnalyzeDTO.Accuracy.builder()
                .accurate(accuracy.getAccurate())
                .inaccurate(accuracy.getInaccurate())
                .build();
    }

    // AnalyzeDTO를 AnalyzeData로 변환하는 메서드
    public static AnalyzeData toDocument(AnalyzeDTO dto) {
        if (dto == null) {
            return null;
        }

        return AnalyzeData.builder()
                .eye(mapResult(dto.eye()))
                .lip(mapResult(dto.lip()))
                .tilt(mapResult(dto.tilt()))
                .userId(dto.userId())
                .date(dto.date())
                .userName(dto.name())
                .videoUrl(dto.videoUrl())
                .objectName(dto.objectName())
                .build();
    }

    // AnalyzeDTO.Result를 AnalyzeData.Result로 변환하는 메서드
    private static AnalyzeData.Result mapResult(AnalyzeDTO.Result dtoResult) {
        if (dtoResult == null) {
            return null;
        }

        return AnalyzeData.Result.builder()
                .status(dtoResult.status())
                .accuracy(mapAccuracyList(dtoResult.accuracy()))
                .build();
    }

    // AnalyzeDTO.Accuracy 리스트를 AnalyzeData.Accuracy 리스트로 변환하는 메서드
    private static List<AnalyzeData.Accuracy> mapAccuracyList(List<AnalyzeDTO.Accuracy> dtoAccuracyList) {
        if (dtoAccuracyList == null) {
            return null;
        }

        return dtoAccuracyList.stream()
                .map(AnalyzeDataMapper::mapAccuracy)
                .collect(Collectors.toList());
    }

    // AnalyzeDTO.Accuracy를 AnalyzeData.Accuracy로 변환하는 메서드
    private static AnalyzeData.Accuracy mapAccuracy(AnalyzeDTO.Accuracy dtoAccuracy) {
        if (dtoAccuracy == null) {
            return null;
        }

        return AnalyzeData.Accuracy.builder()
                .accurate(dtoAccuracy.accurate())
                .inaccurate(dtoAccuracy.inaccurate())
                .build();
    }
}
