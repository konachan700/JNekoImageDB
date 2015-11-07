package imgfs;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            initStatement.addBatch("CREATE TABLE if not exists `fsPreviews`(xmd5 BINARY(16) not null primary key, startSector bigint, sectorSize int, actualSize int, fileType int);");
            initStatement.addBatch("CREATE TABLE if not exists `mainImages`(xmd5 BINARY(16) not null primary key, startSector bigint, sectorSize int, actualSize int, fileType int, imgType int);");
            
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
    
    
    
}
