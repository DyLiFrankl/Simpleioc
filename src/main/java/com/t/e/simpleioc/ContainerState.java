package com.t.e.simpleioc;

public enum ContainerState {


    UNINITIALIZED("未初始化"),    // 未初始化
    XML_LOADED("XML 配置已加载"),       // XML 配置已加载
    SCANNED("包扫描完成"),          // 包扫描完成
    ASPECT_APPLIED("切面已应用"),   // 切面已应用
    LISTENERS_READY("监听器已注册"),  // 监听器已注册
    DEPENDENCIES_INJECTED("依赖注入完成"), // 依赖注入完成
    CONDITIONS_CHECKED("条件检查完成"),    // 条件检查完成
    READY("容器就绪");             // 容器就绪

    private  String state;

    ContainerState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}

