package kopo.frontservice.controller;

import kopo.frontservice.service.IFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/file")
@RestController
public class FileController {

    private final IFileService fileService;

    @PostMapping("/uploadVideoChat")
    public void uploadVideoChat(@RequestPart(required = false) MultipartFile[] videoPart,
                                @RequestPart(required = false) MultipartFile fullRecording) {
        log.info("파일 업로드 실행");

        if (fullRecording != null) {
            log.info("fullRecording: {}", fullRecording.getOriginalFilename());
            fileService.fullFileUploadOnServer(fullRecording);
        } else {
            log.info("No files to upload.");
        }

        if (videoPart != null && videoPart.length > 0) {
            log.info(videoPart[0].getOriginalFilename());
            fileService.filesUploadOnServer(videoPart);
        } else {
            log.info("No files to upload.");
        }
    }
}

