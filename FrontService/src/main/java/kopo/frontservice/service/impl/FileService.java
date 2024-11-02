package kopo.frontservice.service.impl;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AccessControlList;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.util.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import kopo.frontservice.service.IFileService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.amazonaws.services.s3.AmazonS3ClientBuilder.*;

@Slf4j
@Service
public class FileService implements IFileService {

    final String endPoint = "https://kr.object.ncloudstorage.com";
    final String regionName = "kr-standard";

    private final KafkaTemplate<String, Object> metaTemplate;
    private final KafkaTemplate<String, String> analyzeTemplate;
    private static final String ANALYZE_TOPIC = "analyze-data";
    private static final String METADATA_TOPIC = "meta-data";

    @Autowired
    public FileService (
            @Qualifier("metaTemplate") KafkaTemplate<String, Object> metaTemplate,
            @Qualifier("analyzeTemplate") KafkaTemplate<String, String> analyzeTemplate) {
        this.metaTemplate = metaTemplate;
        this.analyzeTemplate = analyzeTemplate;
    }

    // S3 client
    final AmazonS3 s3 = standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, regionName))
            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
            .build();

    final String bucketName = "pro-memoria24";
    final LocalDateTime localDateTime = LocalDateTime.now();


    @Override
    public void fullFileUploadOnServer(String userId, MultipartFile fullRecording) {
        String uploadIdentifier = UUID.randomUUID().toString();
        log.info("생성된 업로드 식별자 : {}", uploadIdentifier);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String dateTime = localDateTime.format(formatter);
        try {
            String filePath = userId + "_" + dateTime + "-" + fullRecording.getOriginalFilename();
            log.info("생성된 파일명 : " + filePath);


            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(fullRecording.getSize());
            metadata.setContentType(fullRecording.getContentType());

            // MultipartFile의 InputStream을 읽어서 byte 배열로 변환
            byte[] fileContent = IOUtils.toByteArray(fullRecording.getInputStream());

            // byte 배열을 사용하여 S3에 업로드
            s3.putObject(bucketName, filePath, new ByteArrayInputStream(fileContent), metadata);

            // 기존 ACL 가져오기
            AccessControlList acl = s3.getObjectAcl(bucketName, filePath);

            // 읽기 권한 추가
            s3.setObjectAcl(bucketName, filePath, CannedAccessControlList.PublicRead);

            // 업로드 후 ObjectMetadata 가져오기
            ObjectMetadata uploadedFileMetadata = s3.getObjectMetadata(bucketName, filePath);

            // 파일 다운 경로
            String downloadFilePath = "https://kr.object.ncloudstorage.com/" + bucketName + "/" + filePath;

            // 메타데이터를 Map에 담기
            Map<String, Object> uploadedMetadata = new HashMap<>();
            uploadedMetadata.put("bucketName", bucketName);
            uploadedMetadata.put("objectName", filePath);
            uploadedMetadata.put("userId", userId);
            uploadedMetadata.put("downloadFilePath", downloadFilePath);
            uploadedMetadata.put("uploadIdentifier", uploadIdentifier);

            // Map을 JSON으로 변환
            ObjectMapper mapper = new ObjectMapper();
            String metadataToJson = mapper.writeValueAsString(uploadedMetadata);

            log.info("Metadata: " + metadataToJson);

            // 메타데이터 저장 토픽 발행
            log.info("메타데이터 저장 토픽 발행");
            metaTemplate.send(METADATA_TOPIC, uploadedMetadata);
            log.info("보낸 카프카 토픽 : {}", METADATA_TOPIC);
            log.info("보낸 카프카 데이터 : {}", uploadedMetadata);
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new IllegalArgumentException();
        } finally {
            // 데이터 분석 요청
            log.info("데이터 분석 요청 토픽 발행");
            analyzeTemplate.send(ANALYZE_TOPIC, uploadIdentifier);
            log.info("보낸 카프카 토픽 : {}", ANALYZE_TOPIC);
            log.info("보낸 카프카 데이터 : {}", uploadIdentifier);
        }
    }

    @Override
    public void filesUploadOnServer(String userId, MultipartFile[] videoPart) {

        log.info("filesUploadOnServer Start : {}", this.getClass().getName());

        String uploadIdentifier = UUID.randomUUID().toString();
        log.info("생성된 업로드 식별자 : {}", uploadIdentifier);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String dateTime = localDateTime.format(formatter);

        try {
            for (MultipartFile uploadFile : videoPart) {
                String filePath = userId + dateTime + "-" + uploadFile.getOriginalFilename();
                log.info("생성된 파일명 : " + filePath);

                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(uploadFile.getSize());
                metadata.setContentType(uploadFile.getContentType());

                // MultipartFile의 InputStream을 읽어서 byte 배열로 변환
                byte[] fileContent = IOUtils.toByteArray(uploadFile.getInputStream());

                // byte 배열을 사용하여 S3에 업로드
                s3.putObject(bucketName, filePath, new ByteArrayInputStream(fileContent), metadata);

                // 기존 ACL 가져오기
                AccessControlList acl = s3.getObjectAcl(bucketName, filePath);

                // 읽기 권한 추가
                s3.setObjectAcl(bucketName, filePath, CannedAccessControlList.PublicRead);

                // 파일 다운 경로
                String downloadFilePath = "https://kr.object.ncloudstorage.com/" + bucketName + "/" + filePath;

                // 메타데이터를 Map에 담기
                Map<String, Object> uploadedMetadata = new HashMap<>();
                uploadedMetadata.put("bucketName", bucketName);
                uploadedMetadata.put("objectName", filePath);
                uploadedMetadata.put("downloadFilePath", downloadFilePath);
                uploadedMetadata.put("userId", userId);
                uploadedMetadata.put("uploadIdentifier", uploadIdentifier);

                // Map을 JSON으로 변환
                ObjectMapper mapper = new ObjectMapper();
                String metadataToJson = mapper.writeValueAsString(uploadedMetadata);
                log.info("Metadata: " + metadataToJson);

                // 메타데이터 저장 토픽 발행
                log.info("메타데이터 저장 토픽 발행");
                metaTemplate.send(METADATA_TOPIC, uploadedMetadata);
                log.info("보낸 카프카 토픽 : {}", METADATA_TOPIC);
                log.info("보낸 카프카 데이터 : {}", uploadedMetadata);

            }
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new IllegalArgumentException();
        } finally {
            // 데이터 분석 요청
            log.info("데이터 분석 요청 토픽 발행");
            analyzeTemplate.send(ANALYZE_TOPIC, uploadIdentifier);
            log.info("보낸 카프카 토픽 : {}", ANALYZE_TOPIC);
            log.info("보낸 카프카 데이터 : {}", uploadIdentifier);
        }
    }
}


