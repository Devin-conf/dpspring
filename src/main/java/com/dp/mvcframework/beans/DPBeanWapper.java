package com.dp.mvcframework.beans;

/**
 * @auther: liudaping
 * @description: 装饰bean
 * @date: 2021-03-26
 * @since 1.0.0
 */
public class DPBeanWapper {

    private Object wapperInstance;

    private Class<?> wapperClass;


    public DPBeanWapper(Object instance){
        this.wapperClass = instance.getClass();
        this.wapperInstance = instance;
    }

    public Object getWapperInstance() {
        return wapperInstance;
    }

    public void setWapperInstance(Object wapperInstance) {
        this.wapperInstance = wapperInstance;
    }

    public Class<?> getWapperClass() {
        return wapperClass;
    }

    public void setWapperClass(Class<?> wapperClass) {
        this.wapperClass = wapperClass;
    }
}

