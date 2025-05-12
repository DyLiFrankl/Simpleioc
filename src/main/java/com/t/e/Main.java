package com.t.e;

import com.t.e.bean.PrototypeBean;
import com.t.e.bean.SingletonBean;
import com.t.e.service.UserService;
import com.t.e.simpleioc.SimpleIoC;
import com.t.e.test.DatabaseConnection;
import com.t.e.test.DatabaseService;
import com.t.e.test.listener.UserCreatedEvent;
import com.t.e.util.AssertUtils;


public class Main {
    public static void main(String[] args) throws Exception {
        String[] scanPackages = new String[]{"com.t.e.controller","com.t.e.service","com.t.e.bean","com.t.e.test", "com.t.e.test.listener"};
        String xmlPaths = new String("src/main/resources/META-INF/custom-beans.xml");
        //初始化IoC容器
        SimpleIoC container = new SimpleIoC(scanPackages, xmlPaths);
        Object o = container.getXmlBean("userRepository");
        //获取UserService实例
        UserService userService = container.getInstance(UserService.class);
        container.publishEvent(new UserCreatedEvent(userService, "John Doe"));
        userService.printUserName();
//        userService.getUserName();
//        container.getBean(AssertUtils.class);
        // 单例 Bean
        SingletonBean singleton1 = container.getBean(SingletonBean.class);
        SingletonBean singleton2 = container.getBean(SingletonBean.class);
        System.out.println("Is singleton1 == singleton2? " + (singleton1 == singleton2)); // true

        // 原型 Bean
        PrototypeBean prototype1 = container.getBean(PrototypeBean.class);
        PrototypeBean prototype2 = container.getBean(PrototypeBean.class);
        System.out.println("Is prototype1 == prototype2? " + (prototype1 == prototype2)); // false


        //测试@AfterThrowing
//        try{
//            userService.throwException();
//        }catch (Exception e){
//            e.printStackTrace();
//        }

        // 当条件类存在时
        try {
            DatabaseService service = container.getBean(DatabaseService.class);
            AssertUtils.assertNotNull(service, "DatabaseService Bean 为 null");
            System.out.println("ConditionOnClass && ConditionalOnMissingBean 测试通过！");
        } catch (Exception e) {
            throw new AssertionError("获取 DatabaseService Bean 失败 , 不存在该bean", e);
        }
        // 当不存在时，应抛出 NoSuchBeanDefinitionException

        //测试bean初始化方法和销毁
        DatabaseConnection db = container.getBean(DatabaseConnection.class);
        container.close();
    }
}
