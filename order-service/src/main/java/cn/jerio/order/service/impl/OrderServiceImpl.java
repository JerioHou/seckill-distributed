package cn.jerio.order.service.impl;

import cn.jerio.constant.RedisKey;
import cn.jerio.idGenerator.service.IdService;
import cn.jerio.oder.service.OrderService;
import cn.jerio.order.dao.OrderDao;
import cn.jerio.pojo.MiaoshaOrder;
import cn.jerio.pojo.MiaoshaUser;
import cn.jerio.pojo.OrderInfo;
import cn.jerio.product.service.GoodsService;
import cn.jerio.vo.GoodsVo;
import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
 * Created by Jerio on 2019/03/04
 */
@Service
public class OrderServiceImpl implements OrderService{

    @Autowired
    private OrderDao orderDao;

    @Reference
    private IdService idService;

    @Resource(name = "myRedisTemplate")
    private RedisTemplate redisTemplate;

    @Reference
    private GoodsService goodsService;


    @Override
    @Transactional
    public OrderInfo miaosha(MiaoshaUser user, GoodsVo goods) {
        //减库存 下订单 写入秒杀订单
        boolean success = goodsService.reduceStock(goods);
        if(success) {
            //order_info maiosha_order
            return createOrder(user, goods);
        }else {
            setGoodsOver(goods.getId());
            return null;
        }
    }


    @Override
    public MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(long userId, long goodsId) {
        return (MiaoshaOrder) redisTemplate.opsForValue().get(RedisKey.USER_GOOD+userId+":"+goodsId);
    }

    @Override
    @Transactional
    public OrderInfo createOrder(MiaoshaUser user, GoodsVo goods) {

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(idService.nextId());
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getMiaoshaPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId(user.getId());
        orderDao.insert(orderInfo);
        MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
        miaoshaOrder.setId(idService.nextId());
        miaoshaOrder.setGoodsId(goods.getId());
        miaoshaOrder.setOrderId(orderInfo.getId());
        miaoshaOrder.setUserId(user.getId());
        orderDao.insertMiaoshaOrder(miaoshaOrder);
        redisTemplate.opsForValue().set(RedisKey.USER_GOOD+user.getId()+":"+goods.getId(),miaoshaOrder);
        return orderInfo;
    }

    @Override
    public OrderInfo getOrderById(long orderId) {
        return orderDao.getOrderById(orderId);
    }

    @Override
    public long getMiaoshaResult(Long userId, long goodsId) {
        MiaoshaOrder order = getMiaoshaOrderByUserIdGoodsId(userId, goodsId);
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

    @Override
    public int updateOrderStatusById(OrderInfo orderInfo) {
        return orderDao.updateOrderStatusById(orderInfo);
    }

    @Override
    public int closeOrder(Date deadLine) {
        return orderDao.colseOrder(deadLine);
    }

    private void setGoodsOver(Long goodsId) {
        redisTemplate.opsForValue().set(RedisKey.isGoodsOver +goodsId, true);
    }

    private boolean getGoodsOver(long goodsId) {
        return redisTemplate.persist(RedisKey.isGoodsOver +goodsId);
    }
}
