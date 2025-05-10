package com.t.e.simpleioc;

import com.t.e.aop.*;
import com.t.e.aop.ProceedingJoinPoint;
import com.t.e.condition.Condition;
import com.t.e.condition.Conditional;
import com.t.e.condition.SimpleConditionContext;
import com.t.e.init.PostConstruct;
import com.t.e.init.PreDestroy;
import com.t.e.listener.ApplicationEvent;
import com.t.e.listener.ApplicationListener;
import com.t.e.listener.EventListener;
import com.t.e.simpleioc.annotations.Autowired;
import com.t.e.simpleioc.annotations.Component;
import com.t.e.simpleioc.annotations.Scope;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.t.e.util.PropertyUtils;
import com.t.e.util.SimpleParameterNameDiscoverer;
import com.t.e.util.XmlBeanDefinitionReader;
import com.t.e.xml.BeanDefinition;
import com.t.e.xml.BeanReference;
import com.t.e.xml.ConstructorArg;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;



public class SimpleIoC {

    public ContainerState getState() {
        return state;
    }

    @FunctionalInterface
    public interface ObjectFactory<T> {
        T getObject() throws Exception; // 用于生成对象
    }
    // 添加切点信息缓存类
    private static class PointcutInfo {
        String classNamePattern;
        String methodNamePattern;

        PointcutInfo(String className, String methodName) {
            this.classNamePattern = className;
            this.methodNamePattern = methodName;
        }
    }
    // 三级缓存
    private final Map<String, Object> singletonObjects = new ConcurrentHashMap<>();      // 一级缓存：完整 Bean
    private final Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>(); // 二级缓存：早期引用
    private final Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>();    // 三级缓存：Bean 工厂

    private final Set<String> singletonsCurrentlyInCreation = Collections.newSetFromMap(new ConcurrentHashMap<>()); // 正在创建的 Bean

    private final Map<String, Class<?>> prototypeBeanClasses = new HashMap<>(); // 存储原型 Bean 的 Class 对象
    private Map<String, Object> beans = new HashMap<>();

    private List<Object> aspects = new ArrayList<>(); // 存储切面实例
    // 修改通知方法的Map结构，存储PointcutInfo
    private final Map<Method, PointcutInfo> aroundAdvices = new ConcurrentHashMap<>();
    private final Map<Method, PointcutInfo> beforeAdvices = new ConcurrentHashMap<>();
    private final Map<Method, PointcutInfo> afterAdvices = new ConcurrentHashMap<>();
    private final Map<Method, PointcutInfo> afterThrowingAdvices = new ConcurrentHashMap<>();
    // 切点表达式解析缓存
    private final Map<String, PointcutInfo> pointcutCache = new ConcurrentHashMap<>();

    // 存储事件类型与监听器的映射
    private final Map<Class<? extends ApplicationEvent>, List<ApplicationListener<?>>> listeners = new ConcurrentHashMap<>();
    //xml配置bean
    private final Map<String, BeanDefinition> beanDefinitions = new HashMap<>();

    private ContainerState state;
    public SimpleIoC(String[] scanPackages, String... xmlPaths) throws Exception {
        loadXmlConfiguration(xmlPaths);
        state = ContainerState.XML_LOADED;

        PropertyUtils.load("jdbc.properties");
        scanPackage(scanPackages);
        state = ContainerState.SCANNED;

        applyAspects();
        state = ContainerState.ASPECT_APPLIED;

        registerListeners();
        state = ContainerState.LISTENERS_READY;

//        injectDependencies();
        state = ContainerState.DEPENDENCIES_INJECTED;

        condition();
        state = ContainerState.CONDITIONS_CHECKED;

        // 最终状态
        state = ContainerState.READY;
    }

    private void condition() {
       singletonObjects.forEach((k, v) -> {
           if (!shouldRegisterBean(v.getClass())) {
              singletonObjects.remove(k, v);
           }
       });
    }


    private void loadXmlConfiguration(String... xmlPaths) throws Exception {
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader();
        for (String xmlPath : xmlPaths) {
            List<BeanDefinition> definitions = reader.loadBeanDefinitions(xmlPath);
            for (BeanDefinition def : definitions) {
                beanDefinitions.put(def.getId(), def);
            }
        }

        for (String beanId : beanDefinitions.keySet()) {
            BeanDefinition definition = beanDefinitions.get(beanId);
            if ("singleton".equals(definition.getScope())) {
                getXmlBean(beanId); // 触发单例 Bean 的创建
            }
        }
    }

    public Object getXmlBean(String beanId) throws Exception {
        BeanDefinition definition = beanDefinitions.get(beanId);
        Class<?> clazz = Class.forName(definition.getClassName());

        // 1. 处理构造函数注入
        Object[] args = new Object[definition.getConstructorArgs().size()];
        for (ConstructorArg arg : definition.getConstructorArgs()) {
            if (arg.getValue() != null) {
                args[arg.getIndex()] = arg.getValue();
            } else if (arg.getRef() != null) {
                args[arg.getIndex()] = getXmlBean(arg.getRef());
            }
        }
        Object instance = clazz.getConstructor(getArgTypes(args)).newInstance(args);

        // 2. 处理属性注入
        for (Map.Entry<String, Object> entry : definition.getProperties().entrySet()) {
            String fieldName = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof BeanReference) {
                value = getXmlBean(((BeanReference) value).getBeanId());
            }
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, value);
        }

        return instance;
    }

    // 辅助方法：获取参数类型数组
    private Class<?>[] getArgTypes(Object[] args) {
        return Arrays.stream(args)
                .map(arg -> arg != null ? arg.getClass() : Object.class)
                .toArray(Class<?>[]::new);
    }

    private void scanPackage(String[] scanPackages) throws Exception {
       for(String basePackage: scanPackages){
           // 将包路径转化为文件路径
           String path = basePackage.replace('.', '/');
           ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
           URL resource = classLoader.getResource(path);
           if (resource == null) {
               throw new RuntimeException("Package not found " + basePackage);
           }

           // 解码 URL 路径
           String filePath = URLDecoder.decode(resource.getFile(), StandardCharsets.UTF_8);
           File directory = new File(filePath);

           // 检查 directory 是否是有效的目录
           if (!directory.isDirectory()) {
               throw new RuntimeException("Path is not a directory: " + filePath);
           }

           // 遍历目录中的文件
           File[] files = directory.listFiles();
           if (files == null) {
               throw new RuntimeException("Unable to list files in directory: " + filePath);
           }

           for (File file : files) {
               if (file.getName().endsWith(".class")) {
                   String className = basePackage + "." + file.getName().replace(".class", "");
                   Class<?> clazz = Class.forName(className, true, classLoader);
                   startCreateBeans(clazz);
               }
           }
       }
    }

    // 获取所有带有指定注解的 Bean 实例
    public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType) {
        Map<String, Object> annotatedBeans = new HashMap<>();
        for (Map.Entry<String, Object> entry : singletonObjects.entrySet()) {
            Class<?> beanClass = entry.getValue().getClass();
            if (isAnnotationPresent(beanClass, annotationType, new HashSet<>())) {
                annotatedBeans.put(entry.getKey(), entry.getValue());
            }
        }
        return annotatedBeans;
    }

    // 递归检查类是否直接或间接带有指定的注解
    private boolean isAnnotationPresent(Class<?> clazz, Class<? extends Annotation> annotationType, Set<Class<?>> checkedAnnotations) {
        if (clazz.isAnnotationPresent(annotationType)) {
            return true;
        }
        for (Annotation annotation : clazz.getAnnotations()) {
            Class<? extends Annotation> annotationClass = annotation.annotationType();
            if (checkedAnnotations.contains(annotationClass)) {
                continue;
            }
            checkedAnnotations.add(annotationClass);
            if (isAnnotationPresent(annotationClass, annotationType, checkedAnnotations)) {
                return true;
            }
        }
        return false;
    }


    private void startCreateBeans(Class<?> clazz) throws Exception {
        // 检查是否有 @Component 注解
        if (isAnnotationPresent(clazz, Component.class, new HashSet<>())) {
            registerBean(clazz);
            // 检查条件是否满足
//            if (shouldRegisterBean(clazz)) {
//                // 注册 Bean
//                registerBean(clazz);
//            }
        }
    }

    // 条件检查核心逻辑
    private boolean shouldRegisterBean(Class<?> clazz) {
        // 简单实现：如果类名包含 "$$EnhancerByCGLIB$$"，则尝试获取父类
        if (clazz.getName().contains("$$EnhancerByCGLIB$$")) {
            clazz = clazz.getSuperclass();
        }
        // 获取所有条件注解
        List<Annotation> conditionalAnnotations = getConditionalAnnotations(clazz);

        // 如果没有条件注解则直接返回true
        if (conditionalAnnotations.isEmpty()) {
            return true;
        }

        // 创建上下文（需传递类加载器和目标类）
        SimpleConditionContext context = new SimpleConditionContext(
                clazz.getClassLoader(),
                clazz,
                this // 传递容器实例，用于检查 Bean 存在性
        );

        for (Annotation annotation : conditionalAnnotations) {
            // 获取注解上的 @Conditional 元注解
            Conditional conditionalMeta = annotation.annotationType().getAnnotation(Conditional.class);
            if (conditionalMeta == null) continue;

            // 检查所有关联的条件类
            for (Class<? extends Condition> conditionClass : conditionalMeta.value()) {
                try {
                    Condition condition = conditionClass.getDeclaredConstructor().newInstance();
                    if (!condition.matches(context)) {
                        return false; // 条件不满足
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to check condition", e);
                }
            }
        }
        return true;
    }

    private List<Annotation> getConditionalAnnotations(Class<?> clazz) {

        List<Annotation> conditionalAnnotations = new ArrayList<>();

        // 获取直接标记的 @Conditional 注解
        Conditional directConditional = clazz.getAnnotation(Conditional.class);
        if (directConditional != null) {
            conditionalAnnotations.add(directConditional);
        }

        // 获取所有其他注解，检查它们是否被 @Conditional 元注解标记
        for (Annotation annotation : clazz.getAnnotations()) {
            if (annotation.annotationType().isAnnotationPresent(Conditional.class)) {
                conditionalAnnotations.add(annotation);
            }
        }

        return conditionalAnnotations;
    }

    // 注册 Bean
    private void registerBean(Class<?> clazz) throws Exception {
        // 判断作用域
        if (isPrototype(clazz.getName())) {
            prototypeBeanClasses.put(clazz.getName(), clazz); // 存储原型 Bean 的 Class
        } else {
            // 单例 Bean 的处理逻辑
            beanScope(clazz);
        }
    }

    private void beanScope(Class<?> clazz) throws Exception {
        // 获取作用域（默认为单例）
        String scope = "singleton";
        if (clazz.isAnnotationPresent(Scope.class)) {
            scope = clazz.getAnnotation(Scope.class).value();
        }
        // 创建 Bean 实例
        Object instance = doCreateBean(clazz);
        // 如果是切面类，单独存储
        if (clazz.isAnnotationPresent(Aspect.class)) {
            Object aspect = createBeanInstance(clazz);
            aspects.add(aspect);
            // 收集所有通知方法
            for (Method method : clazz.getDeclaredMethods()) {
                collectAdviceMethods(method);
            }
        } else {
            // 普通 Bean 处理...
            // 单例 Bean 存入容器
            if ("singleton".equals(scope)) {
//                beans.put(clazz.getName(), instance);
                singletonObjects.put(clazz.getName(), instance);
            }
        }
    }
    //收集通知方法
    private void collectAdviceMethods(Method method){
        if (method.isAnnotationPresent(Around.class)) {
            Around around = method.getAnnotation(Around.class);
            aroundAdvices.put(method, parsePointcut(around.value()));
        }
        else if (method.isAnnotationPresent(Before.class)) {
            Before around = method.getAnnotation(Before.class);
            beforeAdvices.put(method, parsePointcut(around.value()));
        }
        else if (method.isAnnotationPresent(After.class)) {
            After around = method.getAnnotation(After.class);
            afterAdvices.put(method, parsePointcut(around.value()));
        }
        else if (method.isAnnotationPresent(AfterThrowing.class)) {
            AfterThrowing around = method.getAnnotation(AfterThrowing.class);
            afterThrowingAdvices.put(method, parsePointcut(around.value()));
        }

    }


    private PointcutInfo parsePointcut(String expr) {
        return pointcutCache.computeIfAbsent(expr, e -> {
            String pattern = expr.replace("execution(* ", "").replace("(..))", "").trim();
            String className = pattern.substring(0, pattern.lastIndexOf("."));
            String methodName = pattern.substring(pattern.lastIndexOf(".") + 1);
            return new PointcutInfo(className, methodName);
        });
    }

    private void applyAspects() {
        for (String beanName : new ArrayList<>(singletonObjects.keySet())) {
            Object bean = singletonObjects.get(beanName);
            Object proxy = createProxyIfNeeded(bean);
            if (proxy != null) {
//                beans.put(beanName, proxy); // 替换为代理对象
                singletonObjects.put(beanName,proxy); //暂且替换 TODO
            }
        }
    }

    private Object createProxyIfNeeded(Object target) {
        Class<?> targetClass = target.getClass();
        // 检查目标类是否匹配任何通知的切点
        boolean hasAdvice = checkIfAnyAdviceMatches(targetClass);
        if (!hasAdvice) return target;

        // 使用 CGLIB 创建代理对象
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(targetClass);
        enhancer.setCallback((MethodInterceptor) (obj, method, args, proxy) -> {
            // 1. 获取匹配的通知方法
            List<Method> beforeMethods = findMatchedAdviceMethods(method, beforeAdvices);
            List<Method> aroundMethods = findMatchedAdviceMethods(method, aroundAdvices);
            List<Method> afterMethods = findMatchedAdviceMethods(method, afterAdvices);
            List<Method> afterThrowingMethods = findMatchedAdviceMethods(method, afterThrowingAdvices);

            Object result = null;
            Throwable throwable = null;

            try {
                // 2. 执行 @Before 通知, 通过方法反射得到其所在的对象
                invokeAdviceMethods(beforeMethods, new ProceedingJoinPoint(target, method, args));

                // 3. 执行 @Around 通知（如果有）
                if (!aroundMethods.isEmpty()) {
                    ProceedingJoinPoint pjp = new ProceedingJoinPoint(target, method, args);
                    for (Method aroundMethod : aroundMethods) {
                        result = aroundMethod.invoke(getAspectInstance(aroundMethod), pjp);
                    }
                } else {
                    result = proxy.invokeSuper(obj, args); // 无 @Around 则直接执行原方法
                }

                // 4. 执行 @After 通知
                invokeAdviceMethods(afterMethods, new ProceedingJoinPoint(target, method, args));
            } catch (Throwable ex) {
                // 解包 InvocationTargetException 并递归提取最根本的异常原因
                Throwable cause = getRootCause(ex);
                throwable = cause; // 传递原始异常
                invokeAfterThrowingMethods(
                        afterThrowingMethods,
                        new ProceedingJoinPoint(target, method, args),
                        cause // 传递解包后的异常
                );
                throw cause; // 抛出原始异常
            }
            return result;
        });
        return enhancer.create();
    }

    // 递归方法，用于提取最根本的异常原因
    public static Throwable getRootCause(Throwable ex) {
        if (ex instanceof InvocationTargetException) {
            return getRootCause(ex.getCause());
        } else if (ex instanceof UndeclaredThrowableException) {
            return getRootCause(((UndeclaredThrowableException) ex).getUndeclaredThrowable());
        }
        return ex;
    }

    // 辅助方法：检查类是否匹配任何通知的切点
    private boolean checkIfAnyAdviceMatches(Class<?> targetClass) {
        for (Method method : targetClass.getDeclaredMethods()) {
            if (!findMatchedAdviceMethods(method, beforeAdvices).isEmpty() ||
                    !findMatchedAdviceMethods(method, aroundAdvices).isEmpty() ||
                    !findMatchedAdviceMethods(method, afterAdvices).isEmpty() ||
                    !findMatchedAdviceMethods(method, afterThrowingAdvices).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    // 辅助方法：查找匹配的通知方法
    private List<Method> findMatchedAdviceMethods(Method targetMethod, Map<Method, PointcutInfo> adviceMap) {
        List<Method> matchedMethods = new ArrayList<>();
        for (Map.Entry<Method, PointcutInfo> entry : adviceMap.entrySet()) {

            PointcutInfo pointcutInfo = entry.getValue();
            if (matches(pointcutInfo, targetMethod)) {
                matchedMethods.add(entry.getKey());
            }
        }
        return matchedMethods;
    }

    // 辅助方法：执行通知方法
    private void invokeAdviceMethods(List<Method> methods, ProceedingJoinPoint pjp) throws Exception {
        for (Method method : methods) {
            Object aspect = getAspectInstance(method);
            method.invoke(aspect, pjp);
        }
    }

    // 辅助方法：执行 @AfterThrowing 通知
    private void invokeAfterThrowingMethods(
            List<Method> methods,
            ProceedingJoinPoint pjp,
            Throwable ex // 此时 ex 是原始异常（如 RuntimeException）
    ) throws Exception {
        for (Method method : methods) {
            Object aspect = getAspectInstance(method);
            Parameter[] parameters = method.getParameters();
            Object[] args = new Object[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                Class<?> paramType = parameters[i].getType();
                if (Throwable.class.isAssignableFrom(paramType)) {
                    // 检查异常类型是否匹配（如 RuntimeException）
                    if (paramType.isAssignableFrom(ex.getClass())) {
                        args[i] = ex;
                    } else {
                        throw new IllegalArgumentException("Exception type mismatch");
                    }
                } else if (ProceedingJoinPoint.class.isAssignableFrom(paramType)) {
                    args[i] = pjp;
                }
            }
            method.invoke(aspect, args);
        }
    }

    // 辅助方法：获取切面实例
    private Object getAspectInstance(Method adviceMethod) {
        return aspects.stream()
                .filter(aspect -> aspect.getClass() == adviceMethod.getDeclaringClass())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Aspect instance not found"));
    }

    // 辅助方法：查找切面类中的通知方法
    private Method findMethod(Object aspect, String methodName) {
        try {
            return aspect.getClass().getMethod(methodName);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private boolean matches(PointcutInfo pointcut, Method method) {
        String declaringClass = method.getDeclaringClass().getName();
        boolean classMatch = declaringClass.equals(pointcut.classNamePattern);
        boolean methodMatch = pointcut.methodNamePattern.equals("*") ||
                method.getName().equals(pointcut.methodNamePattern);
        return classMatch && methodMatch;
    }


    // 查找带 @Autowired 注解的构造函数
    //如果类中有多个构造函数，可以优先选择参数最多的带 @Autowired 注解的构造函数
    private Constructor<?> findAutowiredConstructor(Class<?> clazz) {
        Constructor<?> targetConstructor = null;
        int maxParams = -1;

        for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
            if (constructor.isAnnotationPresent(Autowired.class)) {
                int paramCount = constructor.getParameterCount();
                if (paramCount > maxParams) {
                    targetConstructor = constructor;
                    maxParams = paramCount;
                }
            }
        }
        return targetConstructor;
    }

    private final SimpleParameterNameDiscoverer parameterNameDiscoverer = new SimpleParameterNameDiscoverer();

    private Object createBeanInstance(Class<?> clazz) throws Exception {
        // 查找带 @Autowired 注解的构造函数
        Constructor<?> constructor = findAutowiredConstructor(clazz);
        Object instance;

        if (constructor != null) {
            // 获取构造函数的参数类型
            Class<?>[] paramTypes = constructor.getParameterTypes();
            Object[] params = new Object[paramTypes.length];

            // 获取构造函数参数名
            String[] paramNames = parameterNameDiscoverer.getParameterNames(constructor);
            if (paramNames == null) {
                throw new RuntimeException("Parameter names not found for constructor: " + constructor);
            }

            // 实例化 Bean
            instance = constructor.newInstance(params);

            // 将 Bean 的早期引用放入缓存
            earlySingletonObjects.put(clazz.getName(), instance);

            // 填充构造函数参数
            for (int i = 0; i < paramTypes.length; i++) {
                params[i] = getBean(paramTypes[i]); // 通过 getBean 获取依赖
                if (params[i] == null) {
                    throw new RuntimeException("Dependency not found: " + paramTypes[i].getSimpleName());
                }
            }

            // 重新设置构造函数参数
            for (int i = 0; i < paramTypes.length; i++) {
                // 获取参数类型
                Class<?> paramType = paramTypes[i];

                // 遍历类的所有字段，找到类型匹配的字段
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.getType().equals(paramType)) {
                        field.setAccessible(true); // 取消访问检查
                        field.set(instance, params[i]); // 设置字段值
                        break;
                    }
                }
            }
        } else {

            // 默认创建（无参构造函数）
            instance = clazz.getDeclaredConstructor().newInstance();

            // 将 Bean 的早期引用放入缓存
            earlySingletonObjects.put(clazz.getName(), instance);
        }

        // 调用初始化方法
        invokePostConstruct(instance);

        return instance;
    }


    //容器中处理初始化方法
    private void invokePostConstruct(Object bean) throws Exception {
        for (Method method : bean.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                method.setAccessible(true);// 取消 Java 语言访问检查
                method.invoke(bean); // 调用初始化方法
            }
        }
    }

    public void close() {
        for (Object bean : singletonObjects.values()) {
            invokePreDestroy(bean);
        }
        singletonObjects.clear();
    }

    //销毁bean
    private void invokePreDestroy(Object bean) {
        for (Method method : bean.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(PreDestroy.class)) {
                try {
                    method.setAccessible(true);
                    method.invoke(bean); // 调用销毁方法
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void injectDependencies() throws Exception {
        for (Object bean : singletonObjects.values()) {
            for (Field field : bean.getClass().getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    // 设置字段可访问
                    field.setAccessible(true);
                    // 从容器中获取依赖对象并注入
                    Object dependency = singletonObjects.get(field.getType().getName());
                    if (dependency != null) {
                        field.set(bean, dependency);
                    } else {
                        // 从原型bean容器中查找依赖对象并注入
                        Class<?> prototypeBeanClass = prototypeBeanClasses.get(field.getType().getName());
                        if (prototypeBeanClass != null) {
                            // 创建原型Bean实例
                            dependency = doCreateBean(prototypeBeanClass);
                            field.set(bean, dependency);
                        } else {
                            throw new RuntimeException("Dependency not found: " + field.getType().getName());
                        }
                    }
                }
            }
        }
    }


    private Object doCreateBean( Class<?> clazz) {
        String beanName = clazz.getName();
        try {
            // 1. 实例化 Bean
            Object beanInstance = createBeanInstance(clazz);
            Object proxy = createProxyIfNeeded(beanInstance);
            // 2. 提前暴露工厂
            ObjectFactory<?> factory = () -> {
                return proxy != null ? proxy : beanInstance;
            };
            singletonFactories.put(beanName, factory);

            // 3. 填充属性(字段注入)
            populateBean(beanInstance);

            // 4. 初始化（调用 @PostConstruct 方法）
            initializeBean(beanInstance);

            // 5. 将完整 Bean 放入一级缓存
            singletonObjects.put(beanName, Objects.requireNonNullElse(proxy, beanInstance));

            // 清理缓存
            earlySingletonObjects.remove(beanName);
            singletonFactories.remove(beanName);
            singletonsCurrentlyInCreation.remove(beanName);

            return factory.getObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean: " + beanName, e);
        }
    }

    private void initializeBean(Object bean) throws Exception {
        // 调用 @PostConstruct 方法
        invokePostConstruct(bean);
    }

    private void populateBean(Object bean) throws Exception {
        for (Field field : bean.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(Autowired.class)) {
                field.setAccessible(true);
                Class<?> fieldType = field.getType();
                Object dependency = getBean(fieldType); // 通过 getBean 获取依赖
                field.set(bean, dependency);
            }
        }
    }

    public <T> T getInstance(Class<T> clazz) throws Exception{
        if(state == ContainerState.READY){
            if(!shouldRegisterBean(clazz)){
                throw new RuntimeException(clazz.getName() + " should not be created");
            }
        }
        return getBean(clazz);
    }

    public <T> T getBean(Class<T> clazz) throws Exception {

        if (!clazz.isAnnotationPresent(Component.class)){
            throw new RuntimeException(clazz.getName() + " is not annotated with @Component");
        }

        String beanName = clazz.getName();
        // 1. 检查是否是原型 Bean
        if (isPrototype(beanName)) {
            return clazz.isAnnotationPresent(Component.class)? createPrototypeBean(beanName, clazz):null;
        }

        // 2. 单例 Bean 的处理逻辑
        // 检查一级缓存
        Object bean = singletonObjects.get(beanName);
        if (bean != null) {
            return (T) bean;
        }

        // 检查二级缓存
        bean = earlySingletonObjects.get(beanName);
        if (bean != null) {
            return (T) bean;
        }

        // 检查三级缓存
        ObjectFactory<?> singletonFactory = singletonFactories.get(beanName);
        if (singletonFactory != null) {
            bean = singletonFactory.getObject();
            earlySingletonObjects.put(beanName, bean); // 升级到二级缓存
            singletonFactories.remove(beanName);
            return (T) bean;
        }

        // 3. 返回空
        return  clazz.isAnnotationPresent(Component.class)? (T)doCreateBean(clazz):null;
    }

    private boolean isPrototype(String beanName) {
        Class<?> clazz = getBeanClass(beanName);
        if (clazz != null && clazz.isAnnotationPresent(Scope.class)) {
            Scope scope = clazz.getAnnotation(Scope.class);
            return "prototype".equals(scope.value());
        }
        return false; // 默认单例
    }

    // 辅助方法：根据 Bean 名称获取 Class 对象
    private Class<?> getBeanClass(String beanName) {
        try {
            return Class.forName(beanName);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    private <T> T createPrototypeBean(String beanName, Class<T> clazz) {

        try {
            Object bean = earlySingletonObjects.get(beanName);
            if (bean != null) {
                return (T) bean;
            }

            // 1. 实例化 Bean
            Object beanInstance = doCreateBean(clazz);
            singletonObjects.remove(beanName); //不能将其(原型bean)放入单例bean集合
            // 2. 填充属性
            populateBean(beanInstance);

            // 3. 初始化（调用 @PostConstruct 方法）
            initializeBean(beanInstance);

            return (T) beanInstance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create prototype bean: " + beanName, e);
        }
    }


    private void registerListeners() {
        for (Object bean : singletonObjects.values()) {
            // 注册接口实现的监听器
            if (bean instanceof ApplicationListener) {
                addApplicationListener((ApplicationListener<?>) bean);
            }
            // 注册注解标记的监听器
            for (Method method : bean.getClass().getMethods()) {
                if (method.isAnnotationPresent(EventListener.class)) {
                    Class<? extends ApplicationEvent> eventType = method.getAnnotation(EventListener.class).value();
                    listeners.computeIfAbsent(eventType, k -> new ArrayList<>())
                            .add(event -> {
                                try {
                                    method.invoke(bean, event);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            });
                }
            }
        }
    }

    // 注册监听器
    public void addApplicationListener(ApplicationListener<?> listener) {
        // 解析监听器的泛型事件类型
        Class<?> eventType = resolveEventType(listener.getClass());
        if (eventType != null) {
            listeners.computeIfAbsent((Class<? extends ApplicationEvent>) eventType, k -> new ArrayList<>())
                    .add(listener);
        }
    }

    // 发布事件
    public void publishEvent(ApplicationEvent event) {
        Class<? extends ApplicationEvent> eventType = event.getClass();
        List<ApplicationListener<?>> matchedListeners = listeners.get(eventType);
        if (matchedListeners != null) {
            for (ApplicationListener<?> listener : matchedListeners) {
                invokeListener(listener, event);
            }
        }
    }

    // 反射调用监听器方法
    @SuppressWarnings("unchecked")
    private void invokeListener(ApplicationListener<?> listener, ApplicationEvent event) {
        try {
            Method method = listener.getClass().getMethod("onApplicationEvent", ApplicationEvent.class);
            method.invoke(listener, event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke listener", e);
        }
    }

    // 解析监听器的泛型事件类型
    private Class<?> resolveEventType(Class<?> listenerClass) {
        Type[] interfaces = listenerClass.getGenericInterfaces();
        for (Type type : interfaces) {
            if (type instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) type;
                if (pType.getRawType() == ApplicationListener.class) {
                    return (Class<?>) pType.getActualTypeArguments()[0];
                }
            }
        }
        return null;
    }

}
