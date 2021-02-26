package com.showyool.blog_8;

import lombok.Getter;

/**
 * 简单的二元组
 */
@Getter
public class Tuple2<T1, T2> {

    private T1 t1;
    private T2 t2;

    public Tuple2(T1 t1, T2 t2) {
        this.t1 = t1;
        this.t2 = t2;
    }

}
