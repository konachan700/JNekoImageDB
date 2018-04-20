package jnekouilib.editor;

public interface EditorTypeNumber {
    public void setXNumberBorderValues(long min, long max);
    public void setXNumber(long val);
    public long getXNumber();
    public boolean isXNumberValid();
}
