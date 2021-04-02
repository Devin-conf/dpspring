package com.dp.mvcframework.context;

import com.dp.mvcframework.beans.DPBeanWapper;
import com.dp.mvcframework.beans.config.DPBeanDefinition;
import com.dp.mvcframework.beans.support.DPBeanDefinitionReader;
import com.dp.mvcframework.myannotation.DPAutowired;
import com.dp.mvcframework.myannotation.DPController;
import com.dp.mvcframework.myannotation.DPService;
import com.google.common.collect.Maps;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @auther: liudaping
 * @description: 上下文  职责是创建 DI  bean
 * @date: 2021-03-24
 * @since 1.0.0
 */
public class DPApplicationContext {

    private String[] configLocatinos;


    private Map<String, DPBeanDefinition> beanDefinitionMap = Maps.newHashMap();


    //封装bean 缓存
    private Map<String, DPBeanWapper> factoryBeanInstaceCache = Maps.newHashMap();


    //原生对象缓存

    private Map<String, Object> factoryBeanObjectCache = Maps.newHashMap();


    private DPBeanDefinitionReader reader;

    public DPApplicationContext(String... configLocations) {
        try {
            //1.加载配置文件
            reader = new DPBeanDefinitionReader(configLocations);

            //读取配置文件     //解析配置文件         //封装beanDefinition
            List<DPBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();


            //保存内容 享元模式
            doRegistBeanDefinition(beanDefinitions);

            //注入
            doAutowrited();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void doAutowrited() {

        //所有的bean 还没有实例化 还是在配置阶段
        for (Map.Entry<String, DPBeanDefinition> beanDefinitionEntry: beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            getBean(beanName);
        }


        //getBean();
    }

    private void doRegistBeanDefinition(List<DPBeanDefinition> beanDefinitions) throws Exception {
        for (DPBeanDefinition beanDefinition: beanDefinitions) {
            if (beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new Exception("The bean  has exist ");
            }
            beanDefinitionMap.put(beanDefinition.getBeanNameClassName(), beanDefinition);
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
        }

    }


    public Object getBean(String beanName) {
        //先拿到配置信息

        DPBeanDefinition beanDefinition = beanDefinitionMap.get(beanName);

        //反射拿到实例化对象

        Object instance = instantiateBean(beanName, beanDefinition);

        //封装成一个beanWapper
        DPBeanWapper beanWapper = new DPBeanWapper(instance);

        //保存

        factoryBeanInstaceCache.put(beanName, beanWapper);

        //执行依赖注入
        populateBean(beanName, beanDefinition, beanWapper);

        return beanWapper.getWapperInstance();

    }


    /**
     * 依赖注入 DI
     *
     * @param beanName
     * @param beanDefinition
     * @param beanWapper
     */
    private void populateBean(String beanName, DPBeanDefinition beanDefinition, DPBeanWapper beanWapper) {

        //涉及到循环依赖， 用两次for 循环解决

        Object innstance = beanWapper.getWapperInstance();
        Class<?> clszz = beanWapper.getWapperClass();

        //在spring中 是 @Component
        if (!(clszz.isAnnotationPresent(DPController.class) || clszz.isAnnotationPresent(DPService.class))) {
            return;
        }
        //把所有包 public private
        for (Field field: clszz.getDeclaredFields()) {
            if (!field.isAnnotationPresent(DPAutowired.class)) {
                continue;
            }

            DPAutowired autowired = field.getAnnotation(DPAutowired.class);

            //判断用户如果没有自定义beaname， 就默认类型注入
            String autowireBeanName = autowired.value().trim();
            if ("".equals(autowireBeanName)) {
                autowireBeanName = field.getType().getName();
            }

            //暴力访问
            field.setAccessible(true);

            try {
                if (this.factoryBeanInstaceCache.get(autowireBeanName) == null) {
                    continue;
                }
                field.set(innstance, this.factoryBeanInstaceCache.get(autowireBeanName).getWapperInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    /**
     * 创建真实实例对象
     *
     * @param beanName
     * @param beanDefinition
     * @return
     */
    private Object instantiateBean(String beanName, DPBeanDefinition beanDefinition) {
        String className = beanDefinition.getBeanNameClassName();
        Object instance = null;
        try {
            if (factoryBeanObjectCache.containsKey(beanName)) {
                instance = factoryBeanObjectCache.get(beanName);
            } else {
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();
                this.factoryBeanObjectCache.put(beanName, instance);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }
        return instance;

    }

    public Object getBean(Class beanClass) {
        return getBean(beanClass.getName());
    }


    public int getBeanDefinnitionCount() {
        return beanDefinitionMap.size();
    }

    public String[] getBeanDefinitionName() {
        return this.beanDefinitionMap.keySet().toArray(new String[beanDefinitionMap.size()]);

    }


    public Properties getConfig() {
        return this.reader.getConfig();
    }


    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

}
