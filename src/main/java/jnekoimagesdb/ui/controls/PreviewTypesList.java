package jnekoimagesdb.ui.controls;

import jnekoimagesdb.core.img.XImg;
import jnekoimagesdb.domain.DSPreviewSize;
import jnekoimagesdb.ui.controls.elements.GUIActionListener;
import jnekoimagesdb.ui.controls.elements.SButton;
import jnekoimagesdb.ui.controls.elements.SEVBox;
import jnekoimagesdb.ui.controls.elements.SFHBox;
import jnekoimagesdb.ui.controls.elements.SFLabel;
import jnekoimagesdb.ui.controls.elements.SNumericTextField;
import jnekoimagesdb.ui.controls.elements.SScrollPane;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import jnekoimagesdb.ui.GUITools;
import jnekoimagesdb.ui.controls.dialogs.XDialogImgCacheRebuild;
import jnekoimagesdb.ui.controls.elements.ElementsIDCodes;
import jnekoimagesdb.ui.controls.elements.STabTextButton;
import jnekoimagesdb.ui.md.dialogs.YesNoBox;
import jnekoimagesdb.ui.md.dialogs.YesNoBoxResult;

public class PreviewTypesList extends SEVBox {
    protected static interface TypesListItemActionListener {
        void refreshNeed(DSPreviewSize d);
    }
    
    protected static class TypesListItem extends SFHBox {        
        private DSPreviewSize 
                thisElement = null;
        
        private TypesListItemActionListener
                alOut = null;
        
        private final GUIActionListener 
                al = (evCode, ID) -> {
                    switch(ID) {
                        case buttonSetAsPrimary:
                            if (XImg.getPSizes().getPrimaryPreviewSize() != null)
                                if (thisElement.equals(XImg.getPSizes().getPrimaryPreviewSize())) {
                                    break;
                                }
                            XImg.getPSizes().setPrimary(thisElement);
                            XImg.wipeFSCache();
                            alOut.refreshNeed(thisElement);
                            break;
                        case buttonDelete:
                            //final XDialogYesNoBox d = new XDialogYesNoBox();
                            //d.showYesNo("Точно удалить секцию?", "Будут удалены все сгенерированные превью для данной секции!");
                            if (YesNoBox.show("Точно удалить секцию? Будут удалены все сгенерированные превью для данной секции!", "Удалить", "Отмена") == YesNoBoxResult.YES) {
                                XImg.getPSizes().deletePreviewSize(thisElement);
                                alOut.refreshNeed(thisElement);
                            }
                            break;
                    }
        };
        
        public TypesListItem(DSPreviewSize pds, TypesListItemActionListener alx) {
            super(4, 120, 9999, 39, 39, "prevsz_container");
            this.setAlignment(Pos.CENTER);
            thisElement = pds;
            alOut = alx;
            this.setAlignment(Pos.CENTER);
            this.getChildren().add((pds.isPrimary()) ? new ImageView(GUITools.loadIcon("options-2-32")) : new ImageView(GUITools.loadIcon("options-1-32")));
            this.getChildren().add(new SFLabel(pds.getPrevName(), 128, 9999, 32, 32, "label_left", "TypesListItem"));
            this.getChildren().add(new SButton(GUITools.loadIcon("delete-32"), ElementsIDCodes.buttonDelete, 32, al, "button_prevsz_el"));
            this.getChildren().add(new SButton(GUITools.loadIcon("selected-32"), ElementsIDCodes.buttonSetAsPrimary, 32, al, "button_prevsz_el"));
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
            itemContainer = new SEVBox(1);
    
    private final SNumericTextField
            hf = new SNumericTextField(ElementsIDCodes.textUnknown, 70, 32, null), 
            wf = new SNumericTextField(ElementsIDCodes.textUnknown, 70, 32, null);
    
    private boolean 
            isSqared = false;
   
    
    private final ImageView
            imgButton = new ImageView(GUITools.loadIcon("unselected2-16"));
    
    private final SButton
            btn = new SButton(GUITools.loadIcon("unselected2-16"), ElementsIDCodes.buttonUnknown, 32, (c, d) -> {
                    isSqared = !isSqared;
                    imgButton.setImage((isSqared) ? GUITools.loadIcon("selected-16") : GUITools.loadIcon("unselected2-16")); 
                }, "button_pts");
    
    public PreviewTypesList() {
        super("svbox_sett_container_red");
        itemsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        itemsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        itemsScroll.setFitToHeight(true);
        itemsScroll.setFitToWidth(true);
        itemsScroll.setContent(itemContainer);
        btn.setGraphic(imgButton);
        
        wf.max = 800;
        wf.min = 32;
        hf.max = 800;
        hf.min = 32;
        
        addNewItem.getChildren().addAll(new SFLabel("Ширина:", 64, 64, 32, 32, "label", "TypesListItem"), 
                wf,
                new SFLabel("Высота:", 64, 64, 32, 32, "label", "TypesListItem"),
                hf,
                new SFLabel("Обрезка:", 64, 64, 32, 32, "label", "TypesListItem"),
                btn,
                GUITools.getSeparator(),
                new SButton(GUITools.loadIcon("plus-32"), ElementsIDCodes.buttonUnknown, 32, (c, d) -> {
                    if (((wf.getLongValue() >= 64) && (wf.getLongValue() <= 800))
                            && ((hf.getLongValue() >= 64) && (hf.getLongValue() <= 800))) {
                        final StringBuilder sb = new StringBuilder();
                        sb
                                .append("Preview_")
                                .append(wf.getLongValue())
                                .append("x")
                                .append(hf.getLongValue())
                                .append("_")
                                .append((isSqared) ? "SQ" : "NS");
                        
                        XImg.getPSizes().addPreviewSize(sb.substring(0), wf.getLongValue(), hf.getLongValue(), isSqared);
                        wf.setText("0");
                        hf.setText("0");
                        isSqared = false;
                        imgButton.setImage(GUITools.loadIcon("unselected2-16"));
                        refresh();
                    }
                }, "button_pts_add")
        );
        GUITools.setMaxSize(addNewItem, 9999, 32);
        
        this.getChildren().addAll(
                new SFLabel("Настройка формата превью.", 64, 9999, 20, 20, "label_darkred", "TypesListItem"),
                new SFLabel("При добавлении, удалении или смене текущего формата превью может потребоваться перестроение БД, "
                        + "что может занять очень продолжительное время. Размер превью может быть от 64 до 800 пикселей по ширине и от 64 до 800 пикселей по высоте.", 
                        64, 9999, 44, 44, "label_darkred_small", "TypesListItem"),
                addNewItem, 
                itemsScroll,
                GUITools.getHSeparator(4),
                new STabTextButton("Перестроить кэш...", ElementsIDCodes.buttonUnknown, 150, 32, (x, y) -> {
                    XDialogImgCacheRebuild.get().startRebuild();
                })
                );
    }
    
    public final void refresh() {
        itemContainer.getChildren().clear();
        XImg.getPSizes().getPreviewSizes().forEach((d) -> {
            itemContainer.getChildren().add(new TypesListItem(d, al2));
        });
    }
}
