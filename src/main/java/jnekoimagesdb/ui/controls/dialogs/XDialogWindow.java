package jnekoimagesdb.ui.controls.dialogs;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import jnekoimagesdb.ui.GUITools;
import jnekoimagesdb.ui.Lang;
import jnekoimagesdb.ui.ResizeHelper;
import jnekoimagesdb.ui.controls.ToolsPanelBottom;
import jnekoimagesdb.ui.controls.ToolsPanelTop;
import jnekoimagesdb.ui.controls.elements.ElementsIDCodes;
import jnekoimagesdb.ui.controls.elements.GUIActionListener;
import jnekoimagesdb.ui.controls.elements.SButton;
import jnekoimagesdb.ui.controls.elements.SEVBox;
import jnekoimagesdb.ui.controls.elements.SFHBox;
import jnekoimagesdb.ui.controls.elements.SFLabel;

public class XDialogWindow {
    public static final int
            COLOR_RED = 1,
            COLOR_GREEN = 2,
            COLOR_BLUE = 3,
            COLOR_BLACK = 4;
    
    private final ImageView 
            imgLogoV = new ImageView(GUITools.loadIcon("logo6"));

    private final GUITools.DragDelta 
            DRD = new GUITools.DragDelta();
    
    private final SEVBox
            windowVBox       = new SEVBox(),
            conatainerMain   = new SEVBox("window_null_hbox"),
            rootConatiner    = new SEVBox("window_null_hbox");
            ;
    
    private boolean 
            enableClose = true;
    
    private ToolsPanelTop
            panelTop;
            
    private ToolsPanelBottom
            panelBottom;
    
    private final SFHBox
            headerWin        = new SFHBox(6, 400, 9999, 40, 40, "window_header_hbox"),
            toolbar          = new SFHBox(0, 400, 9999, 72, 72, "window_null_hbox");
    
    private final Stage
            primaryStage = new Stage();
    
    private final GUIActionListener
            actListener = (a, b) -> {
                switch (b) {
                    case buttonMinimizeWindow:
                        primaryStage.setIconified(true);
                        break;
                    case buttonMaximizeWindow:
                        primaryStage.setMaximized(!primaryStage.isMaximized());
                        break;
                    case buttonCloseWindow: 
                        if (enableClose) primaryStage.close();
                        break;
                }
            };
    
    public XDialogWindow() { }
    
    public void create(ToolsPanelTop pt, ToolsPanelBottom bp, Node px, int style) {
        create(pt, bp, px, style, 1000, 700);
    }
    
    public void setClosingEnable(boolean b) {
        enableClose = b;
    }
    
    public void create(ToolsPanelTop pt, ToolsPanelBottom bp, Node px, int style, int w, int h) {
        final Scene scene = new Scene(windowVBox, w, h);
        
        switch (style) {
            case COLOR_RED:
                GUITools.setStyle(windowVBox, "DialogWindow", "color_red");
                break;
            case COLOR_GREEN:
                GUITools.setStyle(windowVBox, "DialogWindow", "color_green");
                break;
            case COLOR_BLUE:
                GUITools.setStyle(windowVBox, "DialogWindow", "color_blue");
                break;
            case COLOR_BLACK:
                GUITools.setStyle(windowVBox, "DialogWindow", "color_black");
                break;
            default:
                GUITools.setStyle(windowVBox, "DialogWindow", "color_black");
        }
        
        panelTop = pt;
        panelBottom = bp;
        primaryStage.initModality(Modality.APPLICATION_MODAL);
        
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            scene.setFill(Color.TRANSPARENT);
            primaryStage.initStyle(StageStyle.TRANSPARENT);
            
            headerWin.setAlignment(Pos.CENTER_LEFT);
            headerWin.getChildren().addAll(
                    new ImageView(GUITools.loadIcon("win-icon-32")),
                    new SFLabel(Lang.GUITools_WinGUI_Title, 128, 9999, 32, 32, "label_header", "XGreenDialogWindow"),
                    GUITools.getSeparator(),
                    new SButton(GUITools.loadIcon("head-minimize-16"), ElementsIDCodes.buttonMinimizeWindow, 16, actListener, "window_header_button"),
                    GUITools.getSeparator(8),
                    new SButton(GUITools.loadIcon("head-maximize-16"), ElementsIDCodes.buttonMaximizeWindow, 16, actListener, "window_header_button"),
                    GUITools.getSeparator(8),
                    new SButton(GUITools.loadIcon("head-close-16"), ElementsIDCodes.buttonCloseWindow, 16, actListener, "window_header_button"),
                    GUITools.getSeparator(8)
            );
            
            headerWin.setOnMousePressed((MouseEvent mouseEvent) -> {
                DRD.x = primaryStage.getX() - mouseEvent.getScreenX();
                DRD.y = primaryStage.getY() - mouseEvent.getScreenY();
            });

            headerWin.setOnMouseDragged((MouseEvent mouseEvent) -> {
                primaryStage.setX(mouseEvent.getScreenX() + DRD.x);
                primaryStage.setY(mouseEvent.getScreenY() + DRD.y);
            });
            
            rootConatiner.getChildren().add(headerWin);
        }
        
        toolbar.setAlignment(Pos.CENTER_LEFT);
        if (panelTop != null) {
            GUITools.setMaxSize(pt, 9999, 64);
            imgLogoV.setFitHeight(64);
            toolbar.getChildren().addAll(
                    panelTop,
                    imgLogoV
            );
            rootConatiner.getChildren().add(toolbar);
        }
        
        if (px != null) conatainerMain.getChildren().add(px);
        
        conatainerMain.setAlignment(Pos.CENTER);
        GUITools.setMaxSize(conatainerMain, 9999, 9999);
        rootConatiner.getChildren().add(conatainerMain);
        
        if (panelBottom != null) {
            GUITools.setMaxSize(bp, 9999, 24);
            rootConatiner.getChildren().add(panelBottom);
        }

        windowVBox.getChildren().add(rootConatiner);
        
        primaryStage.getIcons().add(GUITools.loadIcon("win-icon-128"));
        primaryStage.getIcons().add(GUITools.loadIcon("win-icon-64"));
        primaryStage.getIcons().add(GUITools.loadIcon("win-icon-32"));
        primaryStage.setMinWidth(840);
        primaryStage.setMinHeight(480);
        primaryStage.setTitle(Lang.JNekoImageDB_title);
        primaryStage.setScene(scene);
        
        if (System.getProperty("os.name").toLowerCase().contains("win")) ResizeHelper.addResizeListener(primaryStage);
    }
    
    public void show() {
        primaryStage.show();
    }
    
    public void showModal() {
        primaryStage.showAndWait();
    }
    
    public void hide() {
        primaryStage.hide();
    }
}
