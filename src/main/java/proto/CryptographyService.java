package proto;

public interface CryptographyService extends Disposable {
	//************* Base crypto *************
    byte[] encrypt(byte[] open);
    byte[] decrypt(byte[] encrypted);
    byte[] hash(byte[] data);
    byte[] hash(byte[] data, int iteration);
    byte[] getAuthData();

	//************* File name generator *************
	String getNameForMainDb();
	String getNameForMetadataDb();
	String getNameForCacheDb();
}
