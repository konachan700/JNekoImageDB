package dataaccess;

public class Lang {
    /*
        В будущем все строки должны браться из ресурсов с учетом языка системы, а не быть захардкожеными.
    */
    public static final String 
            TabAlbumImageList_root_album                                        = "Основной альбом",
            TabAlbumImageList_db_error                                          = "Database error, see logs.",
            TabAlbumImageList_info_format                                       = "В текущем каталоге %d альбомов и %d картинок.",
            
            InfiniteImageList_no_elements_found                                 = "Нет элементов для отображения",
            
            AlbumsCategories_txtAddNew                                          = "Новая группа",
            AlbumsCategories_MenuItem_FAVORITES                                 = "Избранное",
            AlbumsCategories_MenuItem_DELETED                                   = "Удаленные",

            AlbumSelectDialog_SelectAlbums                                      = "Выберите альбомы",
            
            AlbumImageList_Images                                               = "Картинки",
            AlbumImageList_Albums                                               = "Альбомы",
            AlbumImageList_ImagesInAlbum                                        = "Картинки альбома",
            
            ASDNewElement_newTitle                                              = "Добавить альбом",
            
            PleaseWait_PW                                                       = "Пожалуйста, подождите...",
            
            DialogWindow_Title                                                  = "Images database",
            
            GUITools_WinGUI_Title                                               = "JNeko Image Database",
            
            JNekoImageDB_no_salt_file                                           = "Error #1: Cannot open some crypt files.",
            JNekoImageDB_no_master_key                                          = "Error #2: Cannot open some crypt files.",
            JNekoImageDB_no_crypt_support                                       = "Error #3: JVM not support some crypt function.",
            JNekoImageDB_no_DB_connection                                       = "Error #4: Cannot connect to DB.",
            JNekoImageDB_title                                                  = "Images database",
            JNekoImageDB_menu_title_main                                        = "Основное",
            JNekoImageDB_menu_title_albums                                      = "Альбомы",
            JNekoImageDB_menu_title_tags                                        = "Теги",
            JNekoImageDB_menu_main_tags_parser                                  = "Парсер тегов",
            JNekoImageDB_menu_title_settings                                    = "Настройки",
            JNekoImageDB_menu_main_all_images                                   = "Все картинки",
            JNekoImageDB_menu_main_all_images_wo_groups                         = "Все картинки не в альбомах",
            JNekoImageDB_menu_main_all_images_wo_tags                           = "Все картинки без тегов",
            JNekoImageDB_menu_main_tagcloud                                     = "Облако тегов",
            JNekoImageDB_menu_main_fav_tags                                     = "Облако избранных тегов",
            JNekoImageDB_menu_main_add_images                                   = "Добавить картинки",
            JNekoImageDB_menu_settings_album_roots                              = "Управление альбомами",
            JNekoImageDB_menu_settings_main                                     = "Настройки", 
            JNekoImageDB_menu_settings_logs                                     = "Логи",
            
            FSImageList_stat_str_file                                           = "File ",
            FSImageList_stat_str_of                                             = " of ",
            FSImageList_stat_str_separator                                      = "; ",
            FSImageList_stat_str_slash                                          = "/",
            FSImageList_stat_str_mem_use                                        = "Memory use: ",
            FSImageList_stat_str_mem_mb                                         = "MB",
            FSImageList_stat_str_io                                             = "My I/O:",
            FSImageList_stat_str_kbps                                           = " kBps",
            FSImageList_starting_list_folder                                    = "Начинаю просматривать папку...",
            FSImageList_get_folder_list                                         = "Получаю список папок...",
            FSImageList_get_files_list                                          = "Получаю список файлов, может работать медленно...",
            FSImageList_log_str_folders                                         = "Папок ",
            FSImageList_log_str_files                                           = ", файлов: ",
            FSImageList_err_pop_1                                               = "ImagesFS.PopImage(IID) ERROR READING: ",
            FSImageList_err_pop_2                                               = "ImagesFS.PopImage(small_pns_id) ERROR READING: ",
            FSImageList_please_wait_1                                           = "Идет загрузка изображений...",
            FSImageList_log_str_file                                            = "Файл",
            FSImageList_log_str_already_exist                                   = "уже есть в базе данных, пропускаем.",
            FSImageList_log_str_cannoit_be_added                                = "не может быть добавлен в БД, см. логи.",
            FSImageList_log_broken                                              = "не читаем или поврежден, пропускаем...",
            
            Settings_path_for_default_uploading                                 = "Путь к папке для выгрузки картинок",
            Settings_show_deleted                                               = "Показывать группу удаленных",
            Settings_show_full_preview                                          = "Показывать полные миниатюры",
            
            ImageList_images_total                                              = " images",
            ImageList_mem_used                                                  = "Memory use: ",

            NullString                                                          = "",
            AppStyleCSS                                                         = "app_style.css",
            Files3Dots                                                          = "...",
            DateTimeFormat                                                      = "HH:mm dd.MM.yyyy",
            DateTimeFormatWithSeconds                                           = "HH:mm:ss dd.MM.yyyy",
            ArrowNext                                                           = "→",
            
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
