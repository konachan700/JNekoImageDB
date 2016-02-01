package jnekoimagesdb.domain;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="prevsz")
public class DSPreviewSize implements Serializable {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="iid", unique = true, nullable = false)
    private long prevID;
    
    @Column(name="xname", unique = true, nullable = false, length = 128)
    private String prevName;
    
    @Column(name="xwidth", unique = false, nullable = false)
    private long width;
    
    @Column(name="xheight", unique = false, nullable = false)
    private long height;
    
    @Column(name="xsquare", unique = false, nullable = false)
    private boolean squared;
    
    @Column(name="xprimary", unique = false, nullable = false)
    private boolean primary;

    public DSPreviewSize() {
        primary = false;
    }
    
    public DSPreviewSize(String _prevName, long _width, long _height, boolean _squared) {
        prevName = _prevName;
        width = _width;
        height = _height;
        squared = _squared;
        primary = false;
    }
    
    public long getPrevID() {
        return prevID;
    }

    public void setPrevID(long prevID) {
        this.prevID = prevID;
    }

    public String getPrevName() {
        return prevName;
    }

    public void setPrevName(String prevName) {
        this.prevName = prevName;
    }

    public long getWidth() {
        return width;
    }

    public void setWidth(long width) {
        this.width = width;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public boolean isSquared() {
        return squared;
    }

    public void setSquared(boolean squared) {
        this.squared = squared;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }
}
