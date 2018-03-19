package service.fs;

import java.nio.file.Path;

public interface FilePusherActionListener {
    void onPush(Path p, int totalCount, int currentCount);
    void onDuplicateDetected(Path p);
    void onError(Path p, Exception e);
    void onZeroQuene();
}
