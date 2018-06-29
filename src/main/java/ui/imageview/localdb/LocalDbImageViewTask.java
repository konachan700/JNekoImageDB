package ui.imageview.localdb;

import model.entity.ImageEntity;
import ui.imageview.AbstractTask;

public class LocalDbImageViewTask extends AbstractTask {
	private LocalDbImageView localDbImageView;
	private ImageEntity imageEntity;

	public LocalDbImageViewTask(LocalDbImageView localDbImageView, ImageEntity imageEntity, int pageId, int id, int pageCount) {
		this.localDbImageView = localDbImageView;
		this.imageEntity = imageEntity;
		super.setPageCount(pageCount);
		super.setPageId(pageId);
		super.setId(id);
	}

	public ImageEntity getImageEntity() {
		return imageEntity;
	}

	public void setImageEntity(ImageEntity imageEntity) {
		this.imageEntity = imageEntity;
	}

	public LocalDbImageView getLocalDbImageView() {
		return localDbImageView;
	}

	public void setLocalDbImageView(LocalDbImageView localDbImageView) {
		this.localDbImageView = localDbImageView;
	}
}
