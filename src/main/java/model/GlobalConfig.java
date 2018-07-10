package model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javafx.scene.input.MouseButton;
import services.impl.CryptographyServiceImpl;
import ui.dialogs.windows.engine.WindowDimension;
import ui.imageview.localdb.LocalDbImageView;

public class GlobalConfig {
	public static final javafx.scene.paint.Color IMAGE_VIEW__NON_SELECTED_COLOR = javafx.scene.paint.Color.color(0.8,0.8,0.8);
	public static final javafx.scene.paint.Color IMAGE_VIEW__SELECTED_COLOR = javafx.scene.paint.Color.web("#c33"); //color(0.3,0.8,0.3);
	public static final javafx.scene.paint.Color IMAGE_VIEW__FONT_COLOR = javafx.scene.paint.Color.color(0.3,0.3,0.3);
	public static final javafx.scene.text.Font IMAGE_VIEW__FONT = javafx.scene.text.Font.loadFont(
			LocalDbImageView.class.getResource("/style/fonts/FjallaOne-Regular.ttf").toExternalForm(),16);
	public static final String PREVIEW_FORMAT = "jpg";
	public static final String STORAGE_ROOT_DIR = "./storage";
	public static final String PREFIX = "DB";

	/***********************************************************************************************************************/

	// encrypting
	private CryptographyServiceImpl.EncryptType encryptType = CryptographyServiceImpl.EncryptType.AES128;
	private String salt = "6325btvt678&^Vg876cf^f7(gyv9t7rer6c&5w42s3as53zwsxtrv08bIOUN9)^*G^XWE$Zw4z6";

	// general input
	private MouseButton primaryButton = MouseButton.PRIMARY;
	private MouseButton secondaryButton = MouseButton.SECONDARY;

	// folders
	private String browserOutboxFolder = "./storage/outbox";

	// main window
	private Map<String, WindowDimension> windowDimensions = new HashMap<>();
	private int windowResizeStep = 32;
	private long popupLifeTime = 3300;

	// image view
	private Double minImageSize	= 16D;

	// local filesystem image dashboard
	private int localFsPreviewsCountInRow = 6;
	private int localFsPreviewsCountInCol = 5;

	// local database image dashboard
	private int localDbPreviewsCountInRow = 6;
	private int localDbPreviewsCountInCol = 5;
	private Set<String> localFsAllowedFileTypes = new HashSet<>(Arrays.asList(".jpg", ".jpeg", ".jpe", ".png"));

	/***********************************************************************************************************************/

	public WindowDimension getWindowDimension(String id, Double defaultWidth, Double defaultHeight) {
		if (getWindowDimensions().containsKey(id)) {
			return getWindowDimensions().get(id);
		}

		final WindowDimension wd = new WindowDimension(defaultWidth, defaultHeight);
		getWindowDimensions().put(id, wd);
		return getWindowDimensions().get(id);
	}

	public WindowDimension getWindowDimension(String id) {
		return getWindowDimension(id, 1000D, 740D);
	}

	public Double getMinImageSize() {
		return minImageSize;
	}

	public void setMinImageSize(Double minImageSize) {
		this.minImageSize = minImageSize;
	}

	public int getLocalFsPreviewsCountInRow() {
		return localFsPreviewsCountInRow;
	}

	public void setLocalFsPreviewsCountInRow(int localFsPreviewsCountInRow) {
		this.localFsPreviewsCountInRow = localFsPreviewsCountInRow;
	}

	public int getLocalFsPreviewsCountInCol() {
		return localFsPreviewsCountInCol;
	}

	public void setLocalFsPreviewsCountInCol(int localFsPreviewsCountInCol) {
		this.localFsPreviewsCountInCol = localFsPreviewsCountInCol;
	}

	public int getLocalDbPreviewsCountInRow() {
		return localDbPreviewsCountInRow;
	}

	public void setLocalDbPreviewsCountInRow(int localDbPreviewsCountInRow) {
		this.localDbPreviewsCountInRow = localDbPreviewsCountInRow;
	}

	public int getLocalDbPreviewsCountInCol() {
		return localDbPreviewsCountInCol;
	}

	public void setLocalDbPreviewsCountInCol(int localDbPreviewsCountInCol) {
		this.localDbPreviewsCountInCol = localDbPreviewsCountInCol;
	}

	public Set<String> getLocalFsAllowedFileTypes() {
		return localFsAllowedFileTypes;
	}

	public int getWindowResizeStep() {
		return windowResizeStep;
	}

	public void setWindowResizeStep(int windowResizeStep) {
		this.windowResizeStep = windowResizeStep;
	}

	public Map<String, WindowDimension> getWindowDimensions() {
		return windowDimensions;
	}

	public void setWindowDimensions(Map<String, WindowDimension> windowDimensions) {
		this.windowDimensions = windowDimensions;
	}

	public String getBrowserOutboxFolder() {
		return browserOutboxFolder;
	}

	public void setBrowserOutboxFolder(String browserOutboxFolder) {
		this.browserOutboxFolder = browserOutboxFolder;
	}

	public MouseButton getPrimaryButton() {
		return primaryButton;
	}

	public void setPrimaryButton(MouseButton primaryButton) {
		this.primaryButton = primaryButton;
	}

	public MouseButton getSecondaryButton() {
		return secondaryButton;
	}

	public void setSecondaryButton(MouseButton secondaryButton) {
		this.secondaryButton = secondaryButton;
	}

	public CryptographyServiceImpl.EncryptType getEncryptType() {
		return encryptType;
	}

	public void setEncryptType(CryptographyServiceImpl.EncryptType encryptType) {
		this.encryptType = encryptType;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public long getPopupLifeTime() {
		return popupLifeTime;
	}

	public void setPopupLifeTime(long popupLifeTime) {
		this.popupLifeTime = popupLifeTime;
	}
}
