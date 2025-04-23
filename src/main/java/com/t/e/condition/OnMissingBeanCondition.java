package com.t.e.condition;

public class OnMissingBeanCondition implements Condition {
    @Override
    public boolean matches(SimpleConditionContext context) {
        ConditionalOnMissingBean annotation = context.getAnnotation(ConditionalOnMissingBean.class);
        for (Class<?> clazz : annotation.value()) {
            if (context.containsBean(clazz)) {
                return false;
            }
        }
        return true;
    }
}
