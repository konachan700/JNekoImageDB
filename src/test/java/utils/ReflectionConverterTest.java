package utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ReflectionConverterTest {
	private class Dummy {
		private String testString;
		private int testInt;

		public String getTestString() {
			return testString;
		}

		public void setTestString(String testString) {
			this.testString = testString;
		}

		public int getTestInt() {
			return testInt;
		}

		public void setTestInt(int testInt) {
			this.testInt = testInt;
		}
	}

	private class DummyNumberTwo {
		private int testInt;
		private String testString;

		public int getTestInt() {
			return testInt;
		}

		public void setTestInt(int testInt) {
			this.testInt = testInt;
		}

		public String getTestString() {
			return testString;
		}

		public void setTestString(String testString) {
			this.testString = testString;
		}
	}

	@Test
	public void testConvert_sameClasses() {
		Dummy d1 = new Dummy();
		d1.setTestInt(9);
		d1.setTestString("test");

		Dummy d2 = new Dummy();
		ReflectionConverter.convert(d1, d2);

		assertEquals(d2.getTestInt(), d1.getTestInt());
		assertEquals(d2.getTestString(), d1.getTestString());
	}

	@Test
	public void testConvert_differentClasses() {
		Dummy d1 = new Dummy();
		d1.setTestInt(9);
		d1.setTestString("test");

		DummyNumberTwo d2 = new DummyNumberTwo();
		ReflectionConverter.convert(d1, d2);

		assertEquals(d2.getTestInt(), d1.getTestInt());
		assertEquals(d2.getTestString(), d1.getTestString());
	}
}
