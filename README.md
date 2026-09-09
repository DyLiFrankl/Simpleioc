# Simpleioc — 手写 IoC 容器框架

> ⚠️ **本项目为学习练习用途，请勿用于生产环境。**

## 项目简介

Simpleioc 是一个从零手写的轻量级 **IoC（控制反转）容器框架**，仿照 Spring 核心机制实现，目的是通过"造轮子"的方式深入理解 Spring 的底层原理：

- Bean 是如何被扫描、实例化、注入的？
- AOP 动态代理是如何织入的？
- 条件装配、生命周期回调、事件发布这些机制背后做了什么？
- Spring MVC 的请求是如何从 Servlet 分发到 Controller 方法的？

带着这些问题写代码，比只读源码和文档理解得更透彻。

## 已实现的功能模块

### 🧩 IoC 容器核心
- 包扫描 + 注解驱动：`@Component`、`@Autowired`、`@Scope`
- 依赖注入：按类型 / 按名称注入、构造器注入、XML 声明 Bean 的引用注入
- 容器状态管理（`ContainerState`）

### 🎭 AOP 面向切面编程
- 注解式切面：`@Aspect` + `@Before` / `@After` / `@Around` / `@AfterThrowing`
- `ProceedingJoinPoint` 环绕通知
- 基于 **CGLib** 的动态代理实现
- 示例切面：日志切面（`LogAspect`）、声明式事务切面（`TransactionAspect`）

### 🫘 Bean 作用域与生命周期
- `@SingletonBean` / `@PrototypeBean` 单例与原型作用域
- `@PostConstruct` / `@PreDestroy` 初始化与销毁回调

### ✅ 条件装配
- `@Conditional` + `Condition` 接口
- 内置条件：`@ConditionalOnClass`（类存在才装配）、`@ConditionalOnMissingBean`（容器中无此 Bean 才装配）

### 📢 事件机制
- `ApplicationEvent` + `ApplicationListener` 观察者模式
- `@EventListener` 注解式监听
- 示例：用户创建事件（`UserCreatedEvent` / `UserCreatedListener`）

### 🌐 仿 Spring MVC
- `DispatcherServlet` 前端控制器 + `HandlerMapping` 处理器映射
- `@Controller` + `@RequestMapping` + `@ResponseBody`
- `@RequestParam` / `@RequestBody` 参数解析（含基础类型、集合、嵌套对象、默认值）
- 内置测试服务器（`src/test/java/WebServer.java`）验证请求分发

### 🗄️ 数据访问层（仿 JdbcTemplate）
- `JdbcTemplate` + `RowMapper` + `BeanPropertyRowMapper`（结果集自动映射到 Bean）
- `TypeHandler` 类型转换（含枚举处理）
- 简单连接池 `SimpleDataSource`、统一异常 `DataAccessException`

### 📄 XML 配置支持
- `XmlBeanDefinitionReader` 解析 `beans.xml`
- `BeanDefinition` / `ConstructorArg` / `BeanReference`

## 技术栈

- **Java** + **Maven**
- **CGLib** — 动态代理
- **javax.servlet** — Web 层
- **MySQL Connector** — 数据库连接

## 快速开始

```bash
# 运行主程序（IoC + AOP + 事件 + 作用域综合演示）
mvn exec:java -Dexec.mainClass="com.t.e.Main"

# 运行内置 Web 服务器（MVC 请求分发演示）
# 见 src/test/java/WebServer.java
```

## 项目结构

```
src/main/java/com/t/e/
├── simpleioc/            # IoC 容器核心（SimpleIoC、容器状态、注解）
├── aop/                  # AOP 注解与切面模型
├── bean/                 # Bean 作用域注解
├── condition/            # 条件装配注解与实现
├── init/                 # 生命周期回调注解
├── listener/             # 事件模型（事件、监听器）
├── web/                  # 仿 MVC（DispatcherServlet、参数解析）
├── data/                 # 仿 JdbcTemplate 数据访问
├── xml/                  # XML 配置解析
├── util/                 # 工具类（断言、转换、属性、序列化、参数名发现）
└── test/                 # 演示示例（切面、事件、Web 测试用例）
```

## 学习路径建议

1. **先看 IoC**：从 `SimpleIoC` 的包扫描和依赖注入入手，理解反射与容器启动流程
2. **再看 AOP**：对比 CGLib 代理与 JDK 动态代理的区别，理解切面织入时机
3. **然后看 MVC**：`DispatcherServlet` → `HandlerMapping` → 参数解析，对照 Spring MVC 的流程
4. **最后看扩展点**：条件装配、生命周期、事件——这些是 Spring Boot 自动配置的核心思想

## 与 Spring 的对照

| Simpleioc | Spring 对应 |
|-----------|------------|
| `@Component` / `@Autowired` | 同名注解 |
| `SimpleIoC` | `ApplicationContext` |
| `@Before` / `@Around` | Spring AOP / AspectJ |
| `@ConditionalOnClass` | Spring Boot 自动装配 |
| `ApplicationEvent` | Spring 事件机制 |
| `DispatcherServlet` | Spring MVC 前端控制器 |
| `JdbcTemplate` | Spring JDBC |

## 说明

本项目仅作为个人学习 Spring 原理的练习作品，实现以"跑通核心机制"为目标，未考虑生产环境所需的安全性、性能与兼容性。
