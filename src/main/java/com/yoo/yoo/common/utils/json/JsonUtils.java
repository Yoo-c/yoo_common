package com.yoo.yoo.common.utils.json;

import com.alibaba.fastjson.*;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.*;
import com.alibaba.fastjson.util.TypeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * @author yoo
 * @date 2020/4/8
 * @since 1.0
 */
public class JsonUtils {
    public static final String FORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private JsonUtils() {
    }

    private static final SerializeConfig SERIALIZE_CONFIG = new SerializeConfig();
    private static final ParserConfig JSON_TEXT_PARSER_CONFIG = new ParserConfig();
    private static final ParserConfig JSON_OBJECT_PARSER_CONFIG = new ParserConfig();

    static {
        System.setProperty(ParserConfig.AUTOTYPE_SUPPORT_PROPERTY, Boolean.TRUE.toString());
        SERIALIZE_CONFIG.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        JSON_TEXT_PARSER_CONFIG.propertyNamingStrategy = PropertyNamingStrategy.CamelCase;
        JSON_OBJECT_PARSER_CONFIG.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        SERIALIZE_CONFIG.put(Date.class, new SimpleDateFormatSerializer(FORMAT_PATTERN));
    }

    /**
     * 序列化
     *
     * @param object 序列化对象
     * @return
     */
    public static String toJsonStrSnakeCase(Object object) {
        return JSON.toJSONString(object, SERIALIZE_CONFIG);
    }

    /**
     * 序列化（驼峰式转下划线式）
     *
     * @param object 序列化对象
     * @return
     */
    public static JSONObject toJsonObjSnakeCase(Object object) {
        return (JSONObject) JsonUtils.toJSON(object, SERIALIZE_CONFIG);
    }

    /**
     * 反序列化 （下划线转驼峰式）
     *
     * @param json  json格式字符串
     * @param clazz 反序列化类型
     * @return
     */
    public static <T> T fromJsonCamelCase(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz, JSON_TEXT_PARSER_CONFIG);
    }

    /**
     * 将JSONObject兑换转换成Java POJO对象（下划线转驼峰式）
     *
     * @param json  JSONObject
     * @param clazz 转换类型
     * @param <T>
     * @return
     */
    public static <T> T fromJsonCamelCase(JSONObject json, Class<T> clazz) {
        return TypeUtils.cast(json, clazz, JSON_OBJECT_PARSER_CONFIG);
    }

    /**
     * JSON反序列化
     *
     * @param text  json字符串
     * @param clazz 反序列化对象类型
     * @return T
     */
    public static <T> T fromJson(String text, Class<T> clazz) {
        return JSON.parseObject(text, clazz);
    }

    /**
     * 将Java对象转换JSONObject对象, 并格式化时间输出
     *
     * @param javaObject
     * @param config
     * @return
     */
    @SuppressWarnings("unchecked")
    private static Object toJSON(Object javaObject, SerializeConfig config) {
        if (javaObject == null) {
            return null;
        }
        if (javaObject instanceof JSON) {
            return javaObject;
        }
        if (javaObject instanceof Map) {
            Map<Object, Object> map = (Map<Object, Object>) javaObject;
            JSONObject json = new JSONObject(map.size());
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                Object key = entry.getKey();
                String jsonKey = TypeUtils.castToString(key);
                Object jsonValue = toJSON(entry.getValue(), config);
                json.put(jsonKey, jsonValue);
            }
            return json;
        }
        if (javaObject instanceof Collection) {
            Collection<Object> collection = (Collection<Object>) javaObject;
            JSONArray array = new JSONArray(collection.size());
            for (Object item : collection) {
                Object jsonValue = toJSON(item, config);
                array.add(jsonValue);
            }
            return array;
        }
        Class<?> clazz = javaObject.getClass();
        if (clazz.isEnum()) {
            return ((Enum<?>) javaObject).name();
        }
        if (clazz.isArray()) {
            int len = Array.getLength(javaObject);
            JSONArray array = new JSONArray(len);
            for (int i = 0; i < len; ++i) {
                Object item = Array.get(javaObject, i);
                Object jsonValue = toJSON(item, config);
                array.add(jsonValue);
            }
            return array;
        }
        if (isPrimitive(clazz)) {
            return javaObject;
        }
        ObjectSerializer serializer = config.getObjectWriter(clazz);
        if (serializer instanceof JavaBeanSerializer) {
            JavaBeanSerializer javaBeanSerializer = (JavaBeanSerializer) serializer;
            JSONObject json = new JSONObject();
            try {
                Map<String, Object> values = javaBeanSerializer.getFieldValuesMap(javaObject);
                for (Map.Entry<String, Object> entry : values.entrySet()) {
                    if (entry.getValue() instanceof Date) {
                        FieldSerializer fieldSerializer = ((JavaBeanSerializer) serializer).getFieldSerializer(entry.getKey());
                        String format = StringUtils.isEmpty(fieldSerializer.fieldInfo.format) ? FORMAT_PATTERN : fieldSerializer.fieldInfo.format;
                        json.put(entry.getKey(), DateFormatUtils.format((Date) entry.getValue(), format));
                    } else {
                        if (entry.getValue() != null) {
                            json.put(entry.getKey(), toJSON(entry.getValue(), config));
                        }
                    }
                }
            } catch (Exception e) {
                throw new JSONException("toJSON error", e);
            }
            return json;
        }

        String text = JSON.toJSONString(javaObject);
        return JSON.parse(text);
    }

    /**
     * 判断是否基本类型
     *
     * @param clazz
     * @return
     */
    private static boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == Boolean.class
                || clazz == Character.class
                || clazz == Byte.class
                || clazz == Short.class
                || clazz == Integer.class
                || clazz == Long.class
                || clazz == Float.class
                || clazz == Double.class
                || clazz == BigInteger.class
                || clazz == BigDecimal.class
                || clazz == String.class
                || clazz == java.util.Date.class
                || clazz == java.sql.Date.class
                || clazz == java.sql.Time.class
                || clazz == java.sql.Timestamp.class
                || clazz.isEnum()
                ;
    }
}
