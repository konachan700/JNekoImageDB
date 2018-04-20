package service;

import java.io.File;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import model.TagMetadata;
import proto.CryptographyService;
import proto.LocalDaoService;
import proto.UseStorageDirectory;

public class LocalDaoServiceImpl implements UseStorageDirectory, LocalDaoService {
	private final MVStore mvStore;
	private final MVMap<String, TagMetadata> tags;

	public LocalDaoServiceImpl(CryptographyService cryptographyService) {
		final File database = getFile("metadata.kv");

		mvStore = new MVStore.Builder()
				.fileName(database.getAbsolutePath())
				.encryptionKey(Hex.encodeHex(cryptographyService.getAuthData()))
				.autoCommitDisabled()
				.cacheSize(64)
				.open();
		tags = mvStore.openMap("tags");
	}

	@Override public boolean tagIsExist(String tag) {
		return tags.containsKey(tag);
	}

	@Override public void tagSave(String tag, TagMetadata meta) {
		if (tag != null && meta != null) {
			tags.put(tag, meta);
			mvStore.commit();
		}
	}

	@Override public void tagDelete(String tag) {
		if (tag != null) {
			tags.remove(tag);
			mvStore.commit();
		}
	}

	@Override public TagMetadata tagGetMeta(String tag) {
		return tags.get(tag);
	}

	@Override public long tagGetCount() {
		return tags.size();
	}

	@Override public List<String> tagGetList(int start, int end) {
		return tags.keyList().subList(start, end);
	}

	@Override public MVMap<String, TagMetadata> getTags() {
		return tags;
	}

	@Override public void dispose() {
		mvStore.commit();
		mvStore.compactRewriteFully();
		mvStore.close();
	}
}
