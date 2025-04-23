package com.t.e.test;

import com.t.e.condition.ConditionalOnClass;
import com.t.e.condition.ConditionalOnMissingBean;
import com.t.e.simpleioc.annotations.Component;
import com.t.e.util.AssertUtils;

// 示例1：当类路径存在时注册
@Component
//@ConditionalOnClass("com.t.e.aop.After")
@ConditionalOnMissingBean(AssertUtils.class)
public class DatabaseService {
    // ...
}
