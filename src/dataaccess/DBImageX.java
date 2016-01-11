package dataaccess;

@Deprecated
public class DBImageX {
    public volatile long
            prev_oid,
            prev_startSector, 
            prev_sectorSize, 
            prev_actualSize,
            pl_oid,
            pl_idid,
            pl_pdid,
            pl_imgtype,
            ia_oid,
            ia_imgoid,
            ia_alboid,
            prev_type;
    
    public byte[]
            prev_md5;
            //prev_fileName;
    
    public DBImageX() { }
}
