package com.triageagent.s3docs.s3;

import com.triageagent.s3docs.config.S3Properties;
import com.triageagent.s3docs.web.NotFoundException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.time.Instant;
import java.util.List;

@Repository
public class S3DocumentRepository {

    public record DocSummary(String key, Instant lastModified, String eTag) {}
    public record Page(List<DocSummary> items, String nextContinuationToken) {}

    private final S3Client s3;
    private final S3Properties props;

    public S3DocumentRepository(S3Client s3, S3Properties props) {
        this.s3 = s3;
        this.props = props;
    }

    public String putJson(String key, byte[] jsonBytes) {
        String s3Key = toS3Key(key);

        PutObjectRequest req = PutObjectRequest.builder()
                .bucket(props.bucket())
                .key(s3Key)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .serverSideEncryption(ServerSideEncryption.AES256) // SSE-S3
                .build();

        PutObjectResponse resp = s3.putObject(req, RequestBody.fromBytes(jsonBytes));
        return stripQuotes(resp.eTag());
    }

    public byte[] getJson(String key) {
        String s3Key = toS3Key(key);

        try {
            GetObjectRequest req = GetObjectRequest.builder()
                    .bucket(props.bucket())
                    .key(s3Key)
                    .build();

            ResponseBytes<GetObjectResponse> bytes = s3.getObjectAsBytes(req);
            return bytes.asByteArray();
        } catch (NoSuchKeyException e) {
            throw new NotFoundException("Document not found: " + key);
        } catch (S3Exception e) {
            if (e.statusCode() == 404) throw new NotFoundException("Document not found: " + key);
            throw e;
        }
    }

    public void delete(String key) {
        String s3Key = toS3Key(key);

        DeleteObjectRequest req = DeleteObjectRequest.builder()
                .bucket(props.bucket())
                .key(s3Key)
                .build();

        s3.deleteObject(req);
    }

    public Page list(String startsWith, Integer maxKeys, String continuationToken) {
        String prefix = props.normalizedPrefix();
        String effectivePrefix = (startsWith == null || startsWith.isBlank())
                ? prefix
                : prefix + startsWith;

        ListObjectsV2Request req = ListObjectsV2Request.builder()
                .bucket(props.bucket())
                .prefix(effectivePrefix)
                .maxKeys(maxKeys == null ? 50 : Math.min(Math.max(maxKeys, 1), 1000))
                .continuationToken(continuationToken)
                .build();

        ListObjectsV2Response resp = s3.listObjectsV2(req);

        List<DocSummary> items = resp.contents().stream()
                .filter(o -> o.key() != null && o.key().endsWith(".json"))
                .map(o -> new DocSummary(fromS3Key(o.key()), o.lastModified(), null))
                .toList();

        return new Page(items, resp.isTruncated() ? resp.nextContinuationToken() : null);
    }

    private String toS3Key(String key) {
        return props.normalizedPrefix() + key + ".json";
    }

    private String fromS3Key(String s3Key) {
        String prefix = props.normalizedPrefix();
        String k = s3Key.startsWith(prefix) ? s3Key.substring(prefix.length()) : s3Key;
        return k.endsWith(".json") ? k.substring(0, k.length() - 5) : k;
    }

    private static String stripQuotes(String etag) {
        if (etag == null) return null;
        return etag.replace("\"", "");
    }
}
