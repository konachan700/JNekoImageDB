package dao;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="ImageId")
public class ImageId implements Serializable {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name="oid")
    private long oid;

    @Column(name="imgId", unique = true, nullable = false, length = 128)
    private String imgId;

    @Column(name="timestamp")
    private long timestamp;

    @ManyToMany(mappedBy = "Tag", fetch = FetchType.LAZY)
    private Set<Tag> tags = new HashSet<>();

    public ImageId() {
        setTimestamp(System.currentTimeMillis());
    }

    public ImageId(String id) {
        setImgId(id);
        setTimestamp(System.currentTimeMillis());
    }

    public String getImgId() {
        return imgId;
    }

    public void setImgId(String imgId) {
        this.imgId = imgId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getOid() {
        return oid;
    }

    public void setOid(long oid) {
        this.oid = oid;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
    }
}
