package com.yoo.yoo.common.plugin.apollo;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 *
 * @author yoo
 * @date 2020/4/7
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ApolloConfigRegistrar.class)
public @interface EnableApolloConfig {
    Class<?> configClass();
}

