package ui.imageview;

public abstract class AbstractTask {
	private byte[] cachedImage;
	private int pageCount;
	private int pageId;
	private int id;
	private long privateId;

	public byte[] getCachedImage() {
		return cachedImage;
	}

	public void setCachedImage(byte[] cachedImage) {
		this.cachedImage = cachedImage;
	}

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public int getPageId() {
		return pageId;
	}

	public void setPageId(int pageId) {
		this.pageId = pageId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getPrivateId() {
		return privateId;
	}

	public void setPrivateId(long privateId) {
		this.privateId = privateId;
	}
}
