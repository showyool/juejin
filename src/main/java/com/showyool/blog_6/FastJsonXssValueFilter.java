package com.showyool.blog_6;

import com.alibaba.fastjson.serializer.ValueFilter;

public class FastJsonXssValueFilter implements ValueFilter {

    @Override
    public Object process(Object object, String name, Object value) {
        if (value instanceof String) {
            return XssHttpServletRequestWrapper.cleanXSS(String.valueOf(value));
        }
        return value;
    }

}