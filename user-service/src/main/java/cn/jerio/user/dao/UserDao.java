package cn.jerio.user.dao;

import cn.jerio.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

/**
 * Created by Jerio on 2019/03/01
 */
@Mapper
public interface UserDao {
    @Select("select * from user where id = #{id}")
    User getUserById(int id);
}
