package com.showyool.blog_8;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Constants {

    public static final String D_KEY = "com:showyool:key:";
    public static final String D_USER_KEY = "com:showyool:get:user:";
    public static final String USER_KEY = "com:showyool:user:";

    public static Map<Integer, String> tableMap = Maps.newHashMap();
    public static Map<Integer, String> insertMap = Maps.newHashMap();
    public static Map<Integer, String> deleteMap = Maps.newHashMap();
    public static Map<Integer, String> updateMap = Maps.newHashMap();

    public static Map<Integer, ConcurrentLinkedQueue<Tuple2<String, Object[]>>> appendSQLs = new ConcurrentHashMap<>();

    static {
        tableMap.put(0, "order_0");
        tableMap.put(1, "order_1");
        tableMap.put(2, "order_2");
        tableMap.put(3, "order_3");

        insertMap.put(0, "INSERT INTO order_0 (id, user_id, item, count) VALUES (?, ?, ?, ?)");
        insertMap.put(1, "INSERT INTO order_1 (id, user_id, item, count) VALUES (?, ?, ?, ?)");
        insertMap.put(2, "INSERT INTO order_2 (id, user_id, item, count) VALUES (?, ?, ?, ?)");
        insertMap.put(3, "INSERT INTO order_3 (id, user_id, item, count) VALUES (?, ?, ?, ?)");

        deleteMap.put(0, "DELETE FROM order_0 WHERE id = ? LIMIT 1");
        deleteMap.put(1, "DELETE FROM order_1 WHERE id = ? LIMIT 1");
        deleteMap.put(2, "DELETE FROM order_2 WHERE id = ? LIMIT 1");
        deleteMap.put(3, "DELETE FROM order_3 WHERE id = ? LIMIT 1");

        updateMap.put(0, "UPDATE order_0 SET count = ? WHERE id = ? LIMIT 1");
        updateMap.put(1, "UPDATE order_1 SET count = ? WHERE id = ? LIMIT 1");
        updateMap.put(2, "UPDATE order_2 SET count = ? WHERE id = ? LIMIT 1");
        updateMap.put(3, "UPDATE order_3 SET count = ? WHERE id = ? LIMIT 1");

    }

}
