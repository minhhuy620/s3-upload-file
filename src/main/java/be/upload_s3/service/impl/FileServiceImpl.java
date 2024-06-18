package be.upload_s3.service.impl;

import be.upload_s3.dtos.ApiResponse;
import be.upload_s3.dtos.ResponseFileS3;
import be.upload_s3.exception.FileException;
import be.upload_s3.service.FileService;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    @Value("${aws.bucket.name}")
    private String bucketName;

    private final AmazonS3 s3Client;

    @Override
    public String uploadFile(MultipartFile multipartFile, int userID) {
        // convert multipart file to a file
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        try{
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(multipartFile.getBytes());
        }catch(IOException exception){
            System.out.println("An IOException occurred. Here's the stack trace:");
            exception.printStackTrace();
        }
        String fileName = generateFileName(multipartFile, userID);

        // upload file
        PutObjectRequest request = new PutObjectRequest(bucketName, fileName, file);
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("plain/"+ FilenameUtils.getExtension(multipartFile.getOriginalFilename()));
        metadata.addUserMetadata("Title", "File Upload - " + fileName);
        metadata.setContentLength(file.length());
        metadata.addUserMetadata("userID", String.valueOf(userID));
        request.setMetadata(metadata);
        s3Client.putObject(request);
        file.delete();

        return fileName;
    }

    @Override
    public Object downloadFile(String fileName) throws IOException, FileException.FileDownloadException {
        if (bucketIsEmpty()) {
            throw new FileException.FileDownloadException("Requested bucket does not exist or is empty");
        }
        S3Object object = s3Client.getObject(bucketName, fileName);
        try (S3ObjectInputStream s3Object = object.getObjectContent()) {
            ByteArrayOutputStream fileOutputStream = new ByteArrayOutputStream();
                byte[] read_buf = new byte[1024];
                int read_len = 0;
                while ((read_len = s3Object.read(read_buf)) > 0) {
                    fileOutputStream.write(read_buf, 0, read_len);
                }
            byte[] fileBytes = fileOutputStream.toByteArray();
            ByteArrayResource resource = new ByteArrayResource(fileBytes);
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new FileException.FileDownloadException("Could not find the file!");
            }
        }
    }

    @Override
    public ApiResponse delete(String fileName) {
        if(fileName.isEmpty()){
            return new ApiResponse(false,"Could not find this file from s3",0, null);
        }
        s3Client.deleteObject(bucketName,fileName);
        return new ApiResponse(true, "Delete file" + fileName,1, null);
    }

    @Override
    public ApiResponse listFilesFromS3(int userID) {
        ListObjectsV2Request listObjectsRequest =
                new ListObjectsV2Request().withBucketName(bucketName).withPrefix(String.valueOf(userID));

        ListObjectsV2Result objects = s3Client.listObjectsV2(listObjectsRequest);
        if(objects == null){
            return new ApiResponse(false, "Don't have any files from bucket S3", 0, null);
        }
        return new ApiResponse(true, "Files have successfully", 1, new ResponseFileS3(objects.getObjectSummaries()));
    }

    private boolean bucketIsEmpty() {
        ListObjectsV2Result result = s3Client.listObjectsV2(this.bucketName);
        if (result == null){
            return false;
        }
        List<S3ObjectSummary> objects = result.getObjectSummaries();
        return objects.isEmpty();
    }

    private String generateFileName(MultipartFile multiPart, int userID) {
        return userID+"_"+multiPart.getOriginalFilename();
    }

}
