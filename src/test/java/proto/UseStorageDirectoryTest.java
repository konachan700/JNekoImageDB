package proto;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class UseStorageDirectoryTest {
	private class UseStorageDirectoryImplTest implements UseStorageDirectory {}

	@Mock
	UseStorageDirectoryImplTest useStorageDirectory;

	@BeforeMethod
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void testGetExtention() {
		when(useStorageDirectory.getExtention(any())).thenCallRealMethod();
		File f = new File("/var/log/someLog.txt");
		String ext = useStorageDirectory.getExtention(f);
		assertEquals(ext, "txt");
	}
}
