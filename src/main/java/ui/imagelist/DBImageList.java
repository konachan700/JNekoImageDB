package ui.imagelist;

import dao.ImageId;
import fao.ImageFile;
import service.RootService;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class DBImageList extends AbstractImageList {
    private long[] imageCache;



    public DBImageList() {
        super();



    }

    public void refresh() {
        fillImagesList(getCurrentPage(), getPagesCount());
    }

    @Override
    public List<ImageFile> imageRequest(int page, int pages) {
        final int from = getElementsPerPage() * page;
        final long[] ids = new long[getElementsPerPage()];
        final int realLen = imageCache.length - from;
        System.arraycopy(imageCache, from, ids, 0, Math.min(getElementsPerPage(), realLen));

        final List<ImageId> list = RootService.getDaoService().getImageIdList(ids);
        if (Objects.isNull(list)) return null;

        final List<ImageFile> fileList = list.stream()
                .map(iid -> new ImageFile(iid))
                .collect(Collectors.toList());

        return fileList;
    }

    @Override
    public Set<ImageFile> selectedRequest() {
        return new HashSet<>(); // TODO: add selected support
    }

    @Override
    public void onSelect(ImageFile imageFile, int index, boolean selected) {

    }

    @Override
    public void OnRightClick(ImageFile imageFile, int index) {

    }

    public void regenerateCache() {
        imageCache = RootService.getDaoService().generateCache();
        if (Objects.isNull(imageCache)) return;

        final int lastPageCount = imageCache.length % getElementsPerPage();
        final int inFullPageCount = imageCache.length - lastPageCount;
        final int pagesCount1 = inFullPageCount / getElementsPerPage();
        final int pagesCount2 = (lastPageCount > 0) ? pagesCount1 : pagesCount1 - 1;

        setPagesCount(pagesCount2);
    }
}
