package com.t.e.condition;

// 条件类实现
public class OnClassCondition implements Condition {
    @Override
    public boolean matches(SimpleConditionContext context) {
        ConditionalOnClass annotation = context.getAnnotation(ConditionalOnClass.class);
        if (annotation == null) {
            throw new IllegalStateException("@ConditionalOnClass annotation not found");
        }
        for (String className : annotation.value()) {
            if (!context.isClassPresent(className)) {
                return false;
            }
        }
        return true;
    }
}
