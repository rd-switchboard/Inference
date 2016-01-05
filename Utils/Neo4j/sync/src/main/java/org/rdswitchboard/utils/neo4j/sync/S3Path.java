package org.rdswitchboard.utils.neo4j.sync;

import org.apache.commons.lang3.StringUtils;

public class S3Path {
	private static final String PART_S3 = "s3://";
	private static final String PART_SLASH = "/";
	
	private String bucket;
	private String key;
	
	public S3Path(String bucket, String key) {
		super();
		this.bucket = bucket;
		this.key = key;
	}
	
	public static S3Path parse(String path) {
		int idx = path.toLowerCase().indexOf(PART_S3);
		if (idx >= 0) {
			int idx2 = path.indexOf(PART_SLASH, idx + PART_S3.length());
			if (idx2 > 0)
				return new S3Path(path.substring(idx + PART_S3.length(), idx2), path.substring(idx2));
		}
		
		return null;
	}
		
	public boolean isValud() {
		return !StringUtils.isEmpty(bucket) && !StringUtils.isEmpty(key); 
	}
	
	public String getBucket() {
		return bucket;
	}
	
	public void setBucket(String bucket) {
		this.bucket = bucket;
	}
	
	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public String getFile() {
		if (!StringUtils.isEmpty(key)) {
			int idx = key.lastIndexOf(PART_SLASH);
			return idx >= 0 ? key.substring(idx + 1) : key;
		}
		
		return null;
	}
	
	@Override
	public String toString() {
		return "S3Path [bucket=" + bucket + ", key=" + key + "]";
	}
}
