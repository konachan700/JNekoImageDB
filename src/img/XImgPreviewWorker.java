package img;

public class XImgPreviewWorker {
//public class XImgPreviewWorker implements Runnable {
//    private final ConcurrentLinkedQueue<XImgPreviewGen.PreviewElement> 
//            threadQueue = new ConcurrentLinkedQueue<>(), 
//            processedQueue = new ConcurrentLinkedQueue<>();
//
//    private final 
//            Object syncObject;
//
//    private volatile boolean 
//            isExit = false,
//            isWaiting = false,
//            isDisplayProgress = false;
//    
//    private final XImgPreviewGen.PreviewGeneratorActionListener
//            actionListenerX;
//
//    private final XImgCrypto
//            imCrypt;
//
//    private final XImgImages
//            imgConverter = new XImgImages();
//
//    private final String 
//            queneName;
//
//    private String
//            tempString = "";
//
//    private Session 
//            hibSession = null;
//
//    private final XImg.PreviewType
//            myType;
//
//    public XImgPreviewWorker(Object o, XImgPreviewGen.PreviewGeneratorActionListener al, 
//            XImgCrypto ic, String quene, XImg.PreviewType t) {
//        super();
//        queneName       = quene;
//        syncObject      = o;
//        actionListenerX = al;
//        imCrypt         = ic;
//        myType          = t;
//    }
//
//    public void setProgressDisplay(boolean b) {
//        isDisplayProgress = b;
//    }
//
//    public boolean isWaitingThread() {
//        return isWaiting;
//    }
//
//    public void exit() {
//        isExit = true;
//    }
//
//    public void addElementToQueue(Path p) throws XImgPreviewGen.FileIsNotImageException, IOException {
//        if (!Files.exists(p))           throw new IOException("addElementToQueue: file not found ["+p.toString()+"];");
//        if (!Files.isRegularFile(p))    throw new IOException("addElementToQueue: it is not a file ["+p.toString()+"];");
//        if (!Files.isReadable(p))       throw new IOException("addElementToQueue: cannot read file ["+p.toString()+"];");
//
//        if (!imgConverter.isImage(p.toString()))     throw new XImgPreviewGen.FileIsNotImageException("addElementToQueue: file not an image ["+p.toString()+"];");
//
//        final long fileSize = p.toFile().length();
//
//        final XImgPreviewGen.PreviewElement pe = new XImgPreviewGen.PreviewElement();
//        pe.setPath(p);
//        pe.setLong("fileSize", fileSize); 
//        pe.setLong("createTime", System.currentTimeMillis()); 
//
//        threadQueue.add(pe);
//    }
//
//    @Override
//    public void run() {
//        while(true) {
//            synchronized(syncObject) {
//                try {
//                    isWaiting = true;
//                    syncObject.wait();
//                    isWaiting = false;
//                } catch (InterruptedException ex) { }
//            }                
//
//            if (isExit) {
//                return;
//            }
//
////            if (isDisplayProgress) Platform.runLater(() -> { XImg.getProgressListener().OnStartThread(threadQueue.size(), this.hashCode()); });
//            final WriteBatch threadBatch;
//            synchronized(XImg.getDB(queneName)) {
//                threadBatch = XImg.getDB(queneName).createWriteBatch();
//            }
//
//            while (true) {
//                final XImgPreviewGen.PreviewElement pe = threadQueue.poll();
//                if (pe == null) {
//                    try {
//                        synchronized(XImg.getDB(queneName)) {    
////                            if (isDisplayProgress) Platform.runLater(() -> { XImg.getProgressListener().OnInfoUpdate(this.hashCode(), "Writing preview to DB..."); });
//                            XImg.getDB(queneName).write(threadBatch, new WriteOptions().sync(true));
//                            threadBatch.close();
//                        }
//
//                        if (myType == XImg.PreviewType.previews) {
////                            if (isDisplayProgress) Platform.runLater(() -> { XImg.getProgressListener().OnInfoUpdate(this.hashCode(), "Writing metadata to DB..."); });
//
//                            hibSession = HibernateUtil.getNewSession();
//                            HibernateUtil.beginTransaction(hibSession);
//                            processedQueue.stream().map((p) -> new DSImage(p.getMD5())).forEach((di) -> {
//                                hibSession.save(di);
//                            });
//                            HibernateUtil.commitTransaction(hibSession);
//                            hibSession.close();
//
//                            int counterA = 0;
//                            final int queneCount = processedQueue.size();
////                            if (isDisplayProgress) Platform.runLater(() -> { XImg.getProgressListener().OnInfoUpdate(this.hashCode(), "Copying files..."); });
//                            for (XImgPreviewGen.PreviewElement p : processedQueue) {
//                                counterA++;
//                                tempString = "Copying files "+counterA+" of "+queneCount+"...";
//                                if (counterA > 128) {
////                                    if (isDisplayProgress) Platform.runLater(() -> { XImg.getProgressListener().OnInfoUpdate(this.hashCode(), tempString); });
//                                }
////                                try {
////                                    XImgDatastore.pushFile(p.getMD5(), p.getPath());
////                                } catch (Exception ex) {
////                                    Logger.getLogger(XImgPreviewGen.class.getName()).log(Level.SEVERE, null, ex);
////                                }
//                            }
//                        }
//
////                        if (isDisplayProgress) Platform.runLater(() -> { XImg.getProgressListener().OnComplete(this.hashCode()); });
//                        break;
//                    } catch (IOException ex) {
//                        L("cannot close db; " + ex.getMessage());
//                    }
//                }
//
//                try {
//                    final byte[] md5e = getFilePartMD5(pe.getFile().getAbsolutePath());
//                    pe.setMD5(md5e);
//                    final XImgPreviewGen.PreviewElement peDB = readEntry(imCrypt, md5e);
//                    final Image im = peDB.getImage(imCrypt, XImg.getPSizes().getPrimaryPreviewSize().getPrevName());
//                    if (im != null) 
//                        actionListenerX.OnPreviewGenerateComplete(im, peDB.getPath()); 
//
//                } catch (XImgPreviewGen.RecordNotFoundException ex) {
//                    try {
//                        final byte[] md5e = getFilePartMD5(pe.getFile().getAbsolutePath());
//                        XImg.getPSizes().getPreviewSizes().stream().forEach((c) -> {
//                            try {
//                                imgConverter.setPreviewSize((int) c.getWidth(), (int) c.getHeight(), c.isSquared());
//                                final byte preview[] = imgConverter.getPreviewFS(pe.getFile().getAbsolutePath());
//                                final byte previewCrypted[] = imCrypt.Crypt(preview);
//
//                                pe.setCryptedImageBytes(previewCrypted, c.getPrevName());
//                            } catch (IOException ex1) {
//                                L("cannot insert image from db; c=" + c.getPrevName() + "; " + ex1.getMessage());
////                                if (isDisplayProgress) Platform.runLater(() -> { XImg.getProgressListener().OnError(this.hashCode()); });
//                            }    
//                        });
//
//                        final Image im = pe.getImage(imCrypt, XImg.getPSizes().getPrimaryPreviewSize().getPrevName());
//                        if (im != null) {
//                            actionListenerX.OnPreviewGenerateComplete(im, pe.getPath());
////                            if (isDisplayProgress) Platform.runLater(() -> { XImg.getProgressListener().OnNewItemGenerated(threadQueue.size(), pe.getPath(), this.hashCode(), queneName); });
//                        } else {
////                            if (isDisplayProgress) Platform.runLater(() -> { XImg.getProgressListener().OnError(this.hashCode()); });
//                        }
//
//                        addEntryToBatch(threadBatch, imCrypt, md5e, pe);
//
//                    } catch (Error e) {
//                        L("cannot insert image from db; RTE; " + e.getMessage());
////                        if (isDisplayProgress) Platform.runLater(() -> { XImg.getProgressListener().OnError(this.hashCode()); });
//                    } catch (XImgPreviewGen.RecordNotFoundException ex1) {
////                        if (isDisplayProgress) Platform.runLater(() -> { XImg.getProgressListener().OnError(this.hashCode()); });
//                    } catch (IOException ex1) {
//                        L("cannot insert image from db; " + ex1.getMessage());
////                        if (isDisplayProgress) Platform.runLater(() -> { XImg.getProgressListener().OnError(this.hashCode()); });
//                    }
//                } catch (ClassNotFoundException | IOException ex) {
//                    L("cannot load image from db; " + ex.getMessage());
////                    if (isDisplayProgress) Platform.runLater(() -> { XImg.getProgressListener().OnError(this.hashCode()); });
//                }
//            }
//        }
//    }
//
//    @SuppressWarnings("ConvertToTryWithResources")
//    private void addEntryToBatch(WriteBatch b, XImgCrypto c, byte[] md5b, XImgPreviewGen.PreviewElement e) throws IOException {
//        if (e == null) throw new IOException("array is a null;");
//
//        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        final ObjectOutputStream oos = new ObjectOutputStream(baos);
//        oos.writeObject(e);
//        oos.flush();
//
//        final byte[] crypted = c.Crypt(baos.toByteArray());
//        if (crypted == null) throw new IOException("Crypt() return null;");
//
//        b.put(md5b, crypted);
//
//        oos.close();
//        baos.close();
//
//        processedQueue.add(e);
//    }
//
//    @SuppressWarnings("ConvertToTryWithResources")
//    public byte[] getFilePartMD5(String path) throws IOException {
//        try {
//            final FileInputStream fis = new FileInputStream(path);
//            FileChannel fc = fis.getChannel();
//            fc.position(0);
//            final ByteBuffer bb = ByteBuffer.allocate(FILE_PART_SIZE_FOR_CHECKING_MD5);
//            int counter = fc.read(bb);
//            fc.close();
//            fis.close();
//            if (counter > 0) {
//                if (counter == FILE_PART_SIZE_FOR_CHECKING_MD5) 
//                    return imCrypt.MD5(bb.array(), imCrypt.getSalt());
//                else {
//                    final ByteBuffer bb_cutted = ByteBuffer.allocate(counter);
//                    bb_cutted.put(bb.array(), 0, counter);
//                    return imCrypt.MD5(bb_cutted.array(), imCrypt.getSalt());
//                }
//            } else 
//                throw new IOException("cannot calculate MD5 for file ["+path+"]");
//        } catch (IOException ex) {
//            throw new IOException("cannot calculate MD5 for file ["+path+"], " + ex.getMessage());
//        }
//    }
//
//    private XImgPreviewGen.PreviewElement readEntry(XImgCrypto c, byte[] md5b) throws XImgPreviewGen.RecordNotFoundException, IOException, ClassNotFoundException {
//        if (XImg.getDB(queneName) == null) throw new IOException("database not opened;");
//        
//        final DB db = XImg.getDB(queneName);
//        byte[] retnc;
//        synchronized (db) {
//            retnc = db.get(md5b);
//            if (retnc == null ) throw new XImgPreviewGen.RecordNotFoundException("");
//        }
//        
//        final byte[] ret = c.Decrypt(retnc);
//        if (ret == null ) throw new IOException("Decrypt() return null value;");
//        
//        final ByteArrayInputStream bais = new ByteArrayInputStream(ret);
//        final ObjectInputStream oos = new ObjectInputStream(bais);
//        final XImgPreviewGen.PreviewElement retVal = (XImgPreviewGen.PreviewElement) oos.readObject();
//        if (retVal == null ) throw new IOException("readObject() return null value;");
//        
//        return retVal;
//    }
}
