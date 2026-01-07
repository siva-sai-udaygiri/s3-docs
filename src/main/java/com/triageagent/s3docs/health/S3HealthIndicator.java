package com.triageagent.s3docs.health;

import com.triageagent.s3docs.config.S3Properties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

@Component
public class S3HealthIndicator implements HealthIndicator {

    private final S3Client s3;
    private final S3Properties props;

    public S3HealthIndicator(S3Client s3, S3Properties props) {
        this.s3 = s3;
        this.props = props;
    }

    @Override
    public Health health() {
        try {
            s3.headBucket(HeadBucketRequest.builder().bucket(props.bucket()).build());
            return Health.up().withDetail("bucket", props.bucket()).build();
        } catch (Exception e) {
            return Health.down(e).withDetail("bucket", props.bucket()).build();
        }
    }
}
