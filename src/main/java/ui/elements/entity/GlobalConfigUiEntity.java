package ui.elements.entity;

import ui.annotation.creator.UICreatorDialog;
import ui.annotation.creator.UICreatorEntityElement;
import ui.proto.UIEntity;

@UICreatorDialog
public class GlobalConfigUiEntity implements UIEntity {
	@UICreatorEntityElement(caption = "Folder, where you save an images")
	String browserImageDirectory = "./";

	@UICreatorEntityElement(caption = "Folder for quick save")
	String fastSaveImageDirectory = "./";

	@UICreatorEntityElement(caption = "Test number")
	Integer number = 0;
}
