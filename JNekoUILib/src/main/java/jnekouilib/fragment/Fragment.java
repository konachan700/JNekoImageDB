package jnekouilib.fragment;

import jnekouilib.editor.ElementTextArea;
import jnekouilib.panel.Panel;
import jnekouilib.panel.PanelButton;

import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

public class Fragment extends VBox {
    private FragmentHost 
            host = null;
    
    private final Panel 
            panel = new Panel();
    
    private final ElementTextArea
            messageBox = new ElementTextArea();
    
    private final Panel
            buttonsContainer = new Panel();
    
    private Fragment
            messageForm = null;
    
    public Fragment() {
        super();
        super.getStylesheets().add("/styles/window.css");
        super.getStyleClass().addAll("maxHeight", "maxWidth");
    }
    
    public Fragment(FragmentHost host) {
        super();
        super.getStylesheets().add("/styles/window.css");
        super.getStyleClass().addAll("maxHeight", "maxWidth");
        setHost(host);
    }
    
    public FragmentHost getHost() {
        return host;
    }

    public final void setHost(FragmentHost host) {
        this.host = host;
    }
    
    public void back() {
        if (host == null) return;
        host.back();
    }
    
    public Panel getPanel() {
        return panel;
    }
    
    public void showMessage(String title, String text) {
        showMessage(title, text, new PanelButton("iconSaveForList", "OK", "OK", e -> {
            host.back();
        }));
    }
    
    public void showMessage(String title, String text, FragmentMessageCallback c) {
        showMessage(title, text, new PanelButton("iconSaveForList", "OK", "OK", e -> {
            host.back();
            c.OnMessageResult(FragmentMessageResult.OK);
        }));
    }
    
    public void showMessageYesNo(String title, String text, FragmentMessageCallback c) {
        showMessage(title, text, 
                new PanelButton("iconButtonYes", "Yes", "Yes", e -> {
                    host.back();
                    c.OnMessageResult(FragmentMessageResult.YES);
                })
                ,
                new PanelButton("iconButtonNo", "No", "No", e -> {
                    host.back();
                    c.OnMessageResult(FragmentMessageResult.NO);
                })
        );
    }
    
    public void showMessage(String title, String text, PanelButton ... buttons) {
        if (messageForm == null) 
            messageForm = new Fragment();
        messageForm.getStyleClass().addAll("gray_bg");
        messageForm.setHost(host);
        messageForm.getChildren().clear();
        messageForm.setAlignment(Pos.CENTER);
        messageForm.getChildren().addAll(messageBox, buttonsContainer);
        messageBox.setXLabelText(title);
        messageBox.setXText(text);
        messageBox.setTextDisabled(false);
        buttonsContainer.clear();
        buttonsContainer.addSeparator();
        buttonsContainer.addNodes(buttons); 
        buttonsContainer.addSeparator();
        host.showFragment(messageForm, false);
    }
}
