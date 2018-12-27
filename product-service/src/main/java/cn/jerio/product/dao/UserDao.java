package cn.jerio.product.dao;


import cn.jerio.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface UserDao {
    @Select("select * from user where id = #{id}")
    User getUserById(int id);
}
