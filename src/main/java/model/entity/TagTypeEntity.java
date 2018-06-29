package model.entity;

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name="tagTypes", indexes = {
		@Index(columnList = "tagTypeText", name = "tagTypeText_idx")
})
public class TagTypeEntity implements Serializable {
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name="tagTypeID", unique = true, nullable = false)
	private long tagTypeID;

	@Column(name="tagTypeText", unique = true, nullable = false, length = 196)
	private String tagTypeText;

	@Column(name="tagTypeDeleted", nullable = false)
	private boolean tagTypeDeleted = false;

	@OneToMany(mappedBy = "tagType")
	private Set<TagEntity> tags;

	public long getTagTypeID() {
		return tagTypeID;
	}

	public void setTagTypeID(long tagTypeID) {
		this.tagTypeID = tagTypeID;
	}

	public String getTagTypeText() {
		return tagTypeText;
	}

	public void setTagTypeText(String tagTypeText) {
		this.tagTypeText = tagTypeText;
	}

	public boolean isTagTypeDeleted() {
		return tagTypeDeleted;
	}

	public void setTagTypeDeleted(boolean tagTypeDeleted) {
		this.tagTypeDeleted = tagTypeDeleted;
	}

	public Set<TagEntity> getTags() {
		return tags;
	}

	public void setTags(Set<TagEntity> tags) {
		this.tags = tags;
	}
}
