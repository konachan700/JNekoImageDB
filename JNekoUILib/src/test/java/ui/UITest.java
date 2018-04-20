package ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import jnekouilib.anno.UIBooleanField;
import jnekouilib.anno.UICollection;
import jnekouilib.anno.UIEditableCollection;
import jnekouilib.anno.UIFieldType;
import jnekouilib.anno.UILibDataSource;
import jnekouilib.anno.UIListItem;
import jnekouilib.anno.UIListItemHeader;
import jnekouilib.anno.UIListItemRightText;
import jnekouilib.anno.UIListItemSubtitle;
import jnekouilib.anno.UIListItemTextLine;
import jnekouilib.anno.UILongField;
import jnekouilib.anno.UISortIndex;
import jnekouilib.anno.UIStringField;
import jnekouilib.anno.UITextArea;
import jnekouilib.appmenu.AppMenuGroup;
import jnekouilib.appmenu.AppMenuItem;
import jnekouilib.editor.Editor;
import jnekouilib.fragment.FragmentFileList;
import jnekouilib.fragment.FragmentList;
import jnekouilib.windows.UIDialog;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public class UITest {
    @UIListItem
    @UILibDataSource
    public static class testUIItemTag {
        private long id;
        private String line;

        public testUIItemTag(long id, String line) {
            this.id = id;
            this.line = line;
        }
        
        public testUIItemTag() {
            this((new Random()).nextInt(), "ListItem#"+(new Random()).nextInt());
        }
        
        @UISortIndex(index=0)
        @UILongField(name="DBID", type= UIFieldType.GETTER, readOnly=1, labelText="Test ID")
        public long getId() {
            return id;
        }

        @UILongField(name="DBID", type= UIFieldType.SETTER)
        public void setId(long id) {
            this.id = id;
        }

        @UIListItemHeader
        @UISortIndex(index=1)
        @UIStringField(name="ModelName", type= UIFieldType.GETTER, readOnly=0, maxChars=64, helpText="Enter text here", labelText="Test name")
        public String getLine() {
            return line;
        }

        @UIStringField(name="ModelName", type= UIFieldType.SETTER)
        public void setLine(String line) {
            this.line = line;
        }
        
        @UIListItemSubtitle
        public String getSubtext() {
            return "text text text text";
        }
        
        @UIListItemTextLine
        public String getTextLine() {
            return "Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 26.061 sec";
        }
        
        @UIListItemRightText
        public String getRText() {
            return "12345-01";
        }
    }
    
    @UILibDataSource
    public static class testUIItems {
        private long id;
        private long number;
        private String line;
        private String text;
        private boolean bool;
        private Set<testUIItemTag> ifaces = new HashSet<>();
        private Set<testUIItemTag> ifacesOne = new HashSet<>();
        private Set<testUIItemTag> ifacesE = new HashSet<>();

        public testUIItems() {
            
        }
        
        @UISortIndex(index=0)
        @UILongField(name="DBID", type= UIFieldType.GETTER, readOnly=1, labelText="Test ID")
        public long getId() {
            return id;
        }
        
        @UILongField(name="DBID", type= UIFieldType.SETTER)
        public void setId(long id) {
            this.id = id;
        }

        @UISortIndex(index=9)
        @UILongField(name="DBV", type= UIFieldType.GETTER, readOnly=0, labelText="Test value")
        public long getNumber() {
            return number;
        }
        
        @UILongField(name="DBV", type= UIFieldType.SETTER)
        public void setNumber(long number) {
            this.number = number;
        }

        @UISortIndex(index=1)
        @UIStringField(name="ModelName", type= UIFieldType.GETTER, readOnly=0, maxChars=64, helpText="Enter text here", labelText="Test name")
        public String getLine() {
            return line;
        }

        @UIStringField(name="ModelName", type= UIFieldType.SETTER)
        public void setLine(String line) {
            this.line = line;
        }

        @UISortIndex(index=2)
        @UITextArea(name="ModelNote", type= UIFieldType.GETTER, readOnly=0, maxChars=2048, helpText="Enter text here", labelText="Test note")
        public String getText() {
            return text;
        }

        @UITextArea(name="ModelNote", type= UIFieldType.SETTER)
        public void setText(String text) {
            this.text = text;
        }

        @UISortIndex(index=6, separatorPresent=1)
        @UIBooleanField(name="ModelOLD", type= UIFieldType.GETTER, readOnly=0, labelText="Test in production")
        public boolean isBool() {
            return bool;
        }

        @UIBooleanField(name="ModelOLD", type= UIFieldType.SETTER)
        public void setBool(boolean bool) {
            this.bool = bool;
        }

        @UISortIndex(index=4, separatorPresent=1, separatorName="Lists")
        @UICollection(name="ifaces", type= UIFieldType.GETTER, multiSelect=1, yesNoBoxPresent=1, text="Select subtests")
        public Set<testUIItemTag> getIfaces() {
            return ifaces;
        }

        @UICollection(name="ifaces", type= UIFieldType.SETTER)
        public void setIfaces(Set<testUIItemTag> ifaces) {
            this.ifaces = ifaces;
        }
        
        @UISortIndex(index=5)
        @UICollection(name="ifacesOne", type= UIFieldType.GETTER, multiSelect=0, yesNoBoxPresent=1, text="Select one item from list")
        public Set<testUIItemTag> getIfacesOne() {
            return ifacesOne;
        }

        @UICollection(name="ifacesOne", type= UIFieldType.SETTER)
        public void setIfacesOne(Set<testUIItemTag> ifaces) {
            this.ifacesOne = ifaces;
        }
        
        @UISortIndex(index=21)
        @UIEditableCollection(name="ifacesEditable", type= UIFieldType.GETTER, text="Select one item from list")
        public Set<testUIItemTag> getIfacesEditable() {
            return ifacesE;
        }

        @UIEditableCollection(name="ifacesEditable", type= UIFieldType.SETTER)
        public void setIfacesEditable(Set<testUIItemTag> ifaces) {
            this.ifacesE = ifaces;
        }
    }

    @Rule 
    public JavaFXThreadingRule javafxRule = new JavaFXThreadingRule();
    
    @Test
    public void displaySimpleUI() {
        //IconFontFX.register(GoogleMaterialDesignIcons.getIconFont());
        
        final testUIItems t = new testUIItems();
        
        t.id = 1828356;
        t.bool = true;
        t.line = "Test string line";
        t.text = "Test text block";
        t.number = 36128;
        
        final ArrayList<testUIItemTag> al = new ArrayList<>();
        for (int i=0; i<16; i++)
            al.add(new testUIItemTag());
        
//        System.out.println("Collection all items count: "+al.size()); 
        
        t.ifaces = new HashSet<>();
        t.ifaces.add(al.get(0));
        t.ifaces.add(al.get(5));
        t.ifaces.add(al.get(7));
        
//        System.out.println("On-start preselected items count (MSL): "+ t.ifaces.size());
        
        t.ifacesOne = new HashSet<>();
        t.ifacesOne.add(al.get(0));
        t.ifacesOne.add(al.get(2));
        
//        System.out.println("On-start preselected items count (SSL): "+ t.ifacesOne.size());

        final UIDialog d = new UIDialog(800, 600, true, true, "Tests window");
        final Editor e = new Editor();
        
        final FragmentList<testUIItemTag> fl = new FragmentList<>(testUIItemTag.class, "ifaces");
        fl.setCollection(t.ifaces); 
//        fl.setObjectRequesterForNew(object -> new testUIItemTag());
        fl.create();
        
        final FragmentFileList files = new FragmentFileList();
        
        d.addMenu(
                new AppMenuGroup(
                        "UI Tests", "menuHeaderBlack", "menuHeaderIcon",
                        new AppMenuItem("Test Form", (c) -> {
                            d.showFragment(e, true);
                        }).defaultSelected(),
                        new AppMenuItem("Test List", (c) -> {
                            d.showFragment(fl, true);
                        }),
                        new AppMenuItem("Test File list", (c) -> {
                            d.showFragment(files, true);
                        })
                )
        );
        
        files.showSave();
        
        e.setHost(d.getRootFragment()); 
        e.addCollectionHelper("ifaces", al);
        e.addCollectionHelper("ifacesOne", al);
        e.readObject(t);
                 
        d.addLogoFromResources("/styles/test-logo.png");
        
        d.showFragment(e, true); 
        
        d.showAndWait();
        e.saveObject();
        
//        System.out.println("Selected items count on MSL: "+ t.ifaces.size());
//        System.out.println("Selected items count on SSL: "+ t.ifacesOne.size());

        
        final Alert alert = new Alert(AlertType.CONFIRMATION, "Are all elements displayed correctly?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait();
        if (alert.getResult() != ButtonType.YES) 
            Assert.assertTrue(false);
        
        Assert.assertTrue(true);
    }
    
    @Test
    public void isFXPresent() {
        assert Platform.isFxApplicationThread();
    }
    
    public UITest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
}
