package jnekouilib.editor;

public interface EditorTypeText {
    public void setXTextReadOnly(boolean ro);
    public void setXTextMaxChars(int max);
    public void setXTextHelp(String text);
    public void setXText(String text);
    public String getXText();
    public boolean isXTextEmpty();
}
