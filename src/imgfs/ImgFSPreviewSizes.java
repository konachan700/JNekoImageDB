package imgfs;

import datasources.DSPreviewSize;
import datasources.HibernateUtil;
import java.util.ArrayList;
import org.hibernate.Session;

public class ImgFSPreviewSizes {
    private static final ArrayList<DSPreviewSize> 
            previewSizes = new ArrayList<>();
    
    public ImgFSPreviewSizes() {}
    
    public void refreshPreviewSizes() {
        previewSizes.clear();
        previewSizes.addAll(HibernateUtil.getCurrentSession()
                .createCriteria(DSPreviewSize.class)
                .list());
    }
    
    public boolean isPreviewSizesEmpty() {
        if (previewSizes.isEmpty()) return true;
        return previewSizes.stream().noneMatch((el) -> (el.isPrimary()));
    }
    
    public DSPreviewSize getPrimaryPreviewSize() {
        if (previewSizes.isEmpty()) return null;
        for (DSPreviewSize ps : previewSizes) { if (ps.isPrimary()) return ps; }
        return null;
    }
    
    public DSPreviewSize get(int index) {
        return previewSizes.get(index);
    }
    
    public ArrayList<DSPreviewSize> getPreviewSizes() {
        return previewSizes;
    }
    
    public void setPrimary(DSPreviewSize pds) {
        final Session s = HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction(s);
        previewSizes.stream().forEach((ps) -> { 
            ps.setPrimary(false);
            s.save(ps);
        });
        pds.setPrimary(true);
        s.save(pds);
        HibernateUtil.commitTransaction(s);
    }
    
    public void deletePreviewSize(DSPreviewSize pds) {
        final Session s = HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction(s);
        s.delete(pds);
        HibernateUtil.commitTransaction(s);
    }
    
    public void addPreviewSize(String _prevName, long _width, long _height, boolean _squared) {
        final DSPreviewSize ps = new DSPreviewSize(_prevName, _width, _height, _squared);
        final Session s = HibernateUtil.getCurrentSession();
        HibernateUtil.beginTransaction(s);
        s.save(ps);
        HibernateUtil.commitTransaction(s);
    }
}
