package kopo.frontservice.service;

import org.springframework.web.multipart.MultipartFile;

public interface IFileService {

    String accessKey = "gtiHaVgTnYHFkX6aRzLu";
    String secretKey = "eNVqarObDsvHvamFovccIAcUQFFcOzcsKg4CCrI1";

    void fullFileUploadOnServer(MultipartFile fullRecording);
    void filesUploadOnServer(MultipartFile[] videoPart);
}
