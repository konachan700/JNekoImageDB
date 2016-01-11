package albums;

@Deprecated
public interface ASDElementActionListener {
    void OnCheck(Long id, AlbumsListElement e);
    void OnUncheck(Long id, AlbumsListElement e);
    void OnItemClick(Long id, AlbumsListElement e);
    void OnSave(Long id, AlbumsListElement e, String newTitle);  
}
