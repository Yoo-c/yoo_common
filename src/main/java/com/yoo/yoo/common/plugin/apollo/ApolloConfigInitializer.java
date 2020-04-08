package com.yoo.yoo.common.plugin.apollo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.ParserConfig;
import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.*;

/**
 * 描述：
 *
 * @author yoo
 * @date 2020/4/7
 * @since 1.0
 */
@Slf4j
class ApolloConfigInitializer {

    private Class<?> configClass;
    private static Map<String, Field> configMap = new HashMap<>(10);

    ApolloConfigInitializer(Class<?> configClass) {
        this.configClass = configClass;
    }

    /**
     * 初始化
     */
    void initConfig() {
        System.setProperty(ParserConfig.AUTOTYPE_SUPPORT_PROPERTY, Boolean.TRUE.toString());
        Set<String> namespaceSet = new HashSet<>();
        Field[] fields = configClass.getFields();
        for (Field field : fields) {
            ApolloConfigProperty annotation = field.getAnnotation(ApolloConfigProperty.class);
            if (annotation == null) {
                continue;
            }
            try {
                if (!Modifier.isStatic(field.getModifiers())) {
                    throw new UnsupportedOperationException("配置属性[" + configClass.getName() + "#" + field.getName() + "]必须静态变量");
                }
                Config config = ConfigService.getAppConfig();
                String originValue = config.getProperty(annotation.configKey(), annotation.defaultValue());
                Object configValue = transferValue(field, originValue);
                field.setAccessible(true);
                field.set(null, configValue);
                log.info("Hippo配置变量[{}]初始化 {}", annotation.configKey(), originValue);
                configMap.put(annotation.configKey(), field);
                namespaceSet.add(annotation.namespace());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            //监听变化
            for (String namespace : namespaceSet) {
                ConfigService.getConfig(namespace).addChangeListener((ConfigChangeEvent changeEvent) -> {
                    for (String changedKey : changeEvent.changedKeys()) {
                        Field f = configMap.get(changedKey);
                        if (f == null) {
                            continue;
                        }
                        try {
                            f.setAccessible(true);
                            ConfigChange change = changeEvent.getChange(changedKey);
                            field.set(null, transferValue(f, change.getNewValue()));
                            log.info("Hippo配置变量[{}]值改变 {} -> {}", changedKey, change.getOldValue(), change.getNewValue());
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                });
            }
        }
    }

    Object transferValue(Field configField, String originValue) {
        Class<?> type = configField.getType();
        if (type == Boolean.class || type == boolean.class) {
            return Boolean.valueOf(originValue);
        }
        if (type == Integer.class || type == int.class) {
            return Integer.valueOf(originValue);
        }
        if (type == Short.class || type == short.class) {
            return Short.valueOf(originValue);
        }
        if (type == Byte.class || type == byte.class) {
            return Byte.valueOf(originValue);
        }
        if (type == Float.class || type == float.class) {
            return Float.valueOf(originValue);
        }
        if (type == Long.class || type == long.class) {
            return Long.valueOf(originValue);
        }
        if (type == Double.class || type == double.class) {
            return Double.valueOf(originValue);
        }
        if (type == String.class) {
            return originValue;
        }
        if (type == BigDecimal.class) {
            return new BigDecimal(originValue);
        }
        if (type == JSONObject.class) {
            return JSONObject.parseObject(originValue);
        }
        if (type == JSONArray.class) {
            return JSONArray.parseArray(originValue);
        }
        if (Collections.class.isAssignableFrom(type)) {
            ParameterizedType listGenericType = (ParameterizedType) configField.getGenericType();
            Type[] actualTypeArguments = listGenericType.getActualTypeArguments();
            if (actualTypeArguments != null && actualTypeArguments.length > 0) {
                Class<?> genericType = (Class<?>) actualTypeArguments[0];
                return JSONArray.parseArray(originValue, genericType);
            }
        }
        if (Map.class.isAssignableFrom(type)) {
            ParameterizedType listGenericType = (ParameterizedType) configField.getGenericType();
            Type[] actualTypeArguments = listGenericType.getActualTypeArguments();
            JSONObject configValue = JSONObject.parseObject(originValue);
            if (actualTypeArguments != null && actualTypeArguments.length > 0 && configValue.size() > 0) {
                Map<String, Object> configMapValue = new HashMap<>(configValue.size());
                for (Map.Entry<String, Object> entry : configValue.entrySet()) {
                    if (entry.getValue() instanceof JSONObject) {
                        configMapValue.put(entry.getKey(), ((JSONObject) entry.getValue()).toJavaObject(actualTypeArguments[1]));
                    } else {
                        configMapValue.put(entry.getKey(), entry.getValue());
                    }
                }
                return configMapValue;
            }
        }
        return JSON.parseObject(originValue, type);
    }
}
