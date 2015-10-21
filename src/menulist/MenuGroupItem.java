package menulist;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class MenuGroupItem extends VBox {
    /*
        Этот модуль притащен из проекта тестовой софтины для робота. Тут все надо рефакторить, ибо все криво.
    */
    private final MenuLabel 
            header      = new MenuLabel(),
            noitems     = new MenuLabel();

    private Label
            selLabel    = null;

    private final javafx.scene.layout.VBox 
            spoiler     = new javafx.scene.layout.VBox(),
            headerCont  = new javafx.scene.layout.VBox();

    private boolean 
            isDarkTheme = true,
            isExpanded  = true;

    private String 
            bgColor = "#262",
            fontColor = "#fff",
            groupFontColor = "#ff0",
            selectedFontColor = "#ff0",
            GID = "";

    private final Map<String, MenuLabel> 
            items = new LinkedHashMap<>();

    private final Map<String, ImageView> 
            itemIcons = new LinkedHashMap<>();

    private final String 
            subItemStyle    = "-fx-font-weight:bold; -fx-font-size: 10px; -fx-label-padding: 3px 4px 3px 21px; ",
            noItemsStyle    = "-fx-font-style:italic; -fx-font-size: 10px; -fx-label-padding: 3px 4px 3px 21px; ",
            headerItemStyle = "-fx-font-weight:bold; -fx-font-size: 12px; -fx-label-padding: 5px 4px 5px 4px; ";

    private MenuGroupItemActionListener 
            actionListener = null;

    private final MenuGroupItem
            THIS = this;

    @SuppressWarnings("FieldMayBeFinal")
    private ImageView 
            itemIcon10x10 = new ImageView(new Image(new File("./icons/arrow2.png").toURI().toString())),
            groupIcon16x16 = new ImageView(new Image(new File("./icons/arrow1.png").toURI().toString()));

    @SuppressWarnings("Convert2Lambda")
    private final EventHandler<MouseEvent> onHeaderClick = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {
                if (event.getClickCount() == 1) {
                    isExpanded = !isExpanded;
                    if (actionListener != null) actionListener.OnExpandGroup(isExpanded, THIS);
                    _refreshItems();
                }
            }
        }
    };

    @SuppressWarnings("Convert2Lambda")
    private final EventHandler<MouseEvent> onMouseEvent = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent event) {
            if (event.getEventType() == MouseEvent.MOUSE_ENTERED) {
                unselectAll();

                if (event.getSource() instanceof MenuLabel) {
                    ((MenuLabel) event.getSource()).setStyle(subItemStyle + "-fx-text-fill:" + selectedFontColor + "; -fx-underline:true; -fx-background-color:" + bgColor + ";");
                    if (actionListener != null) actionListener.OnItemHover((MenuLabel) event.getSource());
                }

                if (selLabel != null) selLabel.setStyle(subItemStyle + "-fx-text-fill:" + selectedFontColor + "; -fx-background-color:" + bgColor + ";");
            }

            if (event.getEventType() == MouseEvent.MOUSE_EXITED) {
                unselectAll();
                if (selLabel != null) selLabel.setStyle(subItemStyle + "-fx-text-fill:" + selectedFontColor + "; -fx-background-color:" + bgColor + ";");
            }

            if (event.getEventType() == MouseEvent.MOUSE_CLICKED) {
                if (event.getClickCount() == 1) {
                    if (event.getSource() instanceof MenuLabel) {
                        ((MenuLabel) event.getSource()).setStyle(subItemStyle + "-fx-text-fill:" + selectedFontColor + "; -fx-underline:true; -fx-background-color:" + bgColor + ";");
                        selLabel = ((MenuLabel) event.getSource());
                    }

                    if (actionListener != null) actionListener.OnItemClicked((MenuLabel) event.getSource());
                }
            }
        }
    };

    public final void setGID(String id) {
        GID = id;
    }

    public final String getGID() {
        return GID;
    }

    private String _toWebColor(javafx.scene.paint.Color c) {
        return
                "#" +
                Long.toHexString((long)(c.getRed() * 255d)) +
                Long.toHexString((long)(c.getGreen() * 255d)) +
                Long.toHexString((long)(c.getBlue() * 255d));
    }

    public MenuGroupItem() {
        super();
        this.setMinWidth(0);
        this.setMinHeight(0);
        this.setMaxWidth(Integer.MAX_VALUE);
        this.setPrefWidth(Integer.MAX_VALUE);
        setStyle("-fx-background-color:#9999FF;");

        setBgColor(bgColor);
        setBgOpacity(1d);
        noitems.setText("No items");
        header.setText("No items");

        headerCont.setOnMouseClicked(onHeaderClick);

        headerCont.getChildren().add(header);
        spoiler.getChildren().add(noitems);
        this.getChildren().addAll(headerCont, spoiler);
    }

    private void _refreshItems() {
        if (items.isEmpty()) {
            noitems.setVisible(true);
        } else {
            noitems.setVisible(false);
            spoiler.getChildren().clear();
            if (!isExpanded) return;
            final Set<String> s = items.keySet();
            s.stream().forEach((ks) -> {
                items.get(ks).setStyle(subItemStyle + "-fx-text-fill:" + fontColor + "; -fx-background-color:" + bgColor + ";");
                spoiler.getChildren().add(items.get(ks));
            });
        }
    }

    public final void Commit() {
        _refreshItems();
    }

    public final void setExpanded(boolean e) {
        if (actionListener != null) actionListener.OnExpandGroup(e, THIS);
        spoiler.setVisible(e);
    }

    public void unselectAll() {
        final Set<String> s = items.keySet();
        s.stream().forEach((ks) -> {
            items.get(ks).setStyle(subItemStyle + "-fx-text-fill:" + fontColor + "; -fx-background-color:" + bgColor + ";");
        });
    }

    public final void addItem(String ID, MenuLabel l) {
        if (ID == null) throw new NullPointerException("Cannot add item with NULL id.");
        if (l == null)  throw new NullPointerException("Cannot add item with NULL object.");
        if (items.containsKey(ID)) throw new NullPointerException("Item with ID [" + ID + "] already exist.");
        items.put(ID, l);
    }

    public final void addLabel(String ID, String text) {
        final ImageView iw = new ImageView(new Image(new File("./icons/arrow2.png").toURI().toString()));
        itemIcons.put(ID, iw);

        final MenuLabel l = new MenuLabel();
        l.setText(text);
        l.setGraphic(itemIcons.get(ID));
        l.setOnMouseEntered(onMouseEvent);
        l.setOnMouseExited(onMouseEvent);
        l.setOnMouseClicked(onMouseEvent);
        l.setID(ID);
        l.setGID(GID);
        addItem(ID, l);
    }

    public final void removeItem(String ID) {
        itemIcons.remove(ID);
        items.remove(ID);
    }

    public int getElementsCount() {
        return items.size();
    }

    public final void clearAll() {
        itemIcons.clear();
        items.clear();
    }

    public final MenuLabel getLabel(String ID) {
        return items.get(ID);
    }

    public final void setLabelIcon(String ID, ImageView icon) {
        items.get(ID).setGraphic(icon);
        itemIcons.put(ID, icon);
    }

    public final void setLabelText(String ID, String text) {
        items.get(ID).setText(text);
    }

    public void setActionListener(MenuGroupItemActionListener al) {
        actionListener = al;
    }

    public final void setBgOpacity(double o) {
        spoiler.setOpacity(o);
        noitems.setOpacity(o);
        _refreshItems();
    }

    public final void setBgColor(String color) {
        if (color.startsWith("#") == false) color = "#" + color;
        bgColor = color;
        _setColor();
    }

    public final void setFontColorForItems(String color) {
        if (color.startsWith("#") == false) color = "#" + color;
        fontColor = color;
        _setColor();
    }

    public final void setFontColorForSelectedItems(String color) {
        if (color.startsWith("#") == false) color = "#" + color;
        selectedFontColor = color;
        _setColor();
    }

    public final void setFontColorForGroupHeader(String color) {
        if (color.startsWith("#") == false) color = "#" + color;
        groupFontColor = color;
        _setColor();
    }

    public void setGroupTitle(String t) {
        header.setText(t); 
    }

    public void setGroupIcon(ImageView icon) {
        if (icon != null) groupIcon16x16 = icon;
    }

    private void _setColor() {
        noitems.setStyle(noItemsStyle + "-fx-text-fill:" + fontColor + "; -fx-background-color:" + bgColor + ";");
        noitems.setGraphic(itemIcon10x10);

        spoiler.setStyle("-fx-background-color:" + bgColor + ";");

        final javafx.scene.paint.Color cx = (isDarkTheme) ? javafx.scene.paint.Color.web(bgColor).brighter() : javafx.scene.paint.Color.web(bgColor).darker();
        header.setStyle(headerItemStyle + "-fx-text-fill:" + groupFontColor + "; -fx-background-color:" + _toWebColor(cx) + ";");
        header.setGraphic(groupIcon16x16);
        headerCont.setStyle(headerItemStyle + "-fx-background-color:" + _toWebColor(cx) + ";");
    }

    public void setDarkTheme(boolean dark) {
        isDarkTheme = dark;
        setBgColor(bgColor);
    }
}
