package cn.jerio.vo;


import cn.jerio.pojo.OrderInfo;
import lombok.Data;

/**
 * Created by Jerio on 2018/9/2
 */
@Data
public class OrderDetailVo {
    private GoodsVo goods;
    private OrderInfo order;
}
