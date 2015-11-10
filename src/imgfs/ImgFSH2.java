package imgfs;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import jnekoimagesdb.JNekoImageDB;
import org.h2.jdbcx.JdbcConnectionPool;

public class ImgFSH2 {
    private Connection 
            connWrite,
            connRead;
    
    private JdbcConnectionPool 
            pool;
    
    private boolean 
            isConnectedFlag = false;
    
    public ImgFSH2() {
        
    }
    
    public boolean isConnected() {
        return isConnectedFlag;
    }
    
    public void h2Close() {
        try {
            connWrite.commit();
            connWrite.clearWarnings();
            connWrite.close();
            connRead.clearWarnings();
            connRead.clearWarnings();
            pool.dispose();
        } catch (SQLException ex) {
            Logger.getLogger(ImgFSH2.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @SuppressWarnings("ConvertToTryWithResources")
    public void h2DatabaseConnect(String database, String password) throws SQLException {
        try {
            Class.forName("org.h2.Driver").newInstance();
            pool = JdbcConnectionPool.create("jdbc:h2:"+database+";CIPHER=AES;MODE=MySQL;", "jneko", password+" "+password);
            
            connWrite = pool.getConnection();
            connWrite.setAutoCommit(false);
            
            final Statement initStatement = connWrite.createStatement();
            initStatement.addBatch("CREATE TABLE if not exists `fsPreviews`(xmd5 BINARY(16) not null primary key, startSector bigint, endSector bigint, sectorSize bigint, actualSize bigint);");
            //initStatement.addBatch("CREATE TABLE if not exists `mainImages`(xmd5 BINARY(16) not null primary key, startSector bigint, sectorSize int, actualSize int, fileType int, imgType int);");
            
            initStatement.executeBatch();
            initStatement.close();
            
            connWrite.commit();
            connWrite.clearWarnings();
            
            connRead = pool.getConnection();
            connRead.setAutoCommit(false);
            
            isConnectedFlag = true;
        } catch (IllegalAccessException | ClassNotFoundException | InstantiationException e) {
            isConnectedFlag = false;
            throw new SQLException("Database error: org.h2.Driver");
        }
    }
    
    protected PreparedStatement getPSW(String sql) throws SQLException {
        return connWrite.prepareStatement(sql);
    }
    
    protected PreparedStatement getPSR(String sql) throws SQLException {
        return connRead.prepareStatement(sql);
    }
    
    protected ResultSet getFirstR(PreparedStatement ps) throws SQLException {
        ResultSet rs = ps.executeQuery();
        if (rs != null) {
            if (rs.next()) {
                return rs; 
            }   
        }
        closePSR(ps, rs);
        throw new SQLException("getFirstR: No data found;");
    }
    
    protected long getFirstRLong(PreparedStatement ps, String field) throws SQLException {
        final ResultSet rs = getFirstR(ps);
        final long retVal = rs.getLong(field);
        closePSR(ps, rs);
        return retVal;
    }
    
    
    protected void closePSR(PreparedStatement ps, ResultSet rs) throws SQLException {
        rs.close();
        ps.clearWarnings();
        ps.close();
    }
    
    protected void commitPSW(PreparedStatement ps) throws SQLException {
        ps.executeBatch();
        ps.close();
        connWrite.commit();
        connWrite.clearWarnings();
    }
    
    /*---***********************************************************************************************---*/
    
    public boolean isMD5NotPresentInFSPreviews(byte xmd5[]) {
        try {
            final PreparedStatement ps = getPSR("SELECT `actualSize` FROM `fsPreviews` WHERE xmd5=? LIMIT 0,1");
            ps.setBytes(1, xmd5);
            return (getFirstRLong(ps, "actualSize") > 0);
        } catch (SQLException ex) { }
        return false;
    }
    
    public long getLastSectorForFSPreviews() {
        try {
            PreparedStatement ps = getPSR("SELECT `endSector` FROM `fsPreviews` ORDER BY `endSector` DESC LIMIT 0,1");
            return getFirstRLong(ps, "endSector");
        } catch (SQLException ex) { }
        return 0;
    }
    
    public void writeRecords(ArrayList<ImgFSRecord> fsElements) throws IOException {
        if (fsElements.isEmpty()) return;
        try {
            final PreparedStatement ps = getPSW("INSERT IGNORE INTO `fsPreviews` (?, ?, ?, ?, ?);");
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
            
            commitPSW(ps);
        } catch (SQLException ex) {
            throw new IOException("cannot write element to DB, " + ex.getMessage());
        }
    }

    private void _L(String s) {
        System.out.println(s);
        JNekoImageDB.L(s); 
    }
}
