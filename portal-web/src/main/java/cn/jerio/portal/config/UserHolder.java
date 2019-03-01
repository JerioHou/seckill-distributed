package cn.jerio.portal.config;

import cn.jerio.pojo.MiaoshaUser;

/**
 * Created by Jerio on 2019/03/01
 */
public class UserHolder {

    public static ThreadLocal<MiaoshaUser> holder = new ThreadLocal<MiaoshaUser>();

    public static void setUser(MiaoshaUser miaoshaUser){
        holder.set(miaoshaUser);
    }

    public static MiaoshaUser getUser(){
        return holder.get();
    }
}
