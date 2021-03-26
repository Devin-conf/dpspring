package com.dp.mvcframework.webmvc.servlet;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.dp.mvcframework.context.DPApplicationContext;
import com.dp.mvcframework.myannotation.DPAutowired;
import com.dp.mvcframework.myannotation.DPController;
import com.dp.mvcframework.myannotation.DPRequestMapping;
import com.dp.mvcframework.myannotation.DPRequestParam;
import com.dp.mvcframework.myannotation.DPService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static com.dp.mvcframework.util.StrUtil.toLowFirstCase;

/**
 * @auther: liudaping
 * @description: 第一个版本 手写dispatch  职责负责任务调用 请求分发
 * @date: 2021-03-23
 * @since 1.0.0
 */
public class DPDispatchSevlet extends HttpServlet {

    private DPApplicationContext dpApplicationContext;

    private Properties contextConfig = new Properties();

    //享元模式
    private List<String> classNames = Lists.newArrayList();


    //ioc容器 key 类名小写 value 对应对的实例
    private Map<String, Object> ioc = Maps.newConcurrentMap();


    private Map<String, Method> handlerMapping = Maps.newConcurrentMap();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //6 通过url 委派返回
        try {
            doDispatch(req, resp);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void init(ServletConfig config) throws ServletException {

        dpApplicationContext = new DPApplicationContext(config.getInitParameter("contextConfigLocation"));


//        //1 加载配置文件
//        doLoadConfig(config.getInitParameter("contextConfigLocation"));
//
//        //2 扫描先相关的类
//        doScanner(contextConfig.getProperty("scanPackage"));
//
//        /// ==========================ioc部分==========================
//
//        //3 初始化ioc
//        try {
//            doInstance();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        //==========================aop==========================
//
//
//        //========================== di部分==========================
//
//        //4 完成依赖注入
//        doAutowired();
//
//
//        //========================== mvc 部分==========================
//
//        //5 初始化handlermapping
//        doInitHandlerMapping();

        //6 后面的dopost部分

        System.out.println("-----");


    }

    private void doInitHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry: ioc.entrySet()) {

            Class<?> clazz = entry.getValue().getClass();

            if (!clazz.isAnnotationPresent(DPController.class)) {
                continue;
            }

            //类上面的路径
            String baseUrl = "";

            if (clazz.isAnnotationPresent(DPRequestMapping.class)) {
                DPRequestMapping requestMapping = clazz.getAnnotation(DPRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            for (Method method: clazz.getMethods()) {


                if (!method.isAnnotationPresent(DPRequestMapping.class)) {
                    continue;
                }

                DPRequestMapping requestMapping = method.getAnnotation(DPRequestMapping.class);
                String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
//                String url = "/" + baseUrl + "/" + requestMapping.value();
                handlerMapping.put(url, method);
                System.out.println("Mapped: " + url + method);
            }

        }


    }

    private void doAutowired() {
        if (ioc.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry: ioc.entrySet()) {

            //把所有包 public private
            for (Field field: entry.getValue().getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(DPAutowired.class)) {
                    continue;
                }

                DPAutowired autowired = field.getAnnotation(DPAutowired.class);

                //判断用户如果没有自定义beaname， 就默认类型注入
                String beanName = autowired.value().trim();
                if ("".equals(beanName)) {
                    beanName = field.getType().getName();
                }


                //暴力访问
                field.setAccessible(true);

                try {
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void doInstance() throws Exception {
        if (classNames.isEmpty()) {
            return;
        }

        for (String className: classNames) {
            try {
                Class<?> clazz = Class.forName(className);

                if (clazz.isAnnotationPresent(DPController.class)) {
                    String beanName = toLowFirstCase(clazz.getSimpleName());
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);
                } else if (clazz.isAnnotationPresent(DPService.class)) {

                    //多个包出现相同的类名  只能起一个全局唯一的名字
//                    String beanName = clazz.getAnnotation(DPService.class);

                    String beanName = toLowFirstCase(clazz.getSimpleName());
                    if ("".equals(beanName.trim())) {
                        beanName = toLowFirstCase(clazz.getSimpleName());
                    }
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);
                    //默认类小写


                    //如果是接口 下面很多实现类呢  就抛出异常


                    for (Class<?> i: clazz.getInterfaces()) {

                        if (ioc.containsKey(i.getName())) {
                            throw new Exception("the " + i.getName() + "is exist");
                        }
                        ioc.put(i.getName(), instance);
                    }

                } else {
                    continue;
                }


            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
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
                classNames.add(className);
            }

        }
    }

    private void doLoadConfig(String contextconfigLocation) {

        InputStream is = this.getClass().getClassLoader().getResourceAsStream(contextconfigLocation);

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


    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException {

        String url = req.getRequestURI();

        String contextPath = req.getContextPath();

        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");

        if (!this.handlerMapping.containsKey(url)) {
            resp.getWriter().write("404 not found");
            return;
        }

        Map<String, String[]> params = req.getParameterMap();

        Method method = this.handlerMapping.get(url);

        //获取形参列表

        Class<?>[] paramterTypes = method.getParameterTypes();

        Object[] paramValues = new Object[paramterTypes.length];

        for (int i = 0; i < paramterTypes.length; i++) {
            Class paramterType = paramterTypes[i];
            if (paramterType == HttpServletRequest.class) {
                paramValues[i] = req;
            } else if (paramterType == HttpServletResponse.class) {
                paramValues[i] = resp;
            } else if (paramterType == String.class) {
                //通过运行时的状态去拿到你
                Annotation[][] pa = method.getParameterAnnotations();
                for (int j = 0; j < pa.length; j++) {
                    for (Annotation a: pa[i]) {
                        if (a instanceof DPRequestParam) {
                            String paramName = ((DPRequestParam) a).value();
                            if (!"".equals(paramName.trim())) {
                                String value = Arrays.toString(params.get(paramName))
                                        .replaceAll("\\[|\\]", "")
                                        .replaceAll("\\s+", ",");
                                paramValues[i] = value;
                            }
                        }
                    }
                }

            }


            String beanName = toLowFirstCase(method.getDeclaringClass().getSimpleName());
            //反射调用
            method.invoke(ioc.get(beanName), paramValues);


        }
    }
}