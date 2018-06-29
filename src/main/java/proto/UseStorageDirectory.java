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
		return new File(getDirectory().getAbsolutePath() + File.separator + file).getAbsoluteFile();
	}

	default String getExtention(File f) {
		final String name = f.getName();
		final int extDotPos = name.lastIndexOf(".");
		return name.substring(extDotPos + 1);
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
		if (!dir.mkdirs()) {
			if (!dir.exists()) throw new IllegalStateException("can't create storage directory");
		}

		pathToStorageDir
				.append(File.separator)
				.append(Hex.encodeHexString(hash), 8, 16 + 8)
				.append(".bin");

		return new File(pathToStorageDir.toString());
	}
}
