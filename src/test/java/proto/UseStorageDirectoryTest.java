package proto;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.testng.annotations.Test;

public class UseStorageDirectoryTest {
	static class UseStorageDirectoryImplTest implements UseStorageDirectory {}

	@Test
	public void testGetExtention() {
		UseStorageDirectory useStorageDirectory = new UseStorageDirectoryImplTest();
		File f = new File("/var/log/someLog.txt");
		String ext = useStorageDirectory.getExtention(f);
		assertEquals(ext, "txt");
	}
}
