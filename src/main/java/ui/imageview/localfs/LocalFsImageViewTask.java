package ui.imageview.localfs;

import java.nio.file.Path;

import ui.imageview.AbstractTask;

public class LocalFsImageViewTask extends AbstractTask {
	private Path file;
	private LocalFsImageView localFsImageView;

	public LocalFsImageViewTask(Path file, LocalFsImageView localFsImageView, int pageId, int id, int pageCount) {
		this.file = file;
		this.localFsImageView = localFsImageView;
		super.setPageCount(pageCount);
		super.setPageId(pageId);
		super.setId(id);
	}

	public Path getFile() {

		return file;
	}

	public void setFile(Path file) {
		this.file = file;
	}

	public LocalFsImageView getLocalFsImageView() {
		return localFsImageView;
	}

	public void setLocalFsImageView(LocalFsImageView localFsImageView) {
		this.localFsImageView = localFsImageView;
	}
}
