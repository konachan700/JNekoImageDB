package model;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class TagMetadata extends Metadata implements Serializable {
	private final Set<String> seeAlso = new HashSet<>();
	private boolean deleted = false;

	public void addTag(String tag) {
		seeAlso.add(tag);
	}

	public void addTags(String ... tags) {
		seeAlso.addAll(Arrays.asList(tags));
	}

	public void addTags(Collection<String> tags) {
		seeAlso.addAll(tags);
	}

	public Set<String> getTags() {
		return seeAlso;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
}
