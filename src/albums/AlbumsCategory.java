package albums;

import dataaccess.DBWrapper;

public class AlbumsCategory {
    public String name;
    public long ID;
    public int state;

    public AlbumsCategory(String n, long id, int st) {
        name    = n.trim();
        ID      = id;
        state   = st;
    }

    public int saveChanges() {
        return DBWrapper.saveAlbumsCategoryChanges(name, state, ID);
    }
}
