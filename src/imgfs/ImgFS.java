package imgfs;

import dialogs.DialogMTPrevGenProgress;
import imgfstabs.TabAddImagesToDB;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class ImgFS {
    private static final ImgFSCrypto
        cryptoEx = new ImgFSCrypto(() -> {
            
            return null;
        });
    
    private static final ImgFSPreviewGen.PreviewGeneratorProgressListener
            progressInd = new ImgFSPreviewGen.PreviewGeneratorProgressListener() {
                @Override
                public void OnStartThread(int itemsCount, int tID) {
                    progressDialog.itemProgresss(tID);
                }

                @Override
                public void OnNewItemGenerated(int itemsCount, Path p, int tID, String quene) {
                    progressDialog.itemSetInfo(tID, p, itemsCount, quene);
                }

                @Override
                public void OnError(int tID) {

                }

                @Override
                public void OnComplete(int tID) {
                    progressDialog.itemComplete(tID);
                }

                @Override
                public void OnCreated(int tID) {
                    progressDialog.itemCreate(tID);
                }   
            };
    
    private static TabAddImagesToDB         addNewImagesTab;
    private static ImgFSDatastore           datastore;
    private static DialogMTPrevGenProgress  progressDialog = new DialogMTPrevGenProgress();

    public static void init(String databaseName) throws Exception {
        cryptoEx.init(databaseName);
        
        datastore = new ImgFSDatastore(cryptoEx, databaseName);
        datastore.init();
        
        addNewImagesTab = new TabAddImagesToDB(cryptoEx, databaseName);
    }
    
    public static void dispose() {
        datastore.close();
        addNewImagesTab.dispose();
    }
    
    public synchronized static ImgFSCrypto getCrypt() {
        return cryptoEx;
    }
    
    public synchronized static TabAddImagesToDB getAddImagesTab() {
        return addNewImagesTab;
    }
    
    public synchronized static ImgFSDatastore getDatastore() {
        return datastore;
    }
    
    public static ImgFSPreviewGen.PreviewGeneratorProgressListener getProgressListener() {
        return progressInd;
    }
    
    public static void progressShow() {
        progressDialog.show();
    }
}
