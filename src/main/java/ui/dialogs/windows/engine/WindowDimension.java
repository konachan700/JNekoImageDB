package ui.dialogs.windows.engine;

public class WindowDimension {
	Double width;
	Double height;

	public Double getWidth() {
		return width;
	}

	public void setWidth(Double width) {
		this.width = width;
	}

	public Double getHeight() {
		return height;
	}

	public void setHeight(Double height) {
		this.height = height;
	}

	public WindowDimension() {
	}

	public WindowDimension(Double width, Double height) {
		this.width = width;
		this.height = height;
	}
}
