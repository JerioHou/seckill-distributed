package cn.jerio.product.service.impl;


import cn.jerio.pojo.MiaoshaGoods;
import cn.jerio.product.dao.GoodsDao;
import cn.jerio.product.redis.GoodsKey;
import cn.jerio.product.service.GoodsService;
import cn.jerio.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by Jerio on 2018/3/20
 */
@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    GoodsDao goodsDao;

    @Autowired
    RedisTemplate redisTemplate;


    @Override
    public List<GoodsVo> listGoodsVo(){
        return goodsDao.listGoodsVo();
    }
    @Override
    public GoodsVo getGoodsVoByGoodsId(long goodsId) {


        String realKey  = GoodsKey.GoodsVo.getPrefix() + goodsId;
        GoodsVo goodsVo = (GoodsVo) redisTemplate.opsForValue().get(realKey);
        if (goodsVo == null){
            goodsVo = goodsDao.getGoodsVoByGoodsId(goodsId);
            redisTemplate.boundValueOps(realKey).set(goodsVo);
        }
        return goodsVo;
    }
    @Override
    public boolean reduceStock(GoodsVo goods) {
        MiaoshaGoods g = new MiaoshaGoods();
        g.setGoodsId(goods.getId());
        int ret = goodsDao.reduceStock(g);
        return ret > 0;
    }
}
