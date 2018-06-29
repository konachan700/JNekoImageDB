package proto;

import java.util.Collection;
import java.util.List;

import model.ImageEntityWrapper;
import model.Metadata;
import model.entity.ImageEntity;
import model.entity.TagEntity;

public interface LocalDaoService extends Disposable {
	// ******* TAGS **************
	boolean 	tagIsExist(String tag);
	long 		tagGetCount();
	void 		tagSave(String tag);
	void 		tagDelete(String tag);
	TagEntity 	tagGetEntity(String tag);

	List<TagEntity> 	tagGetEntitiesList(int start, int end);
	List<TagEntity>		tagFindStartedWith(String startWith, int count);
	List<TagEntity>		tagGetOrCreate(Collection<String> tags);

	// ******* IMAGES **************
	boolean 	imagesElementExist(byte[] imageHash);
	long 		imagesGetCount();
	void 		imagesWrite(byte[] imageHash, Collection<TagEntity> tags);

	List<ImageEntity> 	imagesGetHashesList(int start, int end);
	ImageEntityWrapper 	imagesFindByTags(Collection<TagEntity> tags, int start, int end);

	// ******* METADATA **************
	void 		saveImageMeta(byte[] hash, Metadata metadata);
	Metadata 	getImageMeta(byte[] hash);

}
