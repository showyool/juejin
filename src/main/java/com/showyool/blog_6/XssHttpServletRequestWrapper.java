package com.showyool.blog_6;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.util.Enumeration;

/**
 * XSS过滤处理
 *
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    /**
     * 默认XSS过滤处理白名单
     */
    public final static Whitelist DEFAULT_WHITE_LIST = Whitelist.relaxed().addAttributes(":all", "style");

    /**
     * 唯一构造器
     *
     * @param request HttpServletRequest
     */
    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getHeader(String name) {
        return super.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return super.getHeaders(name);
    }

    @Override
    public String getRequestURI() {
        return super.getRequestURI();
    }

    /**
     * 需要手动调用此方法，SpringMVC默认应该使用getParameterValues来封装的Model
     * 但是又有一个问题，如果Controller只显示调用一个参数，其他参数又一起走的此方法，不得其解
     */
    @Override
    public String getParameter(String name) {
        String parameter = super.getParameter(name);
        if (parameter != null) {
            return cleanXSS(parameter);
        }
        return null;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values != null) {
            int length = values.length;
            String[] escapeValues = new String[length];
            for (int i = 0; i < length; i++) {
                if (null != values[i]) {
                    // 防xss攻击和过滤前后空格
                    escapeValues[i] = cleanXSS(values[i]);
                } else {
                    escapeValues[i] = null;
                }
            }
            return escapeValues;
        }
        return null;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return super.getInputStream();
    }


    public static String cleanXSS(String value) {
        if (value != null) {
            value = StringUtils.trim(Jsoup.clean(value, DEFAULT_WHITE_LIST));
        }
        return value;
    }
}