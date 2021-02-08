package com.showyool.blog_6;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/xss")
public class XssController {

    @ResponseBody
    @RequestMapping(value = "/check", method = RequestMethod.POST)
    public Object check(@RequestBody Tests tests) {
        tests.setVal(100);
        tests.setKey("hello<script>123</script>");
        return tests;
    }

}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Tests {

    private String key;
    private Integer val;

}