package jnekouilib.fragment;

import javafx.scene.input.MouseEvent;

public interface FragmentListItemActionListener<T> {
    public void OnItemClick(T object, FragmentListItem fli, MouseEvent me);
}
