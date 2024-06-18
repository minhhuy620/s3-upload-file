package be.upload_s3.controller;

import be.upload_s3.dtos.ApiResponse;
import be.upload_s3.exception.FileException;
import be.upload_s3.service.FileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.validation.constraints.NotBlank;
import java.io.IOException;
import java.util.*;

@Tag(name = "File", description = "File management APIs")
@RestController
@Slf4j
@RequestMapping("/api/v1/file")
@Validated
public class FileToS3Controller {
    private final FileService fileService;
    @Autowired
    public FileToS3Controller(FileService fileUploadService) {
        this.fileService = fileUploadService;
    }

    @GetMapping("/files_s3")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ApiResponse> getFileFromS3(@RequestParam(value = "userID") int userID){
        ApiResponse res = fileService.listFilesFromS3(userID);
        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @PostMapping("/upload")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> uploadFile(@RequestParam(value = "file") MultipartFile multipartFile, @RequestParam(value="userID") int userID) throws FileException.FileEmptyException, IOException, FileException.FileDownloadException {
        if (multipartFile.isEmpty()){
            throw new FileException.FileEmptyException("File is empty. Cannot save an empty file");
        }
        boolean isValidFile = isValidFile(multipartFile);
        List<String> allowedFileExtensions = new ArrayList<>(Arrays.asList("xlsx","pdf", "txt", "csv", "png", "jpg", "jpeg", "xlsm","xls", "pptx", "docx", "doc"));

        if (isValidFile && allowedFileExtensions.contains(FilenameUtils.getExtension(multipartFile.getOriginalFilename()))){
            String fileName = fileService.uploadFile(multipartFile, userID);
            ApiResponse apiResponse = ApiResponse.builder()
                    .message("File uploaded successfully. File name =>" + fileName)
                    .success(true)
                    .errorCode(200)
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.OK);
        } else {
            ApiResponse apiResponse = ApiResponse.builder()
                    .message("Invalid File. File extension or File name is not supported")
                    .success(false)
                    .errorCode(400)
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/download")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<?> downloadFile(@RequestParam(value = "fileName")  @NotBlank String fileName) throws FileException.FileDownloadException, IOException {
        Object response = fileService.downloadFile(fileName);
        if (response != null){
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"").body(response);
        } else {
            ApiResponse apiResponse = ApiResponse.builder()
                    .message("File could not be downloaded")
                    .success(false)
                    .errorCode(400)
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/delete")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> delete(@RequestParam(value = "fileName") @NotBlank String fileName){
        ApiResponse apiResponse = fileService.delete(fileName);
        if (apiResponse.getErrorCode() == 0){
            apiResponse = ApiResponse.builder().message("File does not exist")
                    .errorCode(404).build();
            return new ResponseEntity<>(apiResponse.getMessage(), HttpStatus.NOT_FOUND);
        } else {
            apiResponse = ApiResponse.builder().message("File deleted!")
                    .errorCode(200).build();
            return new ResponseEntity<>(apiResponse.getMessage(), HttpStatus.OK);
        }
    }

    private boolean isValidFile(MultipartFile multipartFile){
        if (Objects.isNull(multipartFile.getOriginalFilename())){
            return false;
        }
        return !multipartFile.getOriginalFilename().trim().equals("");
    }
}
