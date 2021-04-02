package com.dp.mvcframework.webmvc.servlet;

import com.dp.mvcframework.context.DPApplicationContext;
import com.dp.mvcframework.myannotation.DPController;
import com.dp.mvcframework.myannotation.DPRequestMapping;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private Map<DPHandlerMappinng, DPHandlerAdapter> handlerAdapters = new HashMap<DPHandlerMappinng, DPHandlerAdapter>();

//    //ioc容器 key 类名小写 value 对应对的实例
//    private Map<String, Object> ioc = Maps.newConcurrentMap();


    private List<DPHandlerMappinng> dpHandlerMappinngs = Lists.newArrayList();


    private Map<String, Method> handlerMapping = Maps.newConcurrentMap();


    private List<DPViewResolver> viewResolvers = Lists.newArrayList();

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
        } catch (Exception e) {
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


        //初始化九大组件

        initStrategies(dpApplicationContext);




        System.out.println("-----");


    }

    private void initStrategies(DPApplicationContext context) {

//        //多文件上传的组件
//        initMultipartResolver(context);
//        //初始化本地语言环境
//        initLocaleResolver(context);
//        //初始化模板处理器
//        initThemeResolver(context);
        //handlerMapping
        initHandlerMappings(context);
        //初始化参数适配器
        initHandlerAdapters(context);
//        //初始化异常拦截器
//        initHandlerExceptionResolvers(context);
//        //初始化视图预处理器
//        initRequestToViewNameTranslator(context);
        //初始化视图转换器
        initViewResolvers(context);
//        //FlashMap管理器
//        initFlashMapManager(context);



    }


    private void initViewResolvers(DPApplicationContext context) {
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();

        File templateRootDir = new File(templateRootPath);
        for (File file : templateRootDir.listFiles()) {
            this.viewResolvers.add(new DPViewResolver(templateRoot));
        }

    }

    private void initHandlerAdapters(DPApplicationContext context) {
        for (DPHandlerMappinng handlerMapping : dpHandlerMappinngs) {
            this.handlerAdapters.put(handlerMapping,new DPHandlerAdapter());
        }
    }



    private void initHandlerMappings(DPApplicationContext context) {
        if(this.dpApplicationContext.getBeanDefinitionCount() == 0){ return;}

        for (String beanName : this.dpApplicationContext.getBeanDefinitionNames()) {
            Object instance = dpApplicationContext.getBean(beanName);
            Class<?> clazz = instance.getClass();

            if(!clazz.isAnnotationPresent(DPController.class)){ continue; }

            //相当于提取 class上配置的url
            String baseUrl = "";
            if(clazz.isAnnotationPresent(DPRequestMapping.class)){
                DPRequestMapping requestMapping = clazz.getAnnotation(DPRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            //只获取public的方法
            for (Method method : clazz.getMethods()) {
                if(!method.isAnnotationPresent(DPRequestMapping.class)){continue;}
                //提取每个方法上面配置的url
                DPRequestMapping requestMapping = method.getAnnotation(DPRequestMapping.class);

                // //demo//query
                String regex = ("/" + baseUrl + "/" + requestMapping.value().replaceAll("\\*",".*")).replaceAll("/+","/");
                Pattern pattern = Pattern.compile(regex);
                //handlerMapping.put(url,method);
                dpHandlerMappinngs.add(new DPHandlerMappinng(pattern,instance,method));
                System.out.println("Mapped : " + regex + "," + method);
            }

        }
    }


    private void doInitHandlerMapping() {
        if (this.dpApplicationContext.getBeanDefinnitionCount() == 0) {
            return;
        }
        for (String beanName: this.dpApplicationContext.getBeanDefinitionName()) {

            Class<?> clazz = dpApplicationContext.getBean(beanName).getClass();

            Object instance = dpApplicationContext.getBean(beanName);

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
//                handlerMapping.put(url, method);
                dpHandlerMappinngs.add(new DPHandlerMappinng(url, instance, method));
                System.out.println("Mapped: " + url + method);
            }

        }


    }

//    private void doAutowired() {
//        if (ioc.isEmpty()) {
//            return;
//        }
//
//        for (Map.Entry<String, Object> entry: ioc.entrySet()) {
//
//            //把所有包 public private
//            for (Field field: entry.getValue().getClass().getDeclaredFields()) {
//                if (field.isAnnotationPresent(DPAutowired.class)) {
//                    continue;
//                }
//
//                DPAutowired autowired = field.getAnnotation(DPAutowired.class);
//
//                //判断用户如果没有自定义beaname， 就默认类型注入
//                String beanName = autowired.value().trim();
//                if ("".equals(beanName)) {
//                    beanName = field.getType().getName();
//                }
//
//
//                //暴力访问
//                field.setAccessible(true);
//
//                try {
//                    field.set(entry.getValue(), ioc.get(beanName));
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//
//    }
//
//    private void doInstance() throws Exception {
//        if (classNames.isEmpty()) {
//            return;
//        }
//
//        for (String className: classNames) {
//            try {
//                Class<?> clazz = Class.forName(className);
//
//                if (clazz.isAnnotationPresent(DPController.class)) {
//                    String beanName = toLowFirstCase(clazz.getSimpleName());
//                    Object instance = clazz.newInstance();
//                    ioc.put(beanName, instance);
//                } else if (clazz.isAnnotationPresent(DPService.class)) {
//
//                    //多个包出现相同的类名  只能起一个全局唯一的名字
////                    String beanName = clazz.getAnnotation(DPService.class);
//
//                    String beanName = toLowFirstCase(clazz.getSimpleName());
//                    if ("".equals(beanName.trim())) {
//                        beanName = toLowFirstCase(clazz.getSimpleName());
//                    }
//                    Object instance = clazz.newInstance();
//                    ioc.put(beanName, instance);
//                    //默认类小写
//
//
//                    //如果是接口 下面很多实现类呢  就抛出异常
//
//
//                    for (Class<?> i: clazz.getInterfaces()) {
//
//                        if (ioc.containsKey(i.getName())) {
//                            throw new Exception("the " + i.getName() + "is exist");
//                        }
//                        ioc.put(i.getName(), instance);
//                    }
//
//                } else {
//                    continue;
//                }
//
//
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InstantiationException e) {
//                e.printStackTrace();
//            }
//        }
//
//
//    }


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


    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        //1、通过URL获得一个HandlerMapping
        DPHandlerMappinng dpHandler = getHandlerMapping(req);

        if (dpHandler == null) {
            processDispatchResult(req, resp, new DPModelAndView("404"));
            return;
        }

        //2 根据一个hangderMapping 获得一个HanndlerAdapter
        DPHandlerAdapter ha = getHandlerAdapter(dpHandler);

        //3 解析某一个方法的形参和返 回值后 统一封装为ModelAndView对象
        DPModelAndView mv = ha.handler(req, resp, dpHandler);

        //4把ModelAndView 变成一个viewResolver
        processDispatchResult(req, resp, mv);

    }

    private DPHandlerAdapter getHandlerAdapter(DPHandlerMappinng dpHandler) {
        if (this.handlerAdapters.isEmpty()) {
            return null;
        }
        return this.handlerAdapters.get(dpHandler);

    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, DPModelAndView mv) throws Exception {
        if (mv == null) {
            return;
        }
        if (this.viewResolvers.isEmpty()) {
            return;
        }

        for (DPViewResolver viewResolver: viewResolvers) {
            DPView view = viewResolver.resolveViewName(mv.getViewName());
            view.render(mv.getModel(), req, resp);
            return;

        }


    }

    private DPHandlerMappinng getHandlerMapping(HttpServletRequest req) {
        if (this.dpHandlerMappinngs.isEmpty()) {
            return null;
        }
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");

        for (DPHandlerMappinng handlerMappinng: dpHandlerMappinngs) {
            Matcher matcher = handlerMappinng.getPattern().matcher(url);
            if (!matcher.matches()) {
                continue;
            }
            return handlerMappinng;
        }
        return null;
    }


}