package albums;

import dataaccess.DBWrapper;

@Deprecated
public class AlbumsCategory {
    public String name;
    public long ID;
    public int state;
    public long parent;

    public AlbumsCategory(String n, long id, int st) {
        name    = n.trim();
        ID      = id;
        state   = st;
        parent  = 0;
    }
    
    public AlbumsCategory(String n, long id, int st, long p) {
        name    = n.trim();
        ID      = id;
        state   = st;
        parent  = p;
    }

    public int saveChanges() {
        return DBWrapper.saveAlbumsCategoryChanges(name, state, ID);
    }
}
