package ui.taglist;

import java.util.Optional;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import proto.LocalDaoService;
import proto.UseServices;

public class TagElement extends Label implements UseServices {
	public TagElement(String tag){
		super(tag);
		super.getStyleClass().addAll("tag_element");
		super.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
		Optional.ofNullable(getService(LocalDaoService.class))
				.map(s -> s.tagGetMeta(tag))
				.map(m -> m.getMeta("tag-color", Color.class))
				.ifPresent(c -> {
					this.setBackground(new Background(new BackgroundFill(c, CornerRadii.EMPTY, Insets.EMPTY)));
				});
	}
}
