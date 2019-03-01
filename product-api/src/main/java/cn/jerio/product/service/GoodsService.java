package cn.jerio.product.service;


import cn.jerio.vo.GoodsVo;

import java.util.List;

public interface GoodsService {


    List<GoodsVo> listGoodsVo();

    GoodsVo getGoodsVoByGoodsId(long goodsId) ;

    boolean reduceStock(GoodsVo goods) ;

}

