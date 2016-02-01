package datasources;

import java.io.Serializable;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name="tags")
public class DSTag implements Serializable {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="iid", unique = true, nullable = false)
    private long tagID;
    
    @Column(name="xname", unique = false, nullable = false, length = 128)
    private String tagName;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="img_tag",
        joinColumns=
            @JoinColumn(name="tagID"),
        inverseJoinColumns=
            @JoinColumn(name="imageID")
        )
    private Set<DSImage> images;

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
}
