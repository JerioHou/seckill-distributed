package cn.jerio.vo;


import cn.jerio.pojo.Goods;
import lombok.Data;

import java.util.Date;

/**
 * Created by Jerio on 2018/3/20
 */
@Data
public class GoodsVo extends Goods {
    private Double miaoshaPrice;
    private Integer stockCount;
    private Date startDate;
    private Date endDate;
}
