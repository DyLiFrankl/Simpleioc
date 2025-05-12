package com.t.e.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.t.e.simpleioc.SimpleIoC;
import com.t.e.util.SerializeUtils;
import jakarta.servlet.ServletException;

import javax.servlet.AsyncContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

@MultipartConfig
public class DispatcherServlet extends HttpServlet {
    private ExecutorService threadPool; // 线程池
    private static final // 初始化 ObjectMapper 时注册模块
    ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // 处理 LocalDateTime
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 禁用时间戳格式

    private SimpleIoC container;
    private Map<String, HandlerMapping> handlerMappings = new HashMap<>(); // URL → 处理方法映射
    private static final Logger logger = Logger.getLogger(DispatcherServlet.class.getName());
    @Override
    public void init() {
        // 初始化线程池（建议使用有界队列）
        threadPool = Executors.newFixedThreadPool(200); // 根据机器性能调整
        // 初始化 IoC 容器（假设配置文件已指定扫描包）
        try {
            String[] scanPackages = new String[]{"com.t.e"};
            String xmlPaths = "src/main/resources/META-INF/custom-beans.xml";
            //初始化IoC容器
            container = new SimpleIoC(scanPackages, xmlPaths);
        } catch (Exception e) {
            try {
                throw new ServletException("Failed to initialize IoC container", e);
            } catch (ServletException ex) {
                throw new RuntimeException(ex);
            }
        }

        // 扫描所有 Controller，注册请求映射
        registerHandlers();
    }

    private void registerHandlers() {

        for (Object bean : container.getBeansWithAnnotation(Controller.class).values()) {
            Class<?> clazz = bean.getClass();
            // 类级别的 @RequestMapping（定义父路径）
            RequestMapping classMapping = clazz.getAnnotation(RequestMapping.class);
            String basePath = classMapping != null ? classMapping.value() : "";

            for (Method method : clazz.getDeclaredMethods()) {
                RequestMapping methodMapping = method.getAnnotation(RequestMapping.class);
                if (methodMapping != null) {
                    String url = basePath + methodMapping.value();
                    handlerMappings.put(url, new HandlerMapping(bean, method));
                }
            }
        }
    }

    @Override
    protected void service(javax.servlet.http.HttpServletRequest req, javax.servlet.http.HttpServletResponse resp) throws javax.servlet.ServletException, IOException {

        String path = req.getRequestURI();
        String method = req.getMethod();
        HandlerMapping handler = handlerMappings.get(path);
        if (handler == null || !handler.supportsMethod(method)) {
            resp.sendError(404, "Not Found");
            return;
        }
        // 在主线程提取所有必要数据
        ParameterSource parameterSource = new AsyncRequestSource(req);

        // 同步阻塞处理业务逻辑（保持主线程存活）
//        try {
//            Object result = threadPool.submit(() -> handler.invoke(parameterSource))
//                    .get(5, TimeUnit.SECONDS); // 设置超时
//
//            // 主线程直接写响应
//            writeResponse(resp, result, handler.getMethod());
//        } catch (TimeoutException e) {
//            resp.sendError(503, "Timeout");
//        } catch (Exception e) {
//            e.printStackTrace();
//            resp.sendError(500, e.getMessage());
//        }

        // 使用 HttpServletRequest 和 HttpServletResponse 明确类型
        AsyncContext asyncContext = req.startAsync();
        threadPool.submit(() -> {
            try {
                Object result = handler.invoke(parameterSource);
                HttpServletResponse asyncResp = (HttpServletResponse) asyncContext.getResponse();
                writeResponse(asyncResp, result, handler.getMethod());
            } catch (Exception e) {
                handleAsyncError(asyncContext, e);
            } finally {
                asyncContext.complete();
            }
        });
    }

    @Override
    public void destroy() {
        threadPool.shutdown(); // 平滑关闭（等待执行中的任务完成）
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow(); // 强制终止剩余任务
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // 将错误处理提取到单独方法
    private void handleAsyncError(AsyncContext asyncContext, Exception e) {
        try {
            HttpServletResponse asyncResp = (HttpServletResponse) asyncContext.getResponse();
            if (e instanceof TimeoutException) {
                asyncResp.sendError(503, "Timeout");
            } else {
                asyncResp.sendError(500, "Internal Error: "+e.getMessage()); // 生产环境建议不暴露e.getMessage()
            }
        } catch (IOException ex) {
            logger.severe("Failed to send error response: "+ ex.getMessage());
        }
        logger.severe("Handler invocation failed: "+ e.getMessage()); // 使用日志替代printStackTrace
    }


    private synchronized void writeResponse(HttpServletResponse resp, Object result, Method method) throws IOException {
        if (method.isAnnotationPresent(ResponseBody.class)) {
            // 返回 JSON
//            resp.setContentType("application/json");
//            resp.getWriter().write(convertToJson(result));
            resp.setContentType("application/json");
            objectMapper.writeValue(resp.getWriter(), result);
        } else {
            // 返回 HTML 视图（需实现视图解析）
            resp.setContentType("text/html");
            resp.getWriter().write(renderView(result));
        }
    }

    // 简单视图渲染（可替换为模板引擎）
    private String renderView(Object viewName) {
        // 假设 viewName 是视图文件路径（如 "success" 对应 "/WEB-INF/views/success.html"）
        String templatePath = "WEB-INF/views/" + viewName + ".html";
        try {
            // 从类路径或文件系统读取模板
            String absolutePath = getClass().getResource("/WEB-INF/views/success.html").getPath();
            InputStream is = getClass().getClassLoader().getResourceAsStream(templatePath);
            if (is == null) {
                return "<h1>Error: View template not found</h1>";
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "<h1>Error: Failed to render view</h1>";
        }
    }

    // 示例：简单 JSON 转换
    private String convertToJson(Object obj) {
        StringBuilder sb = new StringBuilder();
        SerializeUtils.serializeValue(obj, sb);
        return sb.toString();
    }
}
