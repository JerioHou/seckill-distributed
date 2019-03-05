package cn.jerio.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by Jerio on 2019/03/04
 */
@Data
public class MiaoshaMessage implements Serializable{
    private MiaoshaUser user;
    private long goodsId;
}
