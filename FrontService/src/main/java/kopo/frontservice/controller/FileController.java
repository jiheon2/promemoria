package kopo.frontservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import kopo.frontservice.service.IFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/file")
@RestController
public class FileController {

    private final IFileService fileService;

    @PostMapping("/uploadVideoChat")
    public ResponseEntity<Map<String, String>> uploadVideoChat(
            @RequestPart(required = false) MultipartFile[] videoPart,
            @RequestPart(required = false) MultipartFile fullRecording,
            @RequestParam("userId") String userId) {

        log.info("파일 업로드 실행");
        log.info("userId: {}", userId);
        Map<String, String> response = new HashMap<>();

        if (fullRecording != null) {
            log.info("fullRecording: {}", fullRecording.getOriginalFilename());
            fileService.fullFileUploadOnServer(userId, fullRecording);
            response.put("fullRecordingStatus", "uploaded");
        } else {
            log.info("fullRecording No files to upload.");
            response.put("fullRecordingStatus", "no file");
        }

        if (videoPart != null && videoPart.length > 0) {
            log.info(videoPart[0].getOriginalFilename());
            fileService.filesUploadOnServer(userId, videoPart);
            response.put("videoPartStatus", "uploaded");
        } else {
            log.info("videoPart No files to upload.");
            response.put("videoPartStatus", "no files");
        }

        response.put("status", "success");
        return ResponseEntity.ok(response);
    }
}

