package kopo.analyzeservice.feign;

import kopo.analyzeservice.dto.AnalyzeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "modelClient", url = "https://38d6-112-76-111-22.ngrok-free.app")
public interface ModelClient {

    @PostMapping(value = "/evaluate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    AnalyzeDTO analyzeData(@RequestPart("file") MultipartFile file);

}
