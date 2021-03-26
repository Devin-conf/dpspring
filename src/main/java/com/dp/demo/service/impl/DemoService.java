package com.dp.demo.service.impl;

import com.dp.demo.service.IDemoService;
import com.dp.mvcframework.myannotation.DPController;

/**
 * 核心业务逻辑
 */
@DPController
public class DemoService implements IDemoService {

    public String get(String name) {
        return "My name is " + name + ",from service.";
    }

}
