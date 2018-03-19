package service.fs;

import java.nio.file.Path;

public class FilePusherTask {
    private FilePusherActionListener pusherActionListener;
    private Path imagePath;

    public FilePusherTask(FilePusherActionListener p, Path path) {
        pusherActionListener = p;
        imagePath = path;
    }

    public FilePusherActionListener getPusherActionListener() {
        return pusherActionListener;
    }

    public void setPusherActionListener(FilePusherActionListener pusherActionListener) {
        this.pusherActionListener = pusherActionListener;
    }

    public Path getImagePath() {
        return imagePath;
    }

    public void setImagePath(Path imagePath) {
        this.imagePath = imagePath;
    }
}
