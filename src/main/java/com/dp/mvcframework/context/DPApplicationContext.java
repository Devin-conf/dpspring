package com.dp.mvcframework.context;

import com.google.common.collect.Maps;
import com.dp.mvcframework.beans.config.DPBeanDefinition;
import com.dp.mvcframework.beans.support.DPBeanDefinitionReader;

import java.util.List;
import java.util.Map;

/**
 * @auther: liudaping
 * @description: 上下文  职责是创建 DI  bean
 * @date: 2021-03-24
 * @since 1.0.0
 */
public class DPApplicationContext {

    private String[] configLocatinos;


    private Map<String, DPBeanDefinition> beanDefinitionMap = Maps.newHashMap();

    private DPBeanDefinitionReader reader;

    public DPApplicationContext(String... configLocations) {

        //1.加载配置文件
        reader = new DPBeanDefinitionReader(configLocations);

        //读取配置文件     //解析配置文件         //封装beanDefinition
        List<DPBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();


        //保存内容 享元模式
        doRegistBeanDefinition(beanDefinitions);

        //注入
        doAutowrited();


    }

    private void doAutowrited() {

        //getBean();
    }

    private void doRegistBeanDefinition(List<DPBeanDefinition> beanDefinitions) {
    }


    public Object getBean(String beanName) {
        return null;

    }

    public Object getBean(Class beanClass) {
        return getBean(beanClass.getName());
    }


}
