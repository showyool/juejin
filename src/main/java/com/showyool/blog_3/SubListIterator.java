package com.showyool.blog_3;

import java.util.*;

public class SubListIterator<T> implements Iterator {

    /**
     * 原数据列表
     */
    private List<T> dataList;

    /**
     * 分隔单位
     */
    private int subSize;

    private volatile int nextIndex = 0;

    private int listSize;

    private List<List<T>> subLists = new ArrayList<>();

    public SubListIterator(List<T> dataList, int subSize){
        if(dataList != null){
            listSize = dataList.size();
        }

        this.dataList = dataList;
        this.subSize = subSize;

        initSubLists();
    }

    private void initSubLists(){
        if(listSize <= 0){
            return;
        }

        int index = 1;

        Iterator<T> iterator = dataList.iterator();

        List<T> subDataList = new ArrayList();
        while(iterator.hasNext()){
            T next = iterator.next();

            subDataList.add(next);
            if (index % subSize == 0 || listSize == index) {
                subLists.add(subDataList);
                subDataList = new ArrayList();
            }
            index++;
        }
    }


    @Override
    public boolean hasNext() {
        return subLists.size() > nextIndex;
    }

    @Override
    public Object next() {
        if(hasNext()) {
            return subLists.get(nextIndex++);
        }

        return null;
    }

    @Override
    public void remove() {
        new UnsupportedOperationException("不支持该操作");
    }

}
