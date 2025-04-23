package com.t.e.web;

import com.t.e.simpleioc.SimpleIoC;
import com.t.e.util.SerializeUtils;
import jakarta.servlet.ServletException;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {
    private SimpleIoC container;
    private Map<String, HandlerMapping> handlerMappings = new HashMap<>(); // URL → 处理方法映射

    @Override
    public void init() {
        // 初始化 IoC 容器（假设配置文件已指定扫描包）
        try {
            String[] scanPackages = new String[]{"com.t.e.controller","com.t.e.service","com.t.e.bean","com.t.e.test", "com.t.e.test.listener"};
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
        Object o =  container.getBeansWithAnnotation(Controller.class).values();
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

        try {
            // 调用控制器方法
            Object result = handler.invoke(req);
            // 处理响应
            Method m = handler.getMethod();
            writeResponse(resp, result, handler.getMethod());
        } catch (Exception e) {
            resp.sendError(500, "Internal Error: " + e.getMessage());
        }
    }

    private void writeResponse(HttpServletResponse resp, Object result, Method method) throws IOException {
        if (method.isAnnotationPresent(ResponseBody.class)) {
            // 返回 JSON
            resp.setContentType("application/json");
            resp.getWriter().write(convertToJson(result));
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

    // 示例：简单 JSON 转换（可替换为 Jackson/Gson）
    private String convertToJson(Object obj) {
        StringBuilder sb = new StringBuilder();
        SerializeUtils.serializeValue(obj, sb);
        return "{\"data\":\"" + sb + "\"}";
    }
}
