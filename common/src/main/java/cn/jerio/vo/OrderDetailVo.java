package cn.jerio.vo;


import cn.jerio.pojo.OrderInfo;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by Jerio on 2018/9/2
 */
@Data
public class OrderDetailVo implements Serializable {
    private GoodsVo goods;
    private OrderInfo order;
}
