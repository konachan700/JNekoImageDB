package model;

public class BinaryImage {
	private byte[] image;
	private int width;
	private int height;

	public BinaryImage(byte[] image, int width, int height) {
		this.setImage(image);
		this.setWidth(width);
		this.setHeight(height);
	}

	public byte[] getImage() {
		return image;
	}

	public void setImage(byte[] image) {
		this.image = image;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
}
