package com.setronica.unify.application.rest;

import com.setronica.unify.application.dto.JobStatisticsDto;
import com.setronica.unify.application.dto.JobStatusDto;
import com.setronica.unify.application.service.ImpexWebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartRequest;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("catalog/files")
public class ImpexController {
    public static final String TEXT_CSV = "text/csv";

    @Autowired
    private ImpexWebService service;

    @PostMapping(value = "upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> upload(MultipartRequest request) throws IOException {
        Map<String, MultipartFile> fileMap = request.getFileMap();
        if (!fileMap.isEmpty()) {
            for (Map.Entry<String, MultipartFile> entry : fileMap.entrySet()) {
                return new ResponseEntity<>(String.valueOf(service.uploadFile(entry.getValue())), HttpStatus.ACCEPTED);
            }
        }

        return new ResponseEntity<>("No file provided", HttpStatus.BAD_REQUEST);
    }

    @PostMapping(value = "download", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> download() throws IOException {
        return new ResponseEntity<>(String.valueOf(service.downloadFile()), HttpStatus.ACCEPTED);
    }

    @GetMapping(value = "status/{token}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JobStatusDto> getStatus(@PathVariable long token) {
        return new ResponseEntity<>(service.getStatus(token), HttpStatus.OK);
    }

    @GetMapping(value = "stats/{token}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JobStatisticsDto> getStats(@PathVariable long token) {
        return new ResponseEntity<>(service.getStatistics(token), HttpStatus.OK);
    }

    @GetMapping(value = "result/{token}", produces = TEXT_CSV)
    public ResponseEntity<Resource> getResult(@PathVariable long token) throws IOException {
        Resource resource = service.getResult(token);
        if (resource == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }

        HttpHeaders respHeaders = new HttpHeaders();
        respHeaders.setContentType(MediaType.parseMediaType(TEXT_CSV));
        respHeaders.setContentLength(resource.contentLength());
        respHeaders.setContentDisposition(ContentDisposition.formData()
                .name("attachment")
                .filename("catalog.csv", StandardCharsets.UTF_8)
                .build());

        return new ResponseEntity<>(resource, respHeaders, HttpStatus.OK);
    }

    @DeleteMapping("result/{token}")
    public ResponseEntity<Resource> deleteResult(@PathVariable long token) {
        service.deleteResult(token);
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}
