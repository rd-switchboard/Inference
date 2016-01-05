package sync;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;
import org.rdswitchboard.utils.neo4j.sync.S3Path;

public class SyncTest {
	
	public static final String TEST_PATH = "S3://neo4j.rdswitchboard/2015-11/23/neo4j.zip";
	public static final String TEST_BROKEN_PATH = "s://neo4j.rdswitchboard";
	public static final String TEST_BUCKET = "neo4j.rdswitchboard";
	public static final String TEST_KEY = "/2015-11/23/neo4j.zip";
	
	
	@Test
	public void testS3Path() {
		
		S3Path path = S3Path.parse(TEST_PATH);
		
		assertNotNull("Must be able to parse correct S3 Path", 
				path);
		
		assertEquals("Must have detected correct S3 Bucket", 
				TEST_BUCKET,
				path.getBucket());

		assertEquals("Must have detected correct S3 Key", 
				TEST_KEY,
				path.getKey());

		assertNull("Must be able to ignore incorrect S3 Paths", 
				S3Path.parse(TEST_BROKEN_PATH));		
	}
}
