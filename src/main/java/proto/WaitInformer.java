package proto;

public interface WaitInformer {
	void onProgress(String text, long countInQueue);
}
