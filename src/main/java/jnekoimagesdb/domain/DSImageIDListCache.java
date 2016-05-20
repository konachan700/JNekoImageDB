package jnekoimagesdb.domain;

import java.util.List;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

public class DSImageIDListCache {
    public static enum ImgType {
        All, Notagged, NotInAnyAlbum, inAlbum
    }
    
    private static DSImageIDListCache 
            imgAll      = null,
            imgNotagged = null,
            imgWOAlbums = null,
            imgInAlbum  = null;
    
    private final Session hibSession;
    private final ImgType it;
    
    private int[] arrayImg = null;
    private long albumID = 0;

    public DSImageIDListCache(ImgType itx) {
        if (HibernateUtil.getCurrentSession() == null) throw new RuntimeException("DSImageIDListCache: HibernateUtil not inited now.");
        hibSession = HibernateUtil.getCurrentSession();  
        it = itx;
    }
    
    public void reload(long albumId) {
        if (albumID != albumId) {
            albumID = albumId;
            reload();
        }
    }
    
    public synchronized void reload() {
        final List<Long> tmpList;
        switch (it) {
            case Notagged:
                tmpList = hibSession
                        .createQuery("SELECT r.imageID FROM DSImage r WHERE r.tags IS EMPTY ORDER BY r.imageID DESC")
                        .list();
                break;
            case NotInAnyAlbum:
                tmpList = hibSession
                        .createQuery("SELECT r.imageID FROM DSImage r WHERE r.albums IS EMPTY ORDER BY r.imageID DESC")
                        .list();
                break;
            case inAlbum:
                tmpList = hibSession
                        .createCriteria(DSImage.class)
                        .createCriteria("albums")
                        .add(Restrictions.eq("albumID", albumID))
                        .setProjection(Projections.property("imageID"))
                        .addOrder(Order.asc("imageID"))
                        .list();
                break;
            default:
                tmpList = hibSession
                        .createCriteria(DSImage.class)
                        .setProjection(Projections.property("imageID"))
                        .addOrder(Order.asc("imageID"))
                        .list();
                break;
        }

        final int count = tmpList.size();
        arrayImg = new int[count];
        for (int i=0; i<count; i++) {
            arrayImg[i] =  tmpList.get(i).intValue();
        }
    }
    
    public long getID(int num) {
        if (arrayImg == null) throw new RuntimeException("DSImageIDListCache: Cache array is empty. Do reload() first.");
        if ((num >= arrayImg.length) || (num < 0)) return -1;
        return arrayImg[num];
    }
    
    public long getIDReverse(int num) {
        if (arrayImg == null) throw new RuntimeException("DSImageIDListCache: Cache array is empty. Do reload() first.");
        if ((num >= arrayImg.length) || (num < 0)) return -1;
        return arrayImg[arrayImg.length - num - 1];
    }
    
    public int getCount() {
        if (arrayImg == null) throw new RuntimeException("DSImageIDListCache: Cache array is empty. Do reload() first.");
        return arrayImg.length;
    }
    
    public static DSImageIDListCache getAll() {
        if (imgAll == null) imgAll = new DSImageIDListCache(ImgType.All);
        return imgAll;
    }
    
    public static DSImageIDListCache getNotagged() {
        if (imgNotagged == null) imgNotagged = new DSImageIDListCache(ImgType.Notagged);
        return imgNotagged;
    }
    
    public static DSImageIDListCache getWOAlbums() {
        if (imgWOAlbums == null) imgWOAlbums = new DSImageIDListCache(ImgType.NotInAnyAlbum);
        return imgWOAlbums;
    }
    
    public static DSImageIDListCache getInAlbum() {
        if (imgInAlbum == null) imgInAlbum = new DSImageIDListCache(ImgType.inAlbum);
        return imgInAlbum;
    }
    
    public static void reloadAllStatic() {
        getAll().reload();
        getNotagged().reload();
        getWOAlbums().reload();
        getInAlbum().reload();
    }
}
