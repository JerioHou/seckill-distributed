package cn.jerio.product.service.impl;

import cn.jerio.constant.RedisKey;
import cn.jerio.pojo.MiaoshaMessage;
import cn.jerio.pojo.MiaoshaUser;
import cn.jerio.product.mq.RabbitmqSender;
import cn.jerio.product.service.GoodsService;
import cn.jerio.product.service.MiaoshaService;
import cn.jerio.util.MD5Util;
import cn.jerio.util.UUIDUtil;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

/**
 * Created by Jerio on 2019/03/04
 */
@Service
public class MiaoshaServiceImpl implements MiaoshaService {


    @Autowired
    private GoodsService goodsService;

    @Resource(name = "myRedisTemplate")
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private RabbitmqSender mqSender;


    @Override
    public boolean checkPath(MiaoshaUser user, long goodsId, String path) {
        if (user == null || path == null) {
            return false;
        }
        String pathOld = (String) redisTemplate.opsForValue().get(RedisKey.MiaoshaPath+ user.getId() + ":" + goodsId);
        return path.equals(pathOld);
    }

    @Override
    public String createMiaoshaPath(MiaoshaUser user, long goodsId) {
        if (user == null || goodsId <= 0) {
            return null;
        }
        String str = MD5Util.md5(UUIDUtil.uuid() + "123456");
        redisTemplate.opsForValue().set(RedisKey.MiaoshaPath+ user.getId() + ":" + goodsId, str);
        return str;
    }

    @Override
    public void sendMsg(MiaoshaUser user, long goodsId) {
        //入队
        MiaoshaMessage mm = new MiaoshaMessage();
        mm.setUser(user);
        mm.setGoodsId(goodsId);
        mqSender.sendMiaoshaMessage(mm);
    }
}
