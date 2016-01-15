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
@Table(name="albums")
public class DSAlbum implements Serializable {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="iid", unique = true, nullable = false)
    private long albumID;
    
    @Column(name="xname", unique = false, nullable = false, length = 128)
    private String albumName;
    
    @Column(name="xtext", unique = false, nullable = false)
    private String albumText;
    
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name="img_alb",
        joinColumns=
            @JoinColumn(name="albumID"),
        inverseJoinColumns=
            @JoinColumn(name="imageID")
        )
    private Set<DSImage> images;

    public DSAlbum(String name, String text) {
        albumName = name;
        albumText = text;
    }
    
    public long getAlbumID() {
        return albumID;
    }

    public void setAlbumID(long albumID) {
        this.albumID = albumID;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getAlbumText() {
        return albumText;
    }

    public void setAlbumText(String albumText) {
        this.albumText = albumText;
    }

    public Set<DSImage> getImages() {
        return images;
    }

    public void setImages(Set<DSImage> images) {
        this.images = images;
    }
}
