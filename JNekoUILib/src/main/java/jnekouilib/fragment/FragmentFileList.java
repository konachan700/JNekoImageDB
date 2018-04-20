package jnekouilib.fragment;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import jnekouilib.editor.ElementTextField;
import jnekouilib.panel.PanelButton;
import jnekouilib.panel.PanelSearch;
import jnekouilib.utils.FSParser;
import jnekouilib.utils.FSParserActionListener;
import jnekouilib.utils.FSParserActions;
import jnekouilib.utils.MessageBus;
import jnekouilib.utils.MessageBusActions;

import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class FragmentFileList extends Fragment implements FragmentListItemActionListener<Path>, FSParserActionListener {
    private final ArrayList<FragmentListItem<Path>>
            uiItems = new ArrayList<>();
    
    private final ScrollPane
            elementsSP= new ScrollPane();
    
    private final VBox
            vContainer = new VBox();
    
    private FragmentListItem
            selectedItem = null;
    
    private final FSParser
            files = new FSParser(this);
    
    private final ElementTextField
            fNameForOpenSave = new ElementTextField();
    
    public FragmentFileList() {
        super();

        this.getStyleClass().addAll("maxHeight", "maxWidth");

        elementsSP.getStyleClass().addAll("maxWidth", "maxHeight", "ScrollPane");
        elementsSP.setContent(vContainer);
        elementsSP.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        elementsSP.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        elementsSP.setFitToWidth(true);
        elementsSP.setFitToHeight(false);
        
        super.getChildren().addAll(elementsSP);
        
        fNameForOpenSave.addButton(al -> {
            final File f = new File(fNameForOpenSave.getXText());
            if (f.exists() && f.canRead()) {
                if (f.isDirectory()) {
                    files.setPath(f.getAbsolutePath());
                }// else {
                 //   files.setPath(f.getParent());
                //}
                create();
            } else {
                super.showMessage("Error", "Incorrect path:\n"+fNameForOpenSave.getXText());
            }
        });
        
        MessageBus.registerMessageReceiver(
                MessageBusActions.FileListReloadRun,
                (b, obj) -> {
            create();
        });
        
        super.getPanel().clear();
        super.getPanel().addNodes(
                new PanelSearch("Enter for search", s -> {
                    vContainer.getChildren().clear();       
                    uiItems.forEach(item -> {
                        if (item.getIndexedData().toLowerCase().contains(s.toLowerCase())) 
                            vContainer.getChildren().add(item);
                    });
                })
        );
        super.getPanel().addSeparator();
        super.getPanel().addNodes(
                new PanelButton("iconGoToRootDir", "Go to the root directory", "Go to root", e -> {
                    files.getRoots();
                }),
                new PanelButton("iconGoToLevelUp", "Level up", "Go up", e -> {
                    files.levelUp();
                })
        );
        
        files.init();
        files.setPath("");
        files.getFiles();
    }

    public void showSave() {
        fNameForOpenSave.setXLabelText("File name or path");
        if (!super.getChildren().contains(fNameForOpenSave))
            super.getChildren().addAll(fNameForOpenSave);
    }
        
    public final void create() {
        MessageBus.sendMessage(MessageBusActions.FileListReloadStarted);
        uiItems.clear();
        vContainer.getChildren().clear();
        files.getFiles();
    }
    
    public void dispose() {
        files.dispose();
    }
    
    @Override
    public void OnItemClick(Path object, FragmentListItem fli, MouseEvent me) {
        if (me.getButton() == MouseButton.PRIMARY) {
            if (me.getClickCount() == 1) {
                uiItems.forEach(item -> {
                    item.setSelected(false);
                });
                fli.setSelected(true);
                selectedItem = fli;

                fNameForOpenSave.setXText(selectedItem.getTitle());
            }else if (me.getClickCount() == 2) {
                if (object.toFile().isDirectory()) {
                    files.setPath(object);
                    create();
                } else {

                }
            }
        }
    }

    public void levelUp() {
        files.levelUp();
    }
    
    @Override
    public void rootListGenerated(Set<Path> pList) {
        uiItems.clear();
        vContainer.getChildren().clear();       
        pList.forEach(file -> {
            final FragmentListItem fli = new FragmentListItem(file, this);
            fli.create();
            uiItems.add(fli);
            vContainer.getChildren().add(fli);
        });
        MessageBus.sendMessage(MessageBusActions.FileListReloadFinished, pList);
        //fNameForOpenSave.setXText("");
    }

    @Override
    public void fileListRefreshed(Path p, CopyOnWriteArrayList<Path> pList, long execTime) {
        pList.sort((a, b) -> {
            return a.getFileName().toString().compareToIgnoreCase(b.getFileName().toString());
        });
        
        pList.forEach(file -> {
            if (file.toFile().isDirectory()) {
                final FragmentListItem fli = new FragmentListItem(file, this);
                fli.create();
                uiItems.add(fli);
                vContainer.getChildren().add(fli);
            }
        });
        
        pList.forEach(file -> {
            if (!file.toFile().isDirectory()) {
                final FragmentListItem fli = new FragmentListItem(file, this);
                fli.create();
                uiItems.add(fli);
                vContainer.getChildren().add(fli);
            }
        });

        MessageBus.sendMessage(MessageBusActions.FileListReloadFinished, pList);
        //fNameForOpenSave.setXText(p.toFile().getAbsolutePath());
    }

    @Override
    public void onLevelUp(Path p) {
        create();
    }

    @Override
    public void onError(FSParserActions act, Exception e) {
        System.err.println(e.getMessage()); 
        MessageBus.sendMessage(MessageBusActions.FileListReloadError, e);
    }
}
