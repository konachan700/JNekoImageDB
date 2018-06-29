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
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name="tags", indexes = {
		@Index(columnList = "tagText", 			name = "tagText_idx"),
		@Index(columnList = "tagCreateDate", 	name = "tagCreateDate_idx")
})
public class TagEntity implements Serializable {
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name="tagID", unique = true, nullable = false)
	private long tagID;

	@Column(name="tagText", unique = true, nullable = false, length = 196)
	private String tagText;

	@Column(name="tagCreateDate", nullable = false)
	private long tagCreateDate;

	@Column(name="tagDeleted", nullable = false)
	private boolean tagDeleted = false;

	@ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "tagType_join")
	private TagTypeEntity tagType;

	@ManyToMany(fetch = FetchType.LAZY)
	@JoinTable(name="imagesTagsRel",
			joinColumns = @JoinColumn(name="imageID"),
			inverseJoinColumns = @JoinColumn(name="tagID")
	)
	private Set<ImageEntity> images = new HashSet<>();

	public long getTagID() {
		return tagID;
	}

	public void setTagID(long tagID) {
		this.tagID = tagID;
	}

	public String getTagText() {
		return tagText;
	}

	public void setTagText(String tagText) {
		this.tagText = tagText;
	}

	public long getTagCreateDate() {
		return tagCreateDate;
	}

	public void setTagCreateDate(long tagCreateDate) {
		this.tagCreateDate = tagCreateDate;
	}

	public boolean isTagDeleted() {
		return tagDeleted;
	}

	public void setTagDeleted(boolean tagDeleted) {
		this.tagDeleted = tagDeleted;
	}

	public TagTypeEntity getTagType() {
		return tagType;
	}

	public void setTagType(TagTypeEntity tagType) {
		this.tagType = tagType;
	}

	public Set<ImageEntity> getImages() {
		return images;
	}

	public void setImages(Set<ImageEntity> images) {
		this.images = images;
	}
}
