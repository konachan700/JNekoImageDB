package albums;

import dataaccess.Crypto;
import dataaccess.ImageEngine;
import dataaccess.SQLite;
import dataaccess.SplittedFile;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import menulist.MenuGroupItem;

public class AlbumsCategories extends ScrollPane {
    public class AlbumsCategory {
        public String name;
        public long ID;
        public int state;
        
        public AlbumsCategory(String n, long id, int st) {
            name = n.trim();
            ID = id;
            state = st;
        }
        
        public int saveChanges() {
            try { 
                PreparedStatement ps = SQL.getConnection().prepareStatement("UPDATE 'AlbumsGroup' SET groupName=?, state=? WHERE oid=?;");
                ps.setBytes(1, zCrypto.Crypt(zCrypto.align16b(name.getBytes())));
                ps.setInt(2, state);
                ps.setLong(3, ID);
                ps.execute();

                return 0;
            } catch (SQLException  ex) {
                return -1;
            }
        }
    }
    
    public class ACListItem extends HBox {
        public AlbumsCategory AC;
        private TextField lTextField;
        
        public ACListItem(AlbumsCategory ac) {
            super(2);
            AC = ac;
            
            this.getStyleClass().add("itemHBox");
            _s1(this, 9999, 32);
            
            //Label l = new Label(ac.name);
            lTextField = new TextField(ac.name);
            //l.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("albgrp.png"))));
            lTextField.getStyleClass().add("itemLabel");
            _s1(lTextField, 9999, 32);
            
            ImageView i = new ImageView(new Image(getClass().getResourceAsStream("albgrp.png")));
            i.setFitHeight(32);
            i.setFitWidth(32);
            
            Button b = new Button("", new ImageView(new Image(getClass().getResourceAsStream((ac.state==1) ? "delete2.png" : "selected.png"))));
            b.getStyleClass().add("itemButton");
            _s2(b, 32, 32);
            b.setOnMouseClicked((MouseEvent event) -> {
                if (AC.state == 0) {
                    AC.state = 1;
                    AC.saveChanges();
                } else {
                    AC.state = 0;
                    AC.saveChanges();
                }
                RefreshAll();
            });
            
            Button s = new Button("", new ImageView(new Image(getClass().getResourceAsStream("save2.png"))));
            s.getStyleClass().add("itemButton");
            _s2(s, 32, 32);
            s.setOnMouseClicked((MouseEvent event) -> {
                if (lTextField.getText().trim().length() <= 1) return;
                ac.name = lTextField.getText().trim();
                ac.saveChanges();
                RefreshAll();
            });
            
            this.getChildren().addAll(i, lTextField, s, b);
        }
    }
    
    private final SQLite 
            SQL;// = new SQLite();
    
    private final VBox 
            mainPane = new VBox(2);
    
    private final HBox
            toolbox = new HBox(2);
    
    private final TextField
            txtAddNew = new TextField("Новая группа");
    
    private final Button
            todbImg = new Button("", new ImageView(new Image(getClass().getResourceAsStream("adddef.png"))));
    
    private final Crypto
            zCrypto;
    
    private MenuGroupItem MGI;
    
    public AlbumsCategories(Crypto k, MenuGroupItem mgi, SQLite sql) {
        super();
        zCrypto = k;
        MGI     = mgi;
        SQL     = sql;

        SQL.ExecuteSQL("CREATE TABLE if not exists 'AlbumsGroup'(oid int not null primary key, groupName blob, state int);");
                
        this.setVbarPolicy(ScrollBarPolicy.ALWAYS);
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        this.getStyleClass().add("sroll_pane");
        this.setContent(mainPane);
        this.setFitToWidth(true);
        this.setFitToHeight(true);
        this.setMaxSize(9999, 9999);
        this.setPrefSize(9999, 9999);
        
        mainPane.getStyleClass().add("mainPane");
        
        toolbox.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        toolbox.getStyleClass().add("toolbox");
        _s1(toolbox, 9999, 64);
        txtAddNew.getStyleClass().add("txtAddNew");
        _s1(txtAddNew, 9999, 64);
        _s2(todbImg, 64, 64);
        todbImg.getStyleClass().add("ImgButtonR");
        toolbox.getChildren().addAll(txtAddNew, todbImg);
        
        todbImg.setOnMouseClicked((MouseEvent event) -> {
            if (txtAddNew.getText().trim().length() <= 1) return;
            try {
                PreparedStatement ps = SQL.getConnection().prepareStatement("INSERT INTO 'AlbumsGroup' VALUES(?, ?, 1);");
                final long tmr = new Date().getTime();
                ps.setLong(1, tmr);
                ps.setBytes(2, zCrypto.Crypt(zCrypto.align16b(txtAddNew.getText().getBytes())));
                ps.execute();
                RefreshAll();
            } catch (SQLException ex) { }
        });
    }
    
    public HBox getToolbox() {
        return toolbox;
    }
    
    public final void RefreshAll() {
        ArrayList<AlbumsCategory> alac = getAlbumsGroupsID();
        if (alac == null) {
            doNothing();
            return;
        }
        
//        if (alac.size() <= 0) {
//            doNothing();
//            return;
//        }
        
        mainPane.getChildren().clear();
        MGI.clearAll();
        MGI.addLabel(Long.toString(ImageEngine.ALBUM_ID_FAVORITES), "Избранное");
        
        alac.stream().forEach((ac) -> {
            ACListItem acli = new ACListItem(ac);
            mainPane.getChildren().add(acli);
            if (ac.state == 0) {
                MGI.addLabel(""+ac.ID, ac.name);
            }
        });
        
        MGI.addLabel(Long.toString(ImageEngine.ALBUM_ID_DELETED), "Удаленные");
        MGI.Commit();
    }
    
    private void doNothing() {
        
    }

    public ArrayList<AlbumsCategory> getAlbumsGroupsID() {
        try {
            PreparedStatement ps = SQL.getConnection().prepareStatement("SELECT * FROM 'AlbumsGroup' ORDER BY oid DESC;");
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                ArrayList<AlbumsCategory> alac = new ArrayList<>();
                while (rs.next()) {
                    AlbumsCategory ac = new AlbumsCategory(new String(zCrypto.Decrypt(rs.getBytes("groupName"))).trim(), rs.getLong("oid"), rs.getInt("state"));
                    alac.add(ac);
                }
                return alac;
            }
        } catch (SQLException ex) {
            return null;
        }
        
        return null;
    }
    
    private VBox getSeparator1() {
        VBox sep1 = new VBox();
        _s1(sep1, 9999, 16);
        return sep1;
    }
    
    private VBox getSeparator1(double sz) {
        VBox sep1 = new VBox();
        _s2(sep1, sz, 16);
        return sep1;
    }
    
    private void _s2(Region n, double w, double h) {
        n.setMaxSize(w, h);
        n.setPrefSize(w, h);
        n.setMinSize(w, h);
    }
    
    private void _s1(Region n, double w, double h) {
        n.setMaxSize(w, h);
        n.setPrefSize(w, h);
    }
}
