package imgfs;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jnekoimagesdb.JNekoImageDB;

public class ImgFSH2 {
    private Connection 
            currentH2Connection;
    
    private boolean 
            isConnectedFlag = false;
    
    public ImgFSH2() {
        
    }
    
    public boolean isConnected() {
        return isConnectedFlag;
    }
    
    public void h2Close() {
        try {
            currentH2Connection.commit();
            currentH2Connection.clearWarnings();
            currentH2Connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(ImgFSH2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @SuppressWarnings("ConvertToTryWithResources")
    public void h2DatabaseConnect(String database, String password) throws SQLException {
        try {
            Class.forName("org.h2.Driver").newInstance();
            currentH2Connection = DriverManager.getConnection("jdbc:h2:"+database+"0;CIPHER=AES;MODE=MySQL;", "jneko", password+" "+password);
            currentH2Connection.setAutoCommit(false);
            
            final Statement initStatement = currentH2Connection.createStatement();
            initStatement.addBatch("CREATE TABLE if not exists `fsPreviews`(xmd5 BINARY(16) not null primary key, startSector bigint, endSector bigint, sectorSize bigint, actualSize bigint);");
            //initStatement.addBatch("CREATE TABLE if not exists `mainImages`(xmd5 BINARY(16) not null primary key, startSector bigint, sectorSize int, actualSize int, fileType int, imgType int);");
            
            initStatement.executeBatch();
            initStatement.close();
            
            currentH2Connection.commit();
            currentH2Connection.clearWarnings();
            
            isConnectedFlag = true;
        } catch (IllegalAccessException | ClassNotFoundException | InstantiationException e) {
            isConnectedFlag = false;
            throw new SQLException("Database error: org.h2.Driver");
        }
    }
    
    public PreparedStatement getPS(String sql) throws SQLException {
        return currentH2Connection.prepareStatement(sql);
    }
    
    public void commitPS(PreparedStatement ps) throws SQLException {
        ps.executeBatch();
        ps.close();
        currentH2Connection.commit();
        currentH2Connection.clearWarnings();
    }
    
    public long getLastSectorForFSPreviews() {
        try {
            PreparedStatement ps = getPS("SELECT `endSector` FROM `fsPreviews` ORDER BY `endSector` DESC LIMIT 0,1");
            ResultSet rs = ps.executeQuery();
            if (rs != null) {
                if (rs.next()) 
                    return rs.getLong("endSector");
            }
        } catch (SQLException ex) { }
        return 0;
    }
    
    public void writeRecords(ArrayList<ImgFSRecord> fsElements) throws IOException {
        if (fsElements.isEmpty()) return;
        try {
            final PreparedStatement ps = getPS("INSERT IGNORE INTO `fsPreviews` (?, ?, ?, ?, ?);");
            fsElements.stream().forEach((record) -> {
                try {
                    ps.setBytes(1, record.getMD5());
                    ps.setLong(2, record.getStartSector());
                    ps.setLong(3, record.getEndSector());
                    ps.setLong(4, record.getSectorSize());
                    ps.setLong(5, record.getActualSize());
                    ps.addBatch();
                } catch (SQLException ex) {
                    _L("cannot write element to DB, element ["+record.getFile().getAbsolutePath()+"], " + ex.getMessage());
                }
            });
            
            commitPS(ps);
        } catch (SQLException ex) {
            throw new IOException("cannot write element to DB, " + ex.getMessage());
        }
    }

    private void _L(String s) {
        System.out.println(s);
        JNekoImageDB.L(s); 
    }
}
