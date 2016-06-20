package jnekoimagesdb.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javafx.scene.image.Image;
import jnekoimagesdb.core.img.XImgDatastore;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.LoggerFactory;

// Краткий комент, зачем это надо вообще.
//
// Суть такова: id в БД могут иметь окна от удаления элементов, то есть SELECT * FROM table WHERE id>x AND id<y; работать корректно не будет.
// Делать быструю навигацию по базе в 100к+ картинок без такого кеша затруднительно, ибо limit XXXXXX,YY работает крайне медленно во встраиваемых решениях.
// По-другому быструю страничную навигацию без повторов и окон не сделать. Да, оно жрет память, но что поделаешь - тут или быстро и много памяти, или медленно, но мало памяти.
//
// Набор статических методов, дублирующих функционал "getStatic()", нужен исключительно для удобочитаемости кода.

public class DSImageIDListCache {
    private final org.slf4j.Logger 
        logger = LoggerFactory.getLogger(DSImageIDListCache.class);
        
    public static enum ImgType {
        All, Notagged, NotInAnyAlbum, InAlbum, TagList, TagsAndAlbums
    }
    
    private static final Map<ImgType, DSImageIDListCache>
            caches = new HashMap<>();

    private Set<DSTag> 
            searchTag = null,
            excludeTag = null;
    
    private final Session hibSession;
    private final ImgType it;
    
    private int[] arrayImg = null;
    private long albumID = 0;
    private int currentIndex = 0;

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
            tmpList = hibSession.createQuery("SELECT r.imageID FROM DSImage r WHERE r.imageID IN (:tags1) AND r.imageID NOT IN (:tags2) ORDER BY r.imageID DESC")
                .setParameterList("tags1", searchTag)
                .setParameterList("tags2", excludeTag)
                .list();
        } else if ((searchTag != null) && (excludeTag == null)) {
            tmpList = hibSession.createQuery("SELECT r.imageID FROM DSImage r WHERE r.imageID IN (:tags1) ORDER BY r.imageID DESC")
                .setParameterList("tags1", searchTag)
                .list();
        } else if ((searchTag == null) && (excludeTag != null)) {
            tmpList = hibSession.createQuery("SELECT r.imageID FROM DSImage r WHERE r.imageID NOT IN (:tags2) ORDER BY r.imageID DESC")
                .setParameterList("tags2", excludeTag)
                .list();
        } else {
            tmpList = new ArrayList<>();
        }
        return tmpList;
    }
    
    private List<Long> getTaggedIDWithAlbums() {
        if (albumID <= 0) return getTaggedIDWOAlbums();

        // Это не костыль, а задел на будущее, если вдруг кому будет нужен поиск по нескольким альбомам. Мне такой функционал пока кажется излишним.
        final ArrayList<DSAlbum> alb = new ArrayList<>();
        final DSAlbum dsa = (DSAlbum) hibSession
                        .createCriteria(DSAlbum.class)
                        .add(Restrictions.eq("albumID", albumID))
                        .uniqueResult();
        if (dsa == null) return getTaggedIDWOAlbums();
        alb.add(dsa);
        
        final List<Long> tmpList;
        if ((searchTag != null) && (excludeTag != null)) {
            tmpList = hibSession.createQuery("SELECT r.imageID FROM DSImage r WHERE r.imageID IN (:tags1) AND r.imageID NOT IN (:tags2) AND r.imageID IN (:alb1) ORDER BY r.imageID DESC")
                .setParameterList("tags1", searchTag)
                .setParameterList("tags2", excludeTag)
                .setParameterList("alb1", alb)
                .list();
        } else if ((searchTag != null) && (excludeTag == null)) {
            tmpList = hibSession.createQuery("SELECT r.imageID FROM DSImage r WHERE r.imageID IN (:tags1) AND r.imageID IN (:alb1) ORDER BY r.imageID DESC")
                .setParameterList("tags1", searchTag)
                .setParameterList("alb1", alb)
                .list();
        } else if ((searchTag == null) && (excludeTag != null)) {
            tmpList = hibSession.createQuery("SELECT r.imageID FROM DSImage r WHERE r.imageID NOT IN (:tags2) AND r.imageID IN (:alb1) ORDER BY r.imageID DESC")
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
                        .createQuery("SELECT r.imageID FROM DSImage r WHERE r.tags IS EMPTY ORDER BY r.imageID ASC")
                        .list();
                logger.info("Notagged: "+tmpList.size());
                break;
            case NotInAnyAlbum:
                tmpList = hibSession
                        .createQuery("SELECT r.imageID FROM DSImage r WHERE r.albums IS EMPTY ORDER BY r.imageID ASC")
                        .list();
                logger.info("NotInAnyAlbum: "+tmpList.size());
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
                logger.info("ALL: "+tmpList.size());
                break;
        }

        final int count = tmpList.size();
        arrayImg = new int[count];
        for (int i=0; i<count; i++) {
            arrayImg[i] =  tmpList.get(i).intValue();
        }
    }
    
    public final DSImage getDSImage(int num) {
        if (arrayImg == null) throw new RuntimeException("DSImageIDListCache: Cache array is empty. Do reload() first.");
        if ((num >= arrayImg.length) || (num < 0)) return null;
        currentIndex = num;
        long imageID = arrayImg[num];
        final DSImage dsi = (DSImage) hibSession
                        .createCriteria(DSImage.class)
                        .add(Restrictions.eq("imageID", imageID))
                        .uniqueResult();
        return dsi;
    }
    
    public final DSImage getNextDSImage() {
        currentIndex++;
        return getDSImage(currentIndex);
    }
    
    public final DSImage getPrevDSImage() {
        currentIndex--;
        return getDSImage(currentIndex);
    }
    
    public final Image getImage(int num) {
        final DSImage dsi = getDSImage(num);
        if (dsi == null) return null;
            try {
                final Image img = XImgDatastore.getImage(dsi.getMD5());
                return img;
            } catch (IOException ex) {
                return null;
            }
    }
    
    public final Image getNextImage() {
        currentIndex++;
        return getImage(currentIndex);
    }

    public final Image getPrevImage() {
        currentIndex--;
        return getImage(currentIndex);
    }
    
    public long getID(int num) {
        if (arrayImg == null) throw new RuntimeException("DSImageIDListCache: Cache array is empty. Do reload() first.");
        if ((num >= arrayImg.length) || (num < 0)) return -1;
        currentIndex = num;
        return arrayImg[currentIndex];
    }
    
    public long getIDReverse(int num) {
        if (arrayImg == null) throw new RuntimeException("DSImageIDListCache: Cache array is empty. Do reload() first.");
        if ((num >= arrayImg.length) || (num < 0)) return -1;
        currentIndex = arrayImg.length - num - 1;
        return arrayImg[currentIndex];
    }
    
    public int getCount() {
        if (arrayImg == null) throw new RuntimeException("DSImageIDListCache: Cache array is empty. Do reload() first.");
        return arrayImg.length;
    }
    
    public static DSImageIDListCache getStatic(ImgType it) {
        if (!caches.containsKey(it)) caches.put(it, new DSImageIDListCache(it));
        return caches.get(it);
    }
    
    public static DSImageIDListCache getAll() {
        return getStatic(ImgType.All);
    }
    
    public static DSImageIDListCache getNotagged() {
        return getStatic(ImgType.Notagged);
    }
    
    public static DSImageIDListCache getWOAlbums() {
        return getStatic(ImgType.NotInAnyAlbum);
    }
    
    public static DSImageIDListCache getInAlbum() {
        return getStatic(ImgType.InAlbum);
    }
    
    public static DSImageIDListCache getTagged() {
        return getStatic(ImgType.TagList);
    }
    
    public static DSImageIDListCache getTaggedAnDAlbums() {
        return getStatic(ImgType.TagsAndAlbums);
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
