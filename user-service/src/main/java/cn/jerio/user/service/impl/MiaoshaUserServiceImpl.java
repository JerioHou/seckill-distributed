package cn.jerio.user.service.impl;


import cn.jerio.constant.RedisKey;
import cn.jerio.exception.GlobalException;
import cn.jerio.pojo.MiaoshaUser;
import cn.jerio.result.CodeMsg;
import cn.jerio.user.dao.MiaoshaUserDao;
import cn.jerio.user.service.MiaoShaUserService;
import cn.jerio.util.MD5Util;
import cn.jerio.util.UUIDUtil;
import cn.jerio.vo.LoginVo;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jerio
 */
@Service
public class MiaoshaUserServiceImpl implements MiaoShaUserService {

    @Autowired
    private MiaoshaUserDao miaoshaUserDao;
    @Resource
    private RedisTemplate<String,MiaoshaUser> redisTemplate;

    @Value("${redis.tokenExpire}")
    int tokenExpire;

    @Override
    public MiaoshaUser getById(long id) {
        MiaoshaUser user = redisTemplate.boundValueOps(RedisKey.USER_KEY_PRFIX+id).get();
        if (user == null){
            user = miaoshaUserDao.getById(id);
            redisTemplate.opsForValue().set(RedisKey.USER_KEY_PRFIX+id,user);
        }
        return user;
    }

    @Override
    public boolean updatePassword( long id, String formPass) {
        MiaoshaUser user = getById(id);
        if (user == null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        MiaoshaUser toBeUpdateUser = new MiaoshaUser();
        toBeUpdateUser.setId(id);
        toBeUpdateUser.setPassword(MD5Util.formPassToDBPass(formPass,user.getSalt()));
        miaoshaUserDao.updatePass(toBeUpdateUser);
        // 删除缓存数据，重新登录
        redisTemplate.delete(RedisKey.USER_KEY_PRFIX+id);
        return true;
    }

    @Override
    public String login(LoginVo loginVo) {
        if(loginVo == null) {
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();
        //判断手机号是否存在
        MiaoshaUser user = getById(Long.parseLong(mobile));
        if(user == null) {
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }
        //验证密码
        String dbPass = user.getPassword();
        String saltDB = user.getSalt();
        String calcPass = MD5Util.formPassToDBPass(formPass, saltDB);
        if(!calcPass.equals(dbPass)) {
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }
        //生成token
        String token = UUIDUtil.uuid();
        redisTemplate.opsForValue().set(RedisKey.USER_TOKEN+token,user,tokenExpire,TimeUnit.SECONDS);
        return token;
    }

    @Override
    public MiaoshaUser getByToken(String token) {
        return redisTemplate.opsForValue().get(RedisKey.USER_TOKEN + token);
    }
}
