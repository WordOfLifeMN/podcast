package org.wolm.aws;

import static org.fest.assertions.Assertions.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class AwsS3HelperIT {
	private AwsS3Helper helperUnderTest = new AwsS3Helper();

	@Test
	public void shouldHaveBuckets() {
		// when
		List<Bucket> buckets = helperUnderTest.getBuckets();

		// then
		assertThat(buckets.size()).isGreaterThanOrEqualTo(2);

		List<String> names = new ArrayList<>(buckets.size());
		for (Bucket bucket : buckets)
			names.add(bucket.getName());
		assertThat(names).contains("wordoflife.mn.audio", "wordoflife.mn.video");
	}

	@Test
	public void shouldHaveBucket() {
		// when
		Bucket bucket = helperUnderTest.getBucket("wordoflife.mn.audio");

		// then
		assertThat(bucket).isNotNull();
		assert bucket != null;
		assertThat(bucket.getName()).isEqualTo("wordoflife.mn.audio");
	}

	@Test
	public void shouldListObjects() {
		// when
		Bucket bucket = helperUnderTest.getBucket("wordoflife.mn.audio");
		assert bucket != null;
		S3ObjectSummary summary = helperUnderTest.getObjectSummary(bucket, "index.html");

		// then
		assertThat(summary).isNotNull();
		assert summary != null;
		assertThat(summary.getBucketName()).isEqualTo("wordoflife.mn.audio");
		assertThat(summary.getKey()).isEqualTo("index.html");
	}
}
