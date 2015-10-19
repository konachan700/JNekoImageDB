package dataaccess;

public class Lang {
    /*
        В будущем все строки должны браться из ресурсов с учетом языка системы, а не быть захардкожеными.
    */
    
    public static final String 
            AlbumsCategories_txtAddNew                                          = "Новая группа",
            AlbumsCategories_MenuItem_FAVORITES                                 = "Избранное",
            AlbumsCategories_MenuItem_DELETED                                   = "Удаленные",

            AlbumSelectDialog_SelectAlbums                                      = "Выберите альбомы",
            
            AlbumImageList_Images                                               = "Картинки",
            AlbumImageList_Albums                                               = "Альбомы",
            AlbumImageList_ImagesInAlbum                                        = "Картинки альбома",
            
            ASDNewElement_newTitle                                              = "",
            
            PleaseWait_PW                                                       = "Пожалуйста, подождите...",
            
            DialogWindow_Title                                                  = "Images database",
            
            GUITools_WinGUI_Title                                               = "JNeko Image Database",
            
            NullString                                                          = "",
            AppStyleCSS                                                         = "app_style.css",
            Files3Dots                                                          = "...",
            
            ERR_Crypto_no_salt_found                                            = "Info: No salt found, but database exist.",
            ERR_Crypto_new_salt_generated                                       = "Info: New salt are generated, all data lost.",
            ERR_Crypto_new_master_key_generated                                 = "Info: New master key are generated, all data lost.",
            ERR_Crypto_no_master_key_found                                      = "Info: No master key found, but database exist.",
            ERR_Crypto_default_master_key_found                                 = "Info: Private key found on default location.",
            
            ERR_DBEngine_SQL_CONNECTION_CLOSED                                  = "SQL CONNECTION IS CLOSED;",
            ERR_DBEngine_OLD_SQL_CONNECTION_CLOSED                              = "OLD SQL CONNECTION IS CLOSED;",
            
            ERR_DBWrapper_F_getPrerview                                         = "getPrerview ERROR: ", 
            ERR_DBWrapper_F_getImagesX                                          = "getImagesX ERROR: ",
            ERR_DBWrapper_F_WriteAPPSettingsString                              = "WriteAPPSettingsString ERROR: ",
            ERR_DBWrapper_F_ReadAPPSettingsString                               = "ReadAPPSettingsString ERROR: ",
            
            ERR_FSEngine_incorrect_md5_for_record                               = "Invalid database record; md5 incorrect",
            ERR_FSEngine_null_file                                              = "Invalid database record; null file;",
            ERR_FSEngine_cannot_read_write_file                                 = "Cannot read/write file",
            ERR_FSEngine_no_disk_space                                          = "PushFileMT->allocateDiskSpaceMT(): [NULL] cannot allocate disk space."
    ;
    
}
