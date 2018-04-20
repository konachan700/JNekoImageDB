package jnekouilib.editor;

import java.lang.reflect.Method;

public interface EditorFillable {
    public void fillFromObject(Object o, Method m);
}
