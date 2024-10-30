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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static com.amazonaws.services.s3.AmazonS3ClientBuilder.*;

@Slf4j
@Service
public class FileService implements IFileService {

    final String endPoint = "https://kr.object.ncloudstorage.com";
    final String regionName = "kr-standard";

    // S3 client
    final AmazonS3 s3 = standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endPoint, regionName))
            .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
            .build();

    final String bucketName = "pro-memoria24";
    final String userId = "bread";
    final LocalDateTime localDateTime = LocalDateTime.now();

    @Override
    public void fullFileUploadOnServer(MultipartFile fullRecording) {
        try {
            String filePath = userId + "_" + localDateTime + "-" + fullRecording.getOriginalFilename();
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
            uploadedMetadata.put("downloadFilePath", downloadFilePath);

            // Map을 JSON으로 변환
            ObjectMapper mapper = new ObjectMapper();
            String metadataToJson = mapper.writeValueAsString(uploadedMetadata);

            log.info("Metadata: " + metadataToJson);
        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
    }

    @Override
    public void filesUploadOnServer(MultipartFile[] videoPart) {

        try {
                for (MultipartFile uploadFile : videoPart) {
                    String filePath = userId + localDateTime + "-" + uploadFile.getOriginalFilename();
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

                    // Map을 JSON으로 변환
                    ObjectMapper mapper = new ObjectMapper();
                    String metadataToJson = mapper.writeValueAsString(uploadedMetadata);

                    log.info("Metadata: " + metadataToJson);
                }

        } catch (AmazonS3Exception e) {
            e.printStackTrace();
        } catch (SdkClientException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }

    }
}


