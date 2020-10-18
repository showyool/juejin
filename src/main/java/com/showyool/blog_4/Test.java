package com.showyool.blog_4;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;

import java.util.List;

public class Test {

    public static void main(String[] args) {

        // 获取Customer数据，这里就简单模拟
        List<Customer> customerList = Lists.newArrayList(new Customer("Java"), new Customer("Showyool"), new Customer("Soga"));
        int index = 0;
        String[][] exportData = new String[customerList.size()][2];
        for (Customer customer : customerList) {
            exportData[index][0] = String.valueOf(index);
            exportData[index][1] = customer.getName();
            index = index++;
        }
        System.out.println(JSON.toJSONString(exportData));
    }

}

class Customer {

    public Customer(String name) {
        this.name = name;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
