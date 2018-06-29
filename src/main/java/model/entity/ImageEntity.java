package model.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name="images", indexes = {
		@Index(columnList = "imageHash", 		name = "imageHash_idx"),
		@Index(columnList = "imageCreateDate", 	name = "imageCreateDate_idx")
})
public class ImageEntity implements Serializable {
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name="imageID", unique = true, nullable = false)
	private long imageID;

	@Column(name="imageHash", unique = true, nullable = false, length = 64)
	private byte[] imageHash;

	@Column(name="imageCreateDate", nullable = false)
	private long imageCreateDate;

	@Column(name="imageDeleted", nullable = false)
	private boolean imageDeleted = false;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name="imagesTagsRel",
			joinColumns = @JoinColumn(name="tagID"),
			inverseJoinColumns = @JoinColumn(name="imageID")
	)
	private Set<TagEntity> tags = new HashSet<>();

	public long getImageID() {
		return imageID;
	}

	public void setImageID(long imageID) {
		this.imageID = imageID;
	}

	public byte[] getImageHash() {
		return imageHash;
	}

	public void setImageHash(byte[] imageHash) {
		this.imageHash = imageHash;
	}

	public long getImageCreateDate() {
		return imageCreateDate;
	}

	public void setImageCreateDate(long imageCreateDate) {
		this.imageCreateDate = imageCreateDate;
	}

	public boolean isImageDeleted() {
		return imageDeleted;
	}

	public void setImageDeleted(boolean imageDeleted) {
		this.imageDeleted = imageDeleted;
	}

	public Set<TagEntity> getTags() {
		return tags;
	}

	public void setTags(Set<TagEntity> tags) {
		this.tags = tags;
	}
}
