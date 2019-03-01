package cn.jerio.pojo;


import lombok.Data;

import java.io.Serializable;

@Data
public class MiaoshaOrder implements Serializable {
    private Long id;
    private Long userId;
    private Long  orderId;
    private Long goodsId;
}
