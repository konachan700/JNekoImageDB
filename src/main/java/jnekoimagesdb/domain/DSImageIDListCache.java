package jnekoimagesdb.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

public class DSImageIDListCache {
    public static enum ImgType {
        All, Notagged, NotInAnyAlbum, InAlbum, TagList, TagsAndAlbums
    }
    
    private static DSImageIDListCache 
            imgAll      = null,
            imgNotagged = null,
            imgWOAlbums = null,
            imgInAlbum  = null,
            imgTagged   = null,
            imgTAndA    = null;
    
    private Set<DSTag> 
            searchTag = null,
            excludeTag = null;
    
    private final Session hibSession;
    private final ImgType it;
    
    private int[] arrayImg = null;
    private long albumID = 0;

    public DSImageIDListCache(ImgType itx) {
        if (HibernateUtil.getCurrentSession() == null) throw new RuntimeException("DSImageIDListCache: HibernateUtil not inited now.");
        hibSession = HibernateUtil.getCurrentSession();  
        it = itx;
    }
    
    public void reload(long albumId, Set<DSTag> searchTagA, Set<DSTag> excludeTagA) {
        searchTag = searchTagA;
        excludeTag = excludeTagA;
        albumID = albumId;
        reload();
    }
    
    public void reload(Set<DSTag> searchTagA, Set<DSTag> excludeTagA) {
        searchTag = searchTagA;
        excludeTag = excludeTagA;
        reload();
    }
    
    public void reload(long albumId) {
        if (albumID != albumId) {
            albumID = albumId;
            reload();
        }
    }
    
    private List<Long> getAllID() {
        final List<Long> tmpList;
        tmpList = hibSession
            .createCriteria(DSImage.class)
            .setProjection(Projections.property("imageID"))
            .addOrder(Order.asc("imageID"))
            .list();
        return tmpList;
    }
    
    private List<Long> getTaggedIDWOAlbums() {
        final List<Long> tmpList;
        if ((searchTag != null) && (excludeTag != null)) {
            tmpList = hibSession.createQuery("SELECT r.imageID FROM DSImage r WHERE r.imageID IN (:tags1) AND r.imageID NOT IN (:tags2)")
                .setParameterList("tags1", searchTag)
                .setParameterList("tags2", excludeTag)
                .list();
        } else if ((searchTag != null) && (excludeTag == null)) {
            tmpList = hibSession.createQuery("SELECT r.imageID FROM DSImage r WHERE r.imageID IN (:tags1)")
                .setParameterList("tags1", searchTag)
                .list();
        } else if ((searchTag == null) && (excludeTag != null)) {
            tmpList = hibSession.createQuery("SELECT r.imageID FROM DSImage r WHERE r.imageID NOT IN (:tags2)")
                .setParameterList("tags2", excludeTag)
                .list();
        } else {
            tmpList = new ArrayList<>();
        }
        return tmpList;
    }
    
    private List<Long> getTaggedIDWithAlbums() {
        if (albumID <= 0) return getTaggedIDWOAlbums();

        // Это не костыль, а задел на будущее, если вдруг кому будет нужен поиск по нескольким альбомам.
        final ArrayList<DSAlbum> alb = new ArrayList<>();
        final DSAlbum dsa = (DSAlbum) hibSession
                        .createCriteria(DSAlbum.class)
                        .add(Restrictions.eq("albumID", albumID))
                        .uniqueResult();
        if (dsa == null) return getTaggedIDWOAlbums();
        alb.add(dsa);
        
        final List<Long> tmpList;
        if ((searchTag != null) && (excludeTag != null)) {
            tmpList = hibSession.createQuery("SELECT r.imageID FROM DSImage r WHERE r.imageID IN (:tags1) AND r.imageID NOT IN (:tags2) AND r.imageID IN (:alb1)")
                .setParameterList("tags1", searchTag)
                .setParameterList("tags2", excludeTag)
                .setParameterList("alb1", alb)
                .list();
        } else if ((searchTag != null) && (excludeTag == null)) {
            tmpList = hibSession.createQuery("SELECT r.imageID FROM DSImage r WHERE r.imageID IN (:tags1) AND r.imageID IN (:alb1)")
                .setParameterList("tags1", searchTag)
                .setParameterList("alb1", alb)
                .list();
        } else if ((searchTag == null) && (excludeTag != null)) {
            tmpList = hibSession.createQuery("SELECT r.imageID FROM DSImage r WHERE r.imageID NOT IN (:tags2) AND r.imageID IN (:alb1)")
                .setParameterList("tags2", excludeTag)
                .setParameterList("alb1", alb)    
                .list();
        } else {
            tmpList = new ArrayList<>();
        }
        return tmpList;
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
            case InAlbum:
                tmpList = hibSession
                        .createCriteria(DSImage.class)
                        .setProjection(Projections.property("imageID"))
                        .addOrder(Order.asc("imageID"))
                        .createCriteria("albums")
                        .add(Restrictions.eq("albumID", albumID))
                        .list();
                break;
            case TagList:
                tmpList = getTaggedIDWOAlbums();
                break;
            case TagsAndAlbums:
                tmpList = getTaggedIDWithAlbums();
                break;
            default:
                tmpList = getAllID();
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
        if (imgInAlbum == null) imgInAlbum = new DSImageIDListCache(ImgType.InAlbum);
        return imgInAlbum;
    }
    
    public static DSImageIDListCache getTagged() {
        if (imgTagged == null) imgTagged = new DSImageIDListCache(ImgType.TagList);
        return imgTagged;
    }
    
    public static DSImageIDListCache getTaggedAnDAlbums() {
        if (imgTAndA == null) imgTAndA = new DSImageIDListCache(ImgType.TagsAndAlbums);
        return imgTAndA;
    }
    
    public static void reloadAllStatic() {
        getAll().reload();
        getNotagged().reload();
        getWOAlbums().reload();
        getInAlbum().reload();
        getTagged().reload();
        getTaggedAnDAlbums().reload();
    }
}
