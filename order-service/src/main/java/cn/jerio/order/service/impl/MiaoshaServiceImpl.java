package cn.jerio.order.service.impl;

import cn.jerio.constant.RedisKey;
import cn.jerio.oder.service.MiaoshaService;
import cn.jerio.oder.service.OrderService;
import cn.jerio.order.mq.RabbitmqSender;
import cn.jerio.pojo.MiaoshaMessage;
import cn.jerio.pojo.MiaoshaOrder;
import cn.jerio.pojo.MiaoshaUser;
import cn.jerio.pojo.OrderInfo;
import cn.jerio.product.service.GoodsService;
import cn.jerio.util.MD5Util;
import cn.jerio.util.UUIDUtil;
import cn.jerio.vo.GoodsVo;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by Jerio on 2019/03/04
 */
@Service
public class MiaoshaServiceImpl implements MiaoshaService {

    @Reference
    private OrderService orderService;

    @Reference
    private GoodsService goodsService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Autowired
    private RabbitmqSender mqSender;

    @Override
    @Transactional
    public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
        //减库存 下订单 写入秒杀订单
        boolean success = goodsService.reduceStock(goods);
        if(success) {
            //order_info maiosha_order
            return orderService.createOrder(user, goods);
        }else {
            setGoodsOver(goods.getId());
            return null;
        }
    }

    @Override
    public BufferedImage createVerifyCode(MiaoshaUser user, long goodsId) {

        if (user == null || goodsId <= 0) {
            return null;
        }
        int width = 80;
        int height = 32;
        //create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        // set the background color
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);
        // draw the border
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // generate a random code
        String verifyCode = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(verifyCode, 8, 24);
        g.dispose();
        //把验证码存到redis中
        int rnd = calc(verifyCode);
        redisTemplate.opsForValue().set(RedisKey.MiaoshaVerifyCode+user.getId() + ":" + goodsId, rnd,300, TimeUnit.SECONDS);
        //输出图片
        return image;
    }

    private static char[] ops = new char[]{'+', '-', '*'};

    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = "" + num1 + op1 + num2 + op2 + num3;
        return exp;
    }

    private static int calc(String exp) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");
            return (Integer) engine.eval(exp);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean checkVerifyCode(MiaoshaUser user, long goodsId, int verifyCode) {
        if (user == null || goodsId <= 0) {
            return false;
        }
        Integer codeOld = (Integer) redisTemplate.opsForValue().get(RedisKey.MiaoshaVerifyCode+user.getId() + ":" + goodsId);
        if (codeOld == null || codeOld - verifyCode != 0) {
            return false;
        }
        redisTemplate.delete(RedisKey.MiaoshaVerifyCode+user.getId() + ":" + goodsId);
        return true;
    }

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
    public long getMiaoshaResult(Long userId, long goodsId) {
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
        if (order != null) {//秒杀成功
            return order.getOrderId();
        } else {
            boolean isOver = getGoodsOver(goodsId);
            if (isOver) {
                return -1;
            } else {
                return 0;
            }
        }
    }



    private void setGoodsOver(Long goodsId) {
        redisTemplate.opsForValue().set(RedisKey.isGoodsOver +goodsId, true);
    }


    private boolean getGoodsOver(long goodsId) {
        return redisTemplate.persist(RedisKey.isGoodsOver +goodsId);
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
