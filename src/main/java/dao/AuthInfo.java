package dao;

public class AuthInfo {
    private byte[] encryptedMasterKey;
    private byte[] encryptedMasterKeyHash;
    private byte[] decryptedMasterKeyHash;

    public byte[] getEncryptedMasterKey() {
        return encryptedMasterKey;
    }

    public void setEncryptedMasterKey(byte[] encryptedMasterKey) {
        this.encryptedMasterKey = encryptedMasterKey;
    }

    public byte[] getEncryptedMasterKeyHash() {
        return encryptedMasterKeyHash;
    }

    public void setEncryptedMasterKeyHash(byte[] encryptedMasterKeyHash) {
        this.encryptedMasterKeyHash = encryptedMasterKeyHash;
    }

    public byte[] getDecryptedMasterKeyHash() {
        return decryptedMasterKeyHash;
    }

    public void setDecryptedMasterKeyHash(byte[] decryptedMasterKeyHash) {
        this.decryptedMasterKeyHash = decryptedMasterKeyHash;
    }
}
