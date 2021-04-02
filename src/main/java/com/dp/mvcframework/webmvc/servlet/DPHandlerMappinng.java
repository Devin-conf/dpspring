package com.dp.mvcframework.webmvc.servlet;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * @auther: liudaping
 * @description: metod---mapping
 * @date: 2021-03-28
 * @since 1.0.0
 */
public class DPHandlerMappinng {

    private Pattern pattern;     //URL
    private String url;

    private Method method;

    /**
     * method --->实例对象
     */
    private Object controller;

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public DPHandlerMappinng(String url, Object controller, Method method) {
        this.url = url;
        this.method = method;
        this.controller = controller;
    }

    public DPHandlerMappinng(Pattern pattern, Object controller, Method method) {
        this.pattern = pattern;
        this.method = method;
        this.controller = controller;
    }

    public DPHandlerMappinng(Pattern pattern, String url, Method method) {
        this.pattern = pattern;
        this.url = url;
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }
}
