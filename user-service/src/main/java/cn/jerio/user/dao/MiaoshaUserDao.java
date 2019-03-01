package cn.jerio.user.dao;

import cn.jerio.pojo.MiaoshaUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;


@Mapper
public interface MiaoshaUserDao {

    @Select("select * from miaosha_user where id = #{id}")
    MiaoshaUser getById(long id);

    @Update("update miaosha_user set password = #{password} where id = #{id}")
    int updatePass(MiaoshaUser toBeUpdateUser);
}
