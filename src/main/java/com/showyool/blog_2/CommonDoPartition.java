package com.showyool.blog_2;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommonDoPartition<T> {

    private final static Logger logger = Logger.getLogger(CommonDoPartition.class);

    public static final int PARTITION_SIZE = 1000;

    public void partitionToDo(int partitionSize, List<T> all, Consumer<List<T>> consumer) {
        if (CollectionUtils.isEmpty(all)) {
            logger.warn("no data to consume");
            return;
        }
        // 去重
        all = all.stream().distinct().collect(Collectors.toList());

        List<List<T>> partitions = Lists.partition(all, partitionSize);
        for (List<T> list : partitions) {
            consumer.accept(list);
        }
    }

    public void partitionToDo(List<T> all, Consumer<List<T>> consumer) {
        this.partitionToDo(PARTITION_SIZE, all, consumer);
    }

    public <T, R> List<R> partitionToQuery(int partitionSize, List<T> all, Function<List<T>, List<R>> function) {
        if (CollectionUtils.isEmpty(all)) {
            logger.warn("no data to consume");
            return Lists.newArrayList();
        }
        // 去重
        all = all.stream().distinct().collect(Collectors.toList());

        List<List<T>> partitions = Lists.partition(all, partitionSize);
        List<R> result = Lists.newArrayList();
        for (List<T> list : partitions) {
            List<R> resultList = function.apply(list);
            if (!CollectionUtils.isEmpty(resultList)) {
                result.addAll(resultList);
            }
        }
        return result;
    }

    public <R, Z> Map<R, Z> partitionToQuery4Map(int partitionSize, List<T> all, Function<List<T>, Map<R, Z>> function) {
        if (CollectionUtils.isEmpty(all)) {
            logger.warn("no data to consume");
            return Maps.newHashMap();
        }
        // 去重
        all = all.stream().distinct().collect(Collectors.toList());

        List<List<T>> partitions = Lists.partition(all, partitionSize);
        Map<R, Z> result = Maps.newHashMap();
        for (List<T> list : partitions) {
            Map<R, Z> resultList = function.apply(list);
            if (resultList != null && resultList.size() > 0) {
                result.putAll(resultList);
            }
        }
        return result;
    }

    public <T, R> List<R> partitionToQuery(List<T> all, Function<List<T>, List<R>> function) {
        return this.partitionToQuery(PARTITION_SIZE, all, function);
    }

    public <R, Z> Map<R, Z> partitionToQuery4Map(List<T> all, Function<List<T>, Map<R, Z>> function) {
        return this.partitionToQuery4Map(PARTITION_SIZE, all, function);
    }

    public <T, R> void partitionToQueryAndDo(int partitionSize, List<T> all, Function<List<T>, List<R>> function, Consumer<List<R>> consumer) {
        if (CollectionUtils.isEmpty(all)) {
            logger.warn("no data to consume");
            return;
        }

        List<List<T>> partitions = Lists.partition(all, partitionSize);
        List<R> resultList;
        for (List<T> list : partitions) {
            resultList = function.apply(list);
            consumer.accept(resultList);
        }
    }

    public <T, R> void partitionToQueryAndDo(List<T> all, Function<List<T>, List<R>> function, Consumer<List<R>> consumer) {
        this.partitionToQueryAndDo(PARTITION_SIZE, all, function, consumer);
    }

}
