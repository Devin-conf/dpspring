package com.dp.mvcframework.webmvc.servlet;

import java.util.Map;

/**
 * @auther: liudaping
 * @description:
 * @date: 2021-04-01
 * @since 1.0.0
 */
public class DPModelAndView {

    private String viewName;

    private Map<String, ?> model;

    public DPModelAndView(String viewName, Map<String, ?> model) {
        this.viewName = viewName;
        this.model = model;
    }


    public DPModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public Map<String, ?> getModel() {
        return model;
    }

    public void setModel(Map<String, ?> model) {
        this.model = model;
    }
}
