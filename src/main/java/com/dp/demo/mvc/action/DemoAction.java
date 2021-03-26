package com.dp.demo.mvc.action;

import com.dp.demo.service.IDemoService;
import com.dp.mvcframework.myannotation.DPAutowired;
import com.dp.mvcframework.myannotation.DPController;
import com.dp.mvcframework.myannotation.DPRequestMapping;
import com.dp.mvcframework.myannotation.DPRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


//虽然，用法一样，但是没有功能
@DPController
@DPRequestMapping("/demo")
public class DemoAction {

    @DPAutowired
    private IDemoService demoService;

    @DPRequestMapping("/query")
    public void query(HttpServletRequest req, HttpServletResponse resp,
                      @DPRequestParam("name") String name) {
        String result = demoService.get(name);
//		String result = "My name is " + name;
        try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @DPRequestMapping("/add")
    public void add(HttpServletRequest req, HttpServletResponse resp,
                    @DPRequestParam("a") Integer a, @DPRequestParam("b") Integer b) {
        try {
            resp.getWriter().write(a + "+" + b + "=" + (a + b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @DPRequestMapping("/sub")
    public void add(HttpServletRequest req, HttpServletResponse resp,
                    @DPRequestParam("a") Double a, @DPRequestParam("b") Double b) {
        try {
            resp.getWriter().write(a + "-" + b + "=" + (a - b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @DPRequestMapping("/remove")
    public String remove(@DPRequestParam("id") Integer id) {
        return "" + id;
    }

}
