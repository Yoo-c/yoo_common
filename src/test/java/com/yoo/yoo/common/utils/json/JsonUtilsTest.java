package com.yoo.yoo.common.utils.json;


import com.alibaba.fastjson.JSONObject;
import com.yoo.yoo.common.Pc;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 描述：
 *
 * @author flandreli
 * @date 2020/4/8
 * @since 1.0
 */
class JsonUtilsTest {

    private Pc pc = new Pc().setCpu("I7 9400F").setMemory("16GB").setGraphicsCard("2080");

    @Test
    void toJsonStrSnakeCase() {
        String jsonStrSnakeCase = JsonUtils.toJsonStrSnakeCase(pc);
        System.out.println(jsonStrSnakeCase);
    }

    @Test
    void toJsonObjSnakeCase() {
        JSONObject jsonObject = JsonUtils.toJsonObjSnakeCase(pc);
        System.out.println(jsonObject);
    }

    @Test
    void fromJsonCamelCase() {
        Pc pc = JsonUtils.fromJsonCamelCase("{\"graphics_card\":\"2080\",\"memory\":\"16GB\",\"cpu\":\"I7 9400F\"}", Pc.class);
        System.out.println(pc);
    }

    @Test
    void fromJsonCamelCase1() {
        JSONObject jsonObject = JsonUtils.toJsonObjSnakeCase(pc);
        Pc pc = JsonUtils.fromJsonCamelCase(jsonObject, Pc.class);
        System.out.println(pc);
    }

    @Test
    void fromJson() {
        Pc pc = JsonUtils.fromJson("{}", Pc.class);
        System.out.println(pc);
    }

    @Test
    void listJson() {
        List<Pc> list = new ArrayList<>();
        list.add(pc);
        System.out.println(list);
    }

    @Test
    void mapJson() {
        Map<String, Pc> map = new HashMap<>();
        map.put("pc", pc);
        System.out.println(JsonUtils.toJsonStrSnakeCase(map));
    }
}