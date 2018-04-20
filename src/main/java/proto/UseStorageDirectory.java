package proto;

import org.apache.commons.codec.binary.Hex;

import java.io.File;

public interface UseStorageDirectory {
	String PREFIX = "DB";
	File storageDirectory = new File("./storage").getAbsoluteFile();

	default File getDirectory() {
		storageDirectory.mkdirs();
		if (storageDirectory.exists() && storageDirectory.isDirectory()) return storageDirectory;
		throw new IllegalStateException("UseStorageDirectory.getDirectory() cannot create storage directory");
	}

	default File getFile(String file) {
		final File f = new File(getDirectory().getAbsolutePath() + File.separator + file).getAbsoluteFile();
		return f;
	}

	default File getLocalStorageElement(String storageName, byte[] hash) {
		final StringBuilder pathToStorageDir = new StringBuilder();
		pathToStorageDir
				.append(getDirectory().getAbsolutePath())
				.append(File.separator)
				.append(storageName)
				.append(File.separator)
				.append(PREFIX).append(((int)hash[0]) & 0x0F)
				.append(File.separator)
				.append(PREFIX).append(((int)hash[0] >> 4) & 0x0F)
				.append(File.separator)
				.append(PREFIX).append(((int)hash[1]) & 0x0F)
				.append(File.separator)
				.append(PREFIX).append(((int)hash[1] >> 4) & 0x0F);

		final File dir = new File(pathToStorageDir.toString());
		dir.mkdirs();

		pathToStorageDir
				.append(File.separator)
				.append(Hex.encodeHexString(hash), 8, 16 + 8)
				.append(".bin");

		return new File(pathToStorageDir.toString());
	}
}
