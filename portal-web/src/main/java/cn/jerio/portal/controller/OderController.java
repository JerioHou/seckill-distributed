package cn.jerio.portal.controller;

import cn.jerio.annotation.AccessLimit;
import cn.jerio.oder.service.OrderService;
import cn.jerio.pojo.MiaoshaUser;
import cn.jerio.pojo.OrderInfo;
import cn.jerio.product.service.GoodsService;
import cn.jerio.result.CodeMsg;
import cn.jerio.result.Result;
import cn.jerio.user.service.MiaoShaUserService;
import cn.jerio.vo.GoodsVo;
import cn.jerio.vo.OrderDetailVo;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Jerio on 2019/03/04
 */
@Controller
@RequestMapping("/order")
public class OderController {

    @Reference
    MiaoShaUserService userService;

    @Reference
    OrderService orderService;

    @Reference
    GoodsService goodsService;

    @AccessLimit
    @RequestMapping("/detail")
    @ResponseBody
    public Result<OrderDetailVo> info(Model model, MiaoshaUser user,
                                      @RequestParam("orderId") long orderId) {
        OrderInfo order = orderService.getOrderById(orderId);
        if(order == null) {
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        long goodsId = order.getGoodsId();
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        OrderDetailVo vo = new OrderDetailVo();
        vo.setOrder(order);
        vo.setGoods(goods);
        return Result.success(vo);
    }
}
