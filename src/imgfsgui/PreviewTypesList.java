package imgfsgui;

import datasources.DSPreviewSize;
import dialogs.DialogYesNoBox;
import imgfs.ImgFS;
import imgfsgui.elements.GUIActionListener;
import imgfsgui.elements.SButton;
import imgfsgui.elements.SEVBox;
import imgfsgui.elements.SFHBox;
import imgfsgui.elements.SFLabel;
import imgfsgui.elements.SNumericTextField;
import imgfsgui.elements.SScrollPane;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import jnekoimagesdb.GUITools;

public class PreviewTypesList extends SEVBox {
    public static final Image 
            IMG32_SETPRIMARY = GUITools.loadIcon("selected-32"),
            IMG32_DELETE = GUITools.loadIcon("delete-32"),
            IMG32_NORMAL = GUITools.loadIcon("options-1-32"),
            IMG32_ADD = GUITools.loadIcon("plus-32"),
            IMG32_SQUARED = GUITools.loadIcon("selected-16"),
            IMG32_NONSQUARED = GUITools.loadIcon("unselected2-16"),
            IMG32_SELECTED = GUITools.loadIcon("options-2-32");

    protected interface TypesListItemActionListener {
        void refreshNeed(DSPreviewSize d);
    }
    
    protected class TypesListItem extends SFHBox {
        public static final int 
                BTN_DEL = 1,
                BTN_SETPRIMARY = 2;
        
        private DSPreviewSize 
                thisElement = null;
        
        private TypesListItemActionListener
                alOut = null;
        
        private final GUIActionListener 
                al = (int evCode, int ID) -> {
                    switch(evCode) {
                        case BTN_SETPRIMARY:
                            ImgFS.getPSizes().setPrimary(thisElement);
                            alOut.refreshNeed(thisElement);
                            break;
                        case BTN_DEL:
                            final DialogYesNoBox d = new DialogYesNoBox();
                            int res = d.showYesNo("Точно удалить секцию?\r\nТакже будут удалены все сгенерированные превью для данной секции!");
                            if (res == DialogYesNoBox.SELECT_YES) {
                                ImgFS.getPSizes().deletePreviewSize(thisElement);
                                alOut.refreshNeed(thisElement);
                            }
                            break;
                    }
        };
        
        public TypesListItem(DSPreviewSize pds, TypesListItemActionListener alx) {
            super(4, 120, 9999, 32, 32);
            thisElement = pds;
            alOut = alx;
            this.setAlignment(Pos.CENTER);
            this.getChildren().add((pds.isPrimary()) ? new ImageView(IMG32_SELECTED) : new ImageView(IMG32_NORMAL));
            this.getChildren().add(new SFLabel(pds.getPrevName(), 128, 9999, 32, 32, "label", "TypesListItem"));
            this.getChildren().add(new SButton(IMG32_DELETE, BTN_DEL, 32, al));
            this.getChildren().add(new SButton(IMG32_SETPRIMARY, BTN_SETPRIMARY, 32, al));
        }
    }
    
    private final TypesListItemActionListener
            al2 = (c) -> {
                refresh();
            };
    
    private final SFHBox
            addNewItem = new SFHBox(4, 120, 9999, 32+12, 32+12, "AddNewAlbumElement");
    
    private final SScrollPane
            itemsScroll = new SScrollPane();
    
    private final SEVBox
            itemContainer = new SEVBox(4);
    
    private final SNumericTextField
            hf = new SNumericTextField(0, 70, 32, null), 
            wf = new SNumericTextField(0, 70, 32, null);
    
    private boolean 
            isSqared = false;
    
    private final ImageView
            imgButton = new ImageView(IMG32_NONSQUARED);
    
    private final SButton
            btn = new SButton(IMG32_NONSQUARED, 0, 32, (c, d) -> {
                    isSqared = !isSqared;
                    imgButton.setImage((isSqared) ? IMG32_SQUARED : IMG32_NONSQUARED); 
                }, "button_pts");
    
    public PreviewTypesList() {
        super();
        itemsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        itemsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        itemsScroll.setFitToHeight(true);
        itemsScroll.setFitToWidth(true);
        itemsScroll.setContent(itemContainer);
        btn.setGraphic(imgButton);
        
        wf.max = 640;
        wf.min = 32;
        hf.max = 480;
        hf.min = 32;
        
        addNewItem.getChildren().addAll(
                new SFLabel("Ширина:", 64, 64, 32, 32, "label", "TypesListItem"), 
                wf,
                new SFLabel("Высота:", 64, 64, 32, 32, "label", "TypesListItem"),
                hf,
                new SFLabel("Обрезка:", 64, 64, 32, 32, "label", "TypesListItem"),
                btn,
                GUITools.getSeparator(),
                new SButton(IMG32_ADD, 0, 32, (c, d) -> {
                    
                }, "button_pts_add")
        );
        GUITools.setMaxSize(addNewItem, 9999, 32);
        
        this.getChildren().addAll(addNewItem, itemsScroll);
    }
    
    public void refresh() {
        itemContainer.getChildren().clear();
        ImgFS.getPSizes().getPreviewSizes().forEach((d) -> {
            itemContainer.getChildren().add(new TypesListItem(d, al2));
        });
    }
}
