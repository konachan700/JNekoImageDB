package services.api;

public interface WaitInformer {
	void onProgress(String text, long countInQueue);
}
