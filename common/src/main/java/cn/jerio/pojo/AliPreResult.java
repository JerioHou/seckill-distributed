package cn.jerio.pojo;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by Jerio on 2019/03/06
 */
@Data
public class AliPreResult implements Serializable{

    private String code;
    private String msg;
    private String outTradeNo;
    private String qrCode;

}
