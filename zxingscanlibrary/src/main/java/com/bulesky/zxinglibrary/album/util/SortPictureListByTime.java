package com.bulesky.zxinglibrary.album.util;

import java.io.File;
import java.util.Comparator;

/**
 * Describe:按最后一次修改时间给图片排序
 * Created by hsl on 2017/10/10.
 */

public class SortPictureListByTime implements Comparator {
    @Override
    public int compare(Object o, Object t1) {
        String path1 = o.toString();
        String path2 = t1.toString();
        File file1 = new File(path1);
        File file2 = new File(path2);
        if (file1.lastModified() > file2.lastModified()) {
            return -1;
        } else {
            return 1;
        }
    }
}
