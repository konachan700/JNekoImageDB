package model;

import java.util.List;

import model.entity.ImageEntity;

public class ImageEntityWrapper {
	private int count;
	private List<ImageEntity> list;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public List<ImageEntity> getList() {
		return list;
	}

	public void setList(List<ImageEntity> list) {
		this.list = list;
	}
}
