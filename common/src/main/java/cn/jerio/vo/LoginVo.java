package cn.jerio.vo;


import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.io.Serializable;


/**
 * Created by Jerio on 2018/3/14
 */
@Data
public class LoginVo implements Serializable{

    @NotNull
    private String mobile;

    @NotNull
    @Length(min=32)
    private String password;

    @Override
    public String toString() {
        return "LoginVo [mobile=" + mobile + ", password=" + password + "]";
    }
}
