package cn.jerio.pojo;


import lombok.Data;

import java.io.Serializable;

@Data
public class Goods implements Serializable{
    private Long id;
    private String goodsName;
    private String goodsTitle;
    private String goodsImg;
    private String goodsDetail;
    private Double goodsPrice;
    private Integer goodsStock;
}
