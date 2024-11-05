package kopo.analyzeservice.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import kopo.analyzeservice.dto.AnalyzeDTO;
import kopo.analyzeservice.dto.MetaDTO;
import kopo.analyzeservice.dto.MsgDTO;
import kopo.analyzeservice.feign.ModelClient;
import kopo.analyzeservice.repository.AnalyzeRepository;
import kopo.analyzeservice.repository.MetaRepository;
import kopo.analyzeservice.repository.document.AnalyzeData;
import kopo.analyzeservice.repository.document.Metadata;
import kopo.analyzeservice.service.AnalyzeInterface;
import kopo.analyzeservice.util.AnalyzeDataMapper;
import kopo.analyzeservice.util.CustomMultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyzeService implements AnalyzeInterface {

    private final ElasticsearchOperations elasticsearchOperations;
    private final AnalyzeRepository analyzeRepository;
    private final MetaRepository metaRepository;
    private final ModelClient modelClient;
    private final AmazonS3 s3;

    @Value("${ncp.objectStorage.bucketName}")
    private String bucketName;

    @KafkaListener(
            topics = "analyze-data",
            groupId = "analyze-group",
            containerFactory = "analyzeKafkaListenerContainerFactory"
    )
    public void listenAndSaveAnalyzeData(String uploadIdentifier) {

        log.info("카프카에게 받은 메시지 : {}", uploadIdentifier);

        // kafkaObjectName을 사용하여 메타 데이터를 가져오고 분석 데이터를 처리합니다.
        List<MetaDTO> metaList = getMetaList(uploadIdentifier);
        List<AnalyzeDTO> analyzeList = analyzeData(metaList);

        log.info("저장 데이터 : {}", analyzeList);
        log.info("저장 데이터 : {}", metaList);

        // analyzeList 저장 로직 추가
        try {
            this.saveAnalyzeData(metaList, analyzeList); // 메타 정보를 함께 전달
            MsgDTO dto = MsgDTO.builder()
                    .result(1)
                    .msg("Analyze data saved successfully.")
                    .build();
            log.info("Analyze data saved successfully: {}", dto.msg());
        } catch (Exception e) {
            log.error("Error saving analyze data", e);
            MsgDTO dto = MsgDTO.builder()
                    .result(0)
                    .msg("Failed to save analyze data.")
                    .build();
            log.error("Failed to save analyze data: {}", dto.msg());
        }
    }

    @KafkaListener(
            topics = "meta-data",
            groupId = "meta-group",
            containerFactory = "metaKafkaListenerContainerFactory"
    )
    public void listenAndSaveMeta(Map<String, Object> uploadedMetadata) {

        log.info("카프카에게 받은 메세지 : {}", uploadedMetadata);

        try {
            Metadata metadata = Metadata.builder()
                    .uploadIdentifier((String) uploadedMetadata.get("uploadIdentifier"))
                    .objectName((String) uploadedMetadata.get("objectName"))
                    .bucketName((String) uploadedMetadata.get("bucketName"))
                    .downloadFilePath((String) uploadedMetadata.get("downloadFilePath"))
                    .userId((String) uploadedMetadata.get("userId"))
                    .build();

            metaRepository.save(metadata);
            log.info("메타데이터 저장 성공");
        } catch (Exception e) {
            log.error("Error saving metadata", e);
        }
    }

    @Override
    public List<AnalyzeData> getAnalyzeList(String userId) {
        log.info("userId: {}", userId);
        // 사용자 ID로 모든 분석 데이터 조회
        return analyzeRepository.findAllByUserId(userId);
    }

    @Override
    public List<AnalyzeDTO> getAnalyzeData(String userId, String date) {

        log.info("getAnalyzeData start : {}", this.getClass().getName());

        log.info("userId : {}, Date : {}", userId, date);

        List<AnalyzeData> rList = analyzeRepository.findAllByUserIdAndDate(userId, date);

        List<AnalyzeDTO> dtoList = Optional.ofNullable(rList)
                .map(AnalyzeDataMapper::toDTOList)
                .orElse(Collections.emptyList());

        // dtoList가 비어 있지 않을 때만 상세 로그를 남기도록 조건부 로깅
        if (!dtoList.isEmpty()) {
            logDtoListAsJson(dtoList); // JSON 형식으로 dtoList 출력
        } else {
            log.info("No analysis data found for UserId: {}, Date: {}", userId, date);
        }

        log.info("getAnalyzeData end : {}", this.getClass().getName());

        return dtoList;
    }

    @Override
    public List<MetaDTO> getMetaList(String uploadIdentifier) {

        log.info("getMetaList start : {}", this.getClass().getName());

        log.info("objectName : {}", uploadIdentifier);

        List<Metadata> eList = metaRepository.findAllByUploadIdentifier(uploadIdentifier);

        List<MetaDTO> metaList = convertList(eList);

        log.info("metaList : {}", metaList);

        log.info("getMetaList end : {}", this.getClass().getName());

        return metaList;
    }

    @Override
    public List<AnalyzeDTO> analyzeData(List<MetaDTO> metaList) {
        if (metaList == null || metaList.isEmpty()) {
            throw new IllegalArgumentException("metaList는 비어 있을 수 없습니다.");
        }

        return metaList.parallelStream()
                .map(metaDTO -> {
                    String objectName = metaDTO.objectName();
                    String downloadFilePath = metaDTO.downloadFilePath();
                    String analyzeDate = extractDateFromObjectName(objectName);
                    try {
                        MultipartFile file = downloadFileFromS3(bucketName, objectName);
                        AnalyzeDTO analyzeDTO = modelClient.analyzeData(file);
                        return analyzeDTO;
                    } catch (IOException e) {
                        log.error("파일 처리 중 오류 발생: {}", objectName, e);
                        throw new RuntimeException("파일 처리 중 오류가 발생했습니다: " + objectName, e);
                    }
                })
                .collect(Collectors.toList());
    }

    // JSON 형식으로 dtoList를 출력하는 메서드 추가
    private void logDtoListAsJson(List<AnalyzeDTO> dtoList) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            String json = objectMapper.writeValueAsString(dtoList);
            log.debug("DTO List in JSON format:\n{}", json); // DEBUG 레벨로 상세 로그
        } catch (Exception e) {
            log.error("Error while converting DTO list to JSON", e);
        }
    }

    private MultipartFile downloadFileFromS3(String bucketName, String objectName) throws IOException {
        log.info("S3에서 파일 다운로드 시작: {}", objectName);
        try (S3Object s3Object = s3.getObject(bucketName, objectName);
             S3ObjectInputStream s3ObjectInputStream = s3Object.getObjectContent()) {
            byte[] content = IOUtils.toByteArray(s3ObjectInputStream);
            return new CustomMultipartFile(content, "file", objectName, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        }
    }

    private void saveAnalyzeData(List<MetaDTO> metaList, List<AnalyzeDTO> analyzeList) {
        if (analyzeList == null || analyzeList.isEmpty()) {
            log.info("No analyze data to save.");
            return;
        }

        // 메타 필드와 분석 결과를 결합하여 AnalyzeData 엔티티 생성
        List<AnalyzeData> dataList = new ArrayList<>();
        for (int i = 0; i < metaList.size() && i < analyzeList.size(); i++) {
            MetaDTO metaDTO = metaList.get(i);
            AnalyzeDTO analyzeDTO = analyzeList.get(i);

            String analyzeDate = extractDateFromObjectName(metaDTO.objectName());

            AnalyzeData analyzeData = AnalyzeDataMapper.toDocument(
                    metaDTO.userId(),
                    analyzeDate,
                    metaDTO.downloadFilePath(),
                    metaDTO.objectName(),
                    analyzeDTO
            );

            dataList.add(analyzeData);
        }

        analyzeRepository.saveAll(dataList);
        log.info("Saved {} analyze data entries.", dataList.size());
    }

    private String extractDateFromObjectName(String objectName) {
        String analyzeDate = "";
        Pattern pattern = Pattern.compile("_(\\d{4}-\\d{2}-\\d{2}_\\d{2}-\\d{2}-\\d{2})-");
        Matcher matcher = pattern.matcher(objectName);
        if (matcher.find()) {
            analyzeDate = matcher.group(1);
        } else {
            throw new RuntimeException("objectName에서 날짜를 추출할 수 없습니다: " + objectName);
        }
        return analyzeDate;
    }

    public MetaDTO convertToMetaDTO(Metadata metadata) {
        return MetaDTO.builder()
                .bucketName(metadata.getBucketName())
                .downloadFilePath(metadata.getDownloadFilePath())
                .objectName(metadata.getObjectName())
                .userId(metadata.getUserId())
                .uploadIdentifier(metadata.getUploadIdentifier())
                .build();
    }

    public List<MetaDTO> convertList(List<Metadata> eList) {
        return eList.stream()
                .map(this::convertToMetaDTO)
                .collect(Collectors.toList());
    }

}
