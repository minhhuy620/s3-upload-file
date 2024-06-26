package be.upload_s3.exception;

public class FileException {
    public static class FileDownloadException extends Throwable {
        public FileDownloadException(String message) {
            super(message);
        }
    }
    public static class SpringBootFileUploadException extends Exception{
        public SpringBootFileUploadException(String message) {
            super(message);
        }
    }
    public static class FileEmptyException extends SpringBootFileUploadException {
        public FileEmptyException(String message) {
            super(message);
        }
    }
}
