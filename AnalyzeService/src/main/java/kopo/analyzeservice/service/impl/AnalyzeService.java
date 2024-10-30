package kopo.analyzeservice.service.impl;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import kopo.analyzeservice.dto.AnalyzeDTO;
import kopo.analyzeservice.dto.MetaDTO;
import kopo.analyzeservice.dto.MsgDTO;
import kopo.analyzeservice.feign.ModelClient;
import kopo.analyzeservice.repository.AnalyzeRepository;
import kopo.analyzeservice.repository.MetaRepository;
import kopo.analyzeservice.repository.document.AnalyzeData;
import kopo.analyzeservice.service.AnalyzeInterface;
import kopo.analyzeservice.util.AnalyzeDataMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyzeService implements AnalyzeInterface {

    private final AnalyzeInterface analyzeInterface;
    private final AnalyzeRepository analyzeRepository;
    private final MetaRepository metaRepository;
    private final ModelClient modelClient;
    private final AmazonS3 s3;

//    @Value("${ncp.objectStorage.endPoint}")
//    private String endPoint;
//
//    @Value("${ncp.objectStorage.regionName}")
//    private String regionName;
//
//    @Value("${ncp.objectStorage.accessKey}")
//    private String accessKey;
//
//    @Value("${ncp.objectStorage.secretKey}")
//    private String secretKey;

    @Value("${ncp.objectStorage.bucketName}")
    private String bucketName;

//    // S3 client
//    final AmazonS3 amazonS3 = AmazonS3ClientBuilder.standard()
//            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, regionName))
//            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
//            .build();


    @Override
    public MsgDTO saveAnalyzeData(AnalyzeDTO analyzeDTO) {
        return null;
    }

    @Override
    public List<AnalyzeDTO> getAnalyzeList(String userId) {

        log.info("getAnalyzeList start : {}", this.getClass().getName());

        log.info("조회할 UserId : {}", userId);

        List<AnalyzeData> rList = analyzeRepository.findAllByUserId(userId);
        // rList를 Optional로 감싸고 변환을 진행하여 코드 간결화
        List<AnalyzeDTO> dtoList = Optional.ofNullable(rList)
                .map(AnalyzeDataMapper::toDTOList)
                .orElse(Collections.emptyList());

        // dtoList가 비어 있지 않을 때만 상세 로그를 남기도록 조건부 로깅
        if (!dtoList.isEmpty()) {
            logDtoListAsJson(dtoList); // JSON 형식으로 dtoList 출력
        } else {
            log.info("No analysis data found for UserId: {}", userId);
        }

        log.info("getAnalyzeList end : {}", this.getClass().getName());

        return dtoList;
    }

    @Override
    public List<AnalyzeDTO> getAnalyzeData(String userId, Date date) {

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
    public List<MetaDTO> getMetaList(String userId, Date date) {
        return List.of();
    }

    @Override
    public List<AnalyzeDTO> analyzeData(List<MetaDTO> metaList) {

        if (metaList == null || metaList.isEmpty()) {
            throw new IllegalArgumentException("metaList는 비어 있을 수 없습니다.");
        }

        return metaList.parallelStream()
                .map(metaDTO -> {
                    String objectName = metaDTO.objectName();
                    try {
                        MultipartFile file = downloadFileFromS3(bucketName, objectName);
                        return modelClient.analyzeData(file);
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
            return new MockMultipartFile("file", objectName, MediaType.APPLICATION_OCTET_STREAM_VALUE, content);
        }
    }
}
