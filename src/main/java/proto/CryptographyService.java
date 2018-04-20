package proto;

public interface CryptographyService extends Disposable {
    byte[] encrypt(byte[] open);
    byte[] decrypt(byte[] encrypted);
    byte[] hash(byte[] data);
    byte[] getAuthData();
}
