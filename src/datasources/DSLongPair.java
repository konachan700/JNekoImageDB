package datasources;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="longpairs")
public class DSLongPair implements Serializable {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="iid", unique = true, nullable = false)
    private long optID;
    
    @Column(name="xmd5", unique = true, nullable = false, length = 16)
    private byte[] MD5;
    
    @Column(name="xvalue", unique = false, nullable = false)
    private long value;

    public long getOptID() {
        return optID;
    }

    public void setOptID(long optID) {
        this.optID = optID;
    }

    public byte[] getMD5() {
        return MD5;
    }

    public void setMD5(byte[] MD5) {
        this.MD5 = MD5;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }
}
