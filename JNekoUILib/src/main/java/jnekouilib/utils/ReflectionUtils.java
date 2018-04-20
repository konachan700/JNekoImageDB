package jnekouilib.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReflectionUtils {
    public static Class getGenericOfCollection(Class rootClass, Method m) {
        try {
            final ParameterizedType type = (ParameterizedType) m.getGenericReturnType();
            final Class<?> retClass = (Class<?>) type.getActualTypeArguments()[0];
            return retClass;
        } catch (SecurityException ex) {
            Logger.getLogger(ReflectionUtils.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public static Object createNew(Class cl) {
        try {
            final Constructor ca = cl.getDeclaredConstructor();
            ca.setAccessible(true);
            return ca.newInstance();
        } catch (NoSuchMethodException | SecurityException ex) {
            Logger.getLogger(ReflectionUtils.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.getLogger(ReflectionUtils.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    };

    public static boolean isCreatable(Class cl) {
        try {
            final Constructor ca = cl.getDeclaredConstructor();
            return ca != null;
        } catch (NoSuchMethodException | SecurityException ex) {
            Logger.getLogger(ReflectionUtils.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
