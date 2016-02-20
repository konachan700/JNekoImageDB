package jnekoimagesdb.domain;

import java.io.Serializable;
import java.util.Arrays;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="stringpairs")
public class DSStringPair implements Serializable {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="iid", unique = true, nullable = false)
    private long optID;
    
    @Column(name="xmd5", unique = true, nullable = false, length = 16)
    private byte[] MD5;

    @Column(name="xvalue", unique = false, nullable = false, length = 2048)
    private String value;

    public long getOptID() {
        return optID;
    }

    public void setOptID(long optID) {
        this.optID = optID;
    }

    public byte[] getMD5() {
        return Arrays.copyOf(MD5, MD5.length);
    }

    public void setMD5(byte[] MD5) {
        this.MD5 = Arrays.copyOf(MD5, MD5.length);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
