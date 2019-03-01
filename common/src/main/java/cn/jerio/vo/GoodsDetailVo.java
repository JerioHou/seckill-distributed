package cn.jerio.vo;


import cn.jerio.pojo.MiaoshaUser;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by Jerio on 2018/9/2
 */
@Data
public class GoodsDetailVo implements Serializable {
    private int miaoshaStatus = 0;
    private int remainSeconds = 0;
    private GoodsVo goods ;
    private MiaoshaUser user;
}
