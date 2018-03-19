package ui.activity;

import javafx.scene.layout.Pane;
import jiconfont.icons.GoogleMaterialDesignIcons;
import service.RootService;
import ui.imagelist.DBImageList;
import ui.simplepanel.Panel;
import ui.simplepanel.PanelButton;

public class AllImagesActivity extends AbstractActivity {
    private final DBImageList dbImageList = new DBImageList();
    private final Panel panel = new Panel(
            dbImageList.getPaginator(),
            Panel.getSpacer(),
            new PanelButton("Upload...", GoogleMaterialDesignIcons.CLOUD_UPLOAD, e -> {
                RootService.showImportDialog();
            })
    );

    @Override
    public void onShow() {
        dbImageList.refresh();
    }

    public AllImagesActivity() {
        panel.getStyleClass().addAll("null_pane", "max_width", "height_48px");

        dbImageList.generateView(5, 5);
        dbImageList.regenerateCache();
        addAll(dbImageList, panel);
    }

}
