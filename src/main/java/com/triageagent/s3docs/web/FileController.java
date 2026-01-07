package com.triageagent.s3docs.web;

import com.triageagent.s3docs.config.S3Properties;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;

@Validated
@RestController
@RequestMapping("/api/v1/files")
public class FileController {

    private static final String KEY_REGEX = "^[a-zA-Z0-9._-]{1,256}$";

    private final S3Client s3;
    private final S3Properties props;

    public FileController(S3Client s3, S3Properties props) {
        this.s3 = s3;
        this.props = props;
    }

    @GetMapping("/{key}")
    public ResponseEntity<byte[]> getFile(
            @PathVariable @Pattern(regexp = KEY_REGEX, message = "Invalid key") String key
    ) {
        String s3Key = props.normalizedPrefix() + key;

        try {
            HeadObjectResponse head = s3.headObject(b -> b.bucket(props.bucket()).key(s3Key));

            byte[] bytes;
            try (ResponseInputStream<GetObjectResponse> in =
                         s3.getObject(b -> b.bucket(props.bucket()).key(s3Key))) {
                bytes = in.readAllBytes();
            } catch (IOException e) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to read S3 object", e);
            }

            MediaType mt = (head.contentType() == null || head.contentType().isBlank())
                    ? MediaType.APPLICATION_OCTET_STREAM
                    : MediaType.parseMediaType(head.contentType());

            return ResponseEntity.ok()
                    .contentType(mt)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + key + "\"")
                    .body(bytes);

        } catch (NoSuchKeyException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found: " + key);
        } catch (S3Exception e) {
            throw new ResponseStatusException(HttpStatus.valueOf(e.statusCode()), e.awsErrorDetails().errorMessage(), e);
        }
    }
}
