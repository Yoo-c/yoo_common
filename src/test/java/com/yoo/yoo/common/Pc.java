package com.yoo.yoo.common;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 描述：
 *
 * @author yoo
 * @date 2020/4/8
 * @since 1.0
 */
@Data
@Accessors(chain = true)
public class Pc {
    private String cpu;
    private String memory;
    private String graphicsCard;
}
