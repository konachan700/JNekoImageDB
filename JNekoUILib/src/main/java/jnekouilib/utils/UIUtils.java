package jnekouilib.utils;

public class UIUtils {

    
    public static String getStringFromObject(int index, Object ... obj) {
        if (obj == null) return null;
        if (obj.length < (index+1)) return null;
        if (obj[index] == null) return null;
        return (obj[index] instanceof String) ? ((String) obj[index]) : null;
    }

    
}
