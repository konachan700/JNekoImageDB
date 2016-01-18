package datasources;

import java.io.Serializable;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

@Entity
@Table(name="images")
public class DSImage implements Serializable {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="iid", unique = true, nullable = false)
    private long imageID;
    
    @Column(name="xmd5", unique = true, nullable = false, length = 16)
    private byte[] MD5;

    @ManyToMany(mappedBy = "images", fetch = FetchType.LAZY)
    private Set<DSAlbum> albums;

    protected DSImage() {}
    
    public DSImage(byte[] _MD5) {
        MD5 = _MD5;
    }
    
    public DSImage(byte[] _MD5, long _id) {
        MD5 = _MD5;
        imageID = _id;
    }
    
    public long getImageID() {
        return imageID;
    }

    public void setImageID(long imageID) {
        this.imageID = imageID;
    }

    public byte[] getMD5() {
        return MD5;
    }

    public void setMD5(byte[] MD5) {
        this.MD5 = MD5;
    }

    public Set<DSAlbum> getAlbums() {
        return albums;
    }

    public void setAlbums(Set<DSAlbum> albums) {
        this.albums = albums;
    }
    
    @Override
    public boolean equals(Object o) {
        if (o instanceof DSImage)
            return (((DSImage) o).getImageID() == imageID);
        else if (o instanceof Long)
            return (((Long) o).equals(imageID));
        else 
            return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash + (int) (this.imageID ^ (this.imageID >>> 32));
        return hash;
    }
}
