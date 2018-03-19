package dao;

import org.apache.commons.codec.binary.Hex;
import utils.CryptUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Objects;

@Entity
@Table(name="ImageDuplicateProtect", indexes = {
        @Index(columnList = "hashOfNameAndSize", name = "hashOfNameAndSize_hidx"),
})
public class ImageDuplicateProtect implements Serializable {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    @Column(name="oid")
    private long oid;

    @Column(name="hashOfNameAndSize", unique = true, nullable = false, length = 128)
    private String hashOfNameAndSize;

    public ImageDuplicateProtect() {}

    public ImageDuplicateProtect(Path p) {
        if (Objects.isNull(p)) throw new IllegalArgumentException("Path cannot be null");

        final String fileName = p.toFile().getName();
        final String fileSize = "-" + p.toFile().length();
        final byte[] hashable = (fileName + fileSize).getBytes();
        setHashOfNameAndSize(Hex.encodeHexString(CryptUtils.sha512(hashable), true));
    }

    public String getHashOfNameAndSize() {
        return hashOfNameAndSize;
    }

    public void setHashOfNameAndSize(String hashOfNameAndSize) {
        this.hashOfNameAndSize = hashOfNameAndSize;
    }
}
