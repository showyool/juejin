package com.showyool.blog_8;

import lombok.Data;

/**
 * 表对象
 *
 */
@Data
public class Order {

    private Long id;
    private int user_id;
    private String item;
    private int count;

}
