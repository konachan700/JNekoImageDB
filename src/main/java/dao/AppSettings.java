package dao;

public class AppSettings {
    private double mainWindowWidth = 900;
    private double mainWindowHeight = 600;
    private double mainWindowSplitterPos = 400;

    private double importWindowWidth = 900;
    private double importWindowHeight = 600;
    private double importWindowSplitterPos = 300;

    private String lastPathForImporter = ".";

    public String getLastPathForImporter() {
        return lastPathForImporter;
    }

    public void setLastPathForImporter(String lastPathForImporter) {
        this.lastPathForImporter = lastPathForImporter;
    }

    public double getMainWindowWidth() {
        return mainWindowWidth;
    }

    public void setMainWindowWidth(double mainWindowWidth) {
        this.mainWindowWidth = mainWindowWidth;
    }

    public double getMainWindowHeight() {
        return mainWindowHeight;
    }

    public void setMainWindowHeight(double mainWindowHeight) {
        this.mainWindowHeight = mainWindowHeight;
    }

    public double getMainWindowSplitterPos() {
        return mainWindowSplitterPos;
    }

    public void setMainWindowSplitterPos(double mainWindowSplitterPos) {
        this.mainWindowSplitterPos = mainWindowSplitterPos;
    }

    public double getImportWindowWidth() {
        return importWindowWidth;
    }

    public void setImportWindowWidth(double importWindowWidth) {
        this.importWindowWidth = importWindowWidth;
    }

    public double getImportWindowHeight() {
        return importWindowHeight;
    }

    public void setImportWindowHeight(double importWindowHeight) {
        this.importWindowHeight = importWindowHeight;
    }

    public double getImportWindowSplitterPos() {
        return importWindowSplitterPos;
    }

    public void setImportWindowSplitterPos(double importWindowSplitterPos) {
        this.importWindowSplitterPos = importWindowSplitterPos;
    }
}
