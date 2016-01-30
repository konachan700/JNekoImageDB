
package jnekoimagesdb.ui.controls.menulist;

public interface MenuGroupItemActionListener {
    public void OnExpandGroup(boolean expanded, MenuGroupItem item);
    public void OnItemHover(MenuLabel l);
    public void OnItemClicked(MenuLabel l);
}
