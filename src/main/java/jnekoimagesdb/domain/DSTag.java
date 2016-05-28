package jnekoimagesdb.domain;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
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
@Table(name="tags", indexes = { 
    @Index(name="tagNameIndex", columnList="xname") 
})
public class DSTag implements Serializable {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="iid", unique = true, nullable = false)
    private long tagID;
    
    @Column(name="xname", unique = true, nullable = false, length = 128)
    private String tagName;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="img_tag",
        joinColumns=
            @JoinColumn(name="tagID"),
        inverseJoinColumns=
            @JoinColumn(name="imageID")
        )
    private Set<DSImage> images;

    protected DSTag () {}
    
    public DSTag(String _tag) {
        tagName = _tag;
    }
    
    public long getTagID() {
        return tagID;
    }

    public void setTagID(long tagID) {
        this.tagID = tagID;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Set<DSImage> getImages() {
        return images;
    }

    public void setImages(Set<DSImage> images) {
        this.images = images;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o instanceof DSTag) {
            return ((DSTag)o).getTagName().equalsIgnoreCase(tagName);
        } else if (o instanceof String) {
            return ((String)o).equalsIgnoreCase(tagName);
        } else 
            return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.tagName);
        return hash;
    }
}