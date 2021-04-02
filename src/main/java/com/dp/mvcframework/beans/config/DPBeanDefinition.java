package com.dp.mvcframework.beans.config;

/**
 * @auther: liudaping
 * @description:
 * @date: 2021-03-24
 * @since 1.0.0
 */
public class DPBeanDefinition {


    private String factoryBeanName;

    //bean对应的classname
    private String beanNameClassName;

    public DPBeanDefinition(String factoryBeanName, String beanNameClassName) {
        this.factoryBeanName = factoryBeanName;
        this.beanNameClassName = beanNameClassName;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    public String getBeanNameClassName() {
        return beanNameClassName;
    }

    public void setBeanNameClassName(String beanNameClassName) {
        this.beanNameClassName = beanNameClassName;
    }
}
