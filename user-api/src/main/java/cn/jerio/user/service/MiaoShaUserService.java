package cn.jerio.user.service;

import cn.jerio.pojo.MiaoshaUser;
import cn.jerio.vo.LoginVo;

/**
 * Created by Jerio on 2019/03/01
 */
public interface MiaoShaUserService {

    MiaoshaUser getById(long id);

    boolean updatePassword(long id,String formPass);

    String login(LoginVo loginVo) ;

    MiaoshaUser getByToken(String token);


    }
