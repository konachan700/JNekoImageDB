package proto;

import java.util.List;

import org.h2.mvstore.MVMap;

import model.TagMetadata;

public interface LocalDaoService extends Disposable {
	boolean tagIsExist(String tag);
	void tagSave(String tag, TagMetadata meta);
	void tagDelete(String tag);
	TagMetadata tagGetMeta(String tag);
	long tagGetCount();
	List<String> tagGetList(int start, int end);
	MVMap<String, TagMetadata> getTags();


}
