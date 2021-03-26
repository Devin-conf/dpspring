# dpspring
手写spring简单框架

beans
```
ApplicationContext 简单料理界工厂类 getBean() 从ioc容器中回去一个实例的方法
大流程如下
1.在调用servlet init方法后， 就要初始化ApplicationContext
默认是单利， 而且是延时加载的lazy
2.DI是在初始化以后发生的， spring中发生DI由getBean（）触发后 立即发生DI
方法步骤
1.调用servlet init()方法
2.读取配置文件--> beandefinitionReader
3.扫描相关的类--> beanDefinition--> 保存在内存中
4.初始化ioc容器，并且实例化对象 --->beanWrapper:原生对象和代理对象关联关系
5.完成di
6.handlermapping
```







