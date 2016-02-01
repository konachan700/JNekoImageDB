package datasources;

import img.XImg;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

public class SettingsUtil {
    private static Session 
            currSession = null;
    
    public static void init() {
        currSession = HibernateUtil.getCurrentSession();
    }
    
    public static boolean getBoolean(String name, boolean defaultValue) {
        long l = getLong(name, defaultValue ? 1 : 0);
        return l == 1;
    }
    
    public static void setBoolean(String name, boolean value) {
        setLong(name, value ? 1 : 0);
    }
    
    public static int getInt(String name, int defaultValue) {
        return (int)(getLong(name, defaultValue));
    }
    
    public static void setInt(String name, int value) {
        setLong(name, value);
    }
    
    public static long getLong(String name, long defaultValue) {
        final List<DSLongPair> list = currSession
                .createCriteria(DSLongPair.class)
                .add(Restrictions.eq("MD5", XImg.getCrypt().MD5(name.getBytes())))
                .list();        
        if (list.size() > 0) {
            final DSLongPair ds = list.get(0);
            return ds.getValue();
        }
        
        return defaultValue;
    }
    
    public static void setLong(String name, long value) {
        final List<DSLongPair> list = currSession
                .createCriteria(DSLongPair.class)
                .add(Restrictions.eq("MD5", XImg.getCrypt().MD5(name.getBytes())))
                .list();        
        if (list.size() > 0) {
            final DSLongPair ds = list.get(0);
            HibernateUtil.beginTransaction(currSession);
            ds.setValue(value);
            currSession.save(ds);
            HibernateUtil.commitTransaction(currSession);
        } else {
            final DSLongPair ds = new DSLongPair();
            HibernateUtil.beginTransaction(currSession);
            ds.setMD5(XImg.getCrypt().MD5(name.getBytes()));
            ds.setValue(value);
            currSession.save(ds);
            HibernateUtil.commitTransaction(currSession);
        }
    }
    
    public static Path getPath(String name) {
        final String s = _getString(name);
        return (s != null) ? FileSystems.getDefault().getPath(s) : FileSystems.getDefault().getPath("./").toAbsolutePath();
    }
    
    public static void setPath(String name, Path value) {
        if ((name != null) && (value != null)) _setString(name, value.toAbsolutePath().toString());
    }
    
    public static String getString(String name, String defaultValue) {
        final String s = _getString(name);
        return (s != null) ? s : defaultValue;
    }
    
    public static void setString(String name, String value) {
        if ((name != null) && (value != null)) _setString(name, value);
    }
    
    private static void _setString(String name, String value) {
        final List<DSStringPair> list = currSession
                .createCriteria(DSStringPair.class)
                .add(Restrictions.eq("MD5", XImg.getCrypt().MD5(name.getBytes())))
                .list();        
        if (list.size() > 0) {
            final DSStringPair ds = list.get(0);
            HibernateUtil.beginTransaction(currSession);
            ds.setValue(value);
            currSession.save(ds);
            HibernateUtil.commitTransaction(currSession);
        } else {
            final DSStringPair ds = new DSStringPair();
            HibernateUtil.beginTransaction(currSession);
            ds.setMD5(XImg.getCrypt().MD5(name.getBytes()));
            ds.setValue(value);
            currSession.save(ds);
            HibernateUtil.commitTransaction(currSession);
        }
    }
    
    private static String _getString(String name) {
        final List<DSStringPair> list = currSession
                .createCriteria(DSStringPair.class)
                .add(Restrictions.eq("MD5", XImg.getCrypt().MD5(name.getBytes())))
                .list();        
        if (list.size() > 0) {
            final DSStringPair ds = list.get(0);
            return ds.getValue();
        }
        
        return null;
    }
}
