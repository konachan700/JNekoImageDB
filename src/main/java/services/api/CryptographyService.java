package services.api;

public interface CryptographyService {
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
