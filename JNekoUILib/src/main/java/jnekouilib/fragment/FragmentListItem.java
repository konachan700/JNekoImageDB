package jnekouilib.fragment;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import jnekouilib.anno.UIListItem;
import jnekouilib.anno.UIListItemHeader;
import jnekouilib.anno.UIListItemRightText;
import jnekouilib.anno.UIListItemSubtitle;
import jnekouilib.anno.UIListItemTextLine;
import jnekouilib.editor.Editor;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import jiconfont.javafx.IconNode;

public class FragmentListItem<T> extends HBox {
    private final T
            myObject;
    
    private final Label 
            mainTitle = new Label(),
            subTitle  = new Label(),
            textLine  = new Label(),
            rightText = new Label();
    
    private boolean 
            selected = false;
    
    private final IconNode 
            bigIcon   = new IconNode();
    
    private final FragmentListItemActionListener<T>
            actionListener;
    
    public FragmentListItem(T object, String icon, FragmentListItemActionListener<T> al) {
        myObject = object;
        actionListener = al;
        
        this.getStyleClass().addAll(Editor.maxWidthStyle, "FLI_this");
        this.setAlignment(Pos.CENTER);
        this.setOnMouseClicked(value -> {
            actionListener.OnItemClick(myObject, this, value); 
        });
        
        bigIcon.getStyleClass().addAll("FLI_bigIcon", icon);
        mainTitle.getStyleClass().addAll("FLI_mainTitle");
        subTitle.getStyleClass().addAll("FLI_subTitle");
        textLine.getStyleClass().addAll("FLI_textLine");
        rightText.getStyleClass().addAll("FLI_rightText");
        
        rightText.setAlignment(Pos.CENTER_RIGHT);
    }
    
    public FragmentListItem(T object, FragmentListItemActionListener<T> al) {
        this(object, "FLI_def_icon", al);
    }
    
    private VBox createVBox() {
        final VBox sep = new VBox();
        sep.getStyleClass().addAll(Editor.maxWidthStyle);
        return sep;
    }
    
    public String getTitle() {
        return mainTitle.getText();
    }
    
    private HBox createHBox() {
        final HBox sep = new HBox();
        sep.getStyleClass().addAll(Editor.maxWidthStyle);
        return sep;
    }
    
    private void createFile() {
        Path path;
        File file;
        
        if (getMyObject() instanceof Path) {
            path = (Path) getMyObject();
            file = path.toFile();
        } else if (getMyObject() instanceof File) {
            file = (File) getMyObject();
        } else {
            return;
        }

        final String 
                rr = (file.canRead()) ? "R" : "",
                rw = (file.canWrite()) ? "W" : "",
                re = (file.canExecute()) ? "E" : "";

        mainTitle.setText(file.getName().trim().isEmpty() ? file.getAbsolutePath() : file.getName());
        bigIcon.getStyleClass().clear();
        if (file.isDirectory()) {
            subTitle.setText("Directory");
            rightText.setText(rr + rw);
            bigIcon.getStyleClass().addAll("FLI_bigIcon", "FLI_dir_icon");
        } else {
            subTitle.setText("File size: "+file.length()+" bytes");
            rightText.setText(rr + rw + re);
//            try {
//                String mimeType = Magic.getMagicMatch(file, false).getMimeType();
//                textLine.setText(mimeType);
//            } catch (MagicParseException | MagicMatchNotFoundException | MagicException ex) {
//                //Logger.getLogger(FragmentListItem.class.getName()).log(Level.SEVERE, null, ex);
//            }
            bigIcon.getStyleClass().addAll("FLI_bigIcon", "FLI_file_icon");
        }
    }
    
    public String getIndexedData() {
        return mainTitle.getText() + " " + subTitle.getText() + " " + textLine.getText() + " " + rightText.getText();
    }
    
    public void create() {
        if (getMyObject() == null) return;
        if (getMyObject() instanceof String) {
            final String text = (String) getMyObject();
            mainTitle.setText(text);
            this.getChildren().addAll(bigIcon, mainTitle);
        } else if (getMyObject() instanceof Number) {
            final Number val = (Number) getMyObject();
            mainTitle.setText("#"+val.toString());
            this.getChildren().addAll(bigIcon, mainTitle); 
        } else {
            if ((getMyObject() instanceof Path) || (getMyObject() instanceof File)) {
                createFile();
            } else {
                if (! myObject.getClass().isAnnotationPresent(UIListItem.class)) return;

                final Method[] methods = getMyObject().getClass().getMethods();
                if (methods == null) return;
                if (methods.length == 0) return;

                for (Method method : methods) {
                    try {
                        String retVal;
                        if (method.isAnnotationPresent(UIListItemHeader.class)) {
                            retVal = (String) method.invoke(getMyObject());
                            if (retVal != null) mainTitle.setText(retVal); else mainTitle.setText("");
                        }

                        if (method.isAnnotationPresent(UIListItemSubtitle.class)) {
                            retVal = (String) method.invoke(getMyObject());
                            if (retVal != null) subTitle.setText(retVal); else subTitle.setText("");
                        }

                        if (method.isAnnotationPresent(UIListItemTextLine.class)) {
                            retVal = (String) method.invoke(getMyObject());
                            if (retVal != null) textLine.setText(retVal); else textLine.setText("");
                        }

                        if (method.isAnnotationPresent(UIListItemRightText.class)) {
                            retVal = (String) method.invoke(getMyObject());
                            if (retVal != null) rightText.setText(retVal); else rightText.setText("");
                        }
                    } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        Logger.getLogger(Editor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            final VBox rootContainer = createVBox();
            rootContainer.setAlignment(Pos.CENTER);
            this.getChildren().addAll(bigIcon, rootContainer);
            
            final HBox headerContainer = createHBox();
            headerContainer.setAlignment(Pos.CENTER);
            headerContainer.getChildren().addAll(mainTitle, createHBox(), rightText);
            
            rootContainer.getChildren().addAll(headerContainer, subTitle, textLine);
        }
    }

    public T getMyObject() {
        return myObject;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        
//        bigIcon.getStyleClass().clear();
        //bigIcon.getStyleClass().addAll((selected) ? "FLI_bigIconSelected" : "FLI_bigIcon");
        
        this.getStyleClass().removeAll("FLI_thisSelected", "FLI_this");
        this.getStyleClass().addAll((selected) ? "FLI_thisSelected" : "FLI_this");
    }
}
