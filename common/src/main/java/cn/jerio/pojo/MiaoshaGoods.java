package cn.jerio.pojo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class MiaoshaGoods implements Serializable {
    private Long id;
    private Long goodsId;
    private Integer stockCount;
    private Date startDate;
    private Date endDate;
}
