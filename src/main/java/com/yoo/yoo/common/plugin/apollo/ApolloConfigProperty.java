package com.yoo.yoo.common.plugin.apollo;

import java.lang.annotation.*;

/**
 * 描述：
 *
 * @author yoo
 * @date 2020/4/7
 * @since 1.0
 */
@Target({ElementType.TYPE, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApolloConfigProperty {
    /**
     * 配置Key
     */
    String configKey();

    /**
     * 配置默认值
     */
    String defaultValue() default "";

    /**
     * 如果使用虚拟应用，需要配置指定的命名空间
     */
    String namespace() default "application";
}
