package com.dp.mvcframework.beans.support;

import com.dp.mvcframework.beans.config.DPBeanDefinition;
import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import static com.dp.mvcframework.util.StrUtil.toLowFirstCase;

/**
 * @auther: liudaping
 * @description: 基础支撑 工具类
 * @date: 2021-03-24
 * @since 1.0.0
 */
public class DPBeanDefinitionReader {

    private String[] configLocations;

    private Properties contextConfig = new Properties();



    //保存扫描的结果
    private List<String> registryBeanClasses = Lists.newArrayList();

    public DPBeanDefinitionReader(String[] configLocations) {
        doLoadConfig(configLocations[0]);

        //扫描配置相关的类
        doScanner(contextConfig.getProperty("scanPackage"));

    }

    /**
     * 配置信息变DPBeanDefinition
     *
     * @return
     */
    public List<DPBeanDefinition> loadBeanDefinitions() {
        List<DPBeanDefinition> result = Lists.newArrayList();
        try {
            for (String className: registryBeanClasses) {

                Class<?> beanClass = Class.forName(className);
                //如果是接口的话  就不添加， 因为下面有一个 单独处理接口注入
                if(beanClass.isInterface()){continue;}

                //保存类 对应的classname
                //还有beanname 1. 默认类名首字母小写 2 自定义  3 接口
                String beanName = beanClass.getSimpleName();
                String beanClassName = beanClass.getName();

                //单独处理 如果是接口的话， beanName = 接口名字  beanclassname 是 实现类的名字
                result.add(doCreateBeandefinition(toLowFirstCase(beanName), beanClassName));

                //完成接口注入
                for (Class clazz : beanClass.getInterfaces()){
                    result.add(doCreateBeandefinition(toLowFirstCase(clazz.getName()), beanClass.getName()));

                }

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return result;
    }


    private DPBeanDefinition doCreateBeandefinition(String beanName, String beanClassName) {
        return new DPBeanDefinition(beanName, beanClassName);

    }


    private void doLoadConfig(String contextconfigLocation) {

        //源码策略模式
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextconfigLocation.replaceAll("classpath:", ""));

        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void doScanner(String scanpackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanpackage.replaceAll("\\.", "/"));
        File classPath = new File(url.getFile());
        //当做一个classpath 文件
        for (File file: classPath.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanpackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String className = scanpackage + "." + file.getName().replace(".class", "");
                registryBeanClasses.add(className);
            }

        }
    }

    public Properties getConfig(){
        return this.contextConfig;
    }
}
