package com.yoo.yoo.common.plugin.apollo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Objects;

/**
 * @author yoo
 * @date 2020/4/7
 * @since 1.0
 */
@Configurable
@Order(1)
@Slf4j
class ApolloConfigRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        AnnotationAttributes annotationAttributes = AnnotationAttributes.fromMap(annotationMetadata.getAnnotationAttributes(EnableApolloConfig.class.getName()));
        Class<?> configClass = Objects.requireNonNull(annotationAttributes).getClass("configClass");
        try {
            ApolloConfigInitializer initializer = new ApolloConfigInitializer(configClass);
            initializer.initConfig();
            log.info("配置中心初始化完成");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
