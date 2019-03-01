package cn.jerio.portal.controller;

import cn.jerio.pojo.MiaoshaUser;
import cn.jerio.product.service.GoodsService;
import cn.jerio.result.Result;
import cn.jerio.vo.GoodsDetailVo;
import cn.jerio.vo.GoodsVo;
import com.alibaba.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by Jerio on 2019/03/01
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;


    @RequestMapping(value="/to_list")
    public ModelAndView list(HttpServletRequest request, HttpServletResponse response,
                              MiaoshaUser user) {
//        model.addAttribute("user", user);
//        String html = redisTemplate.opsForValue().get(RedisKey.GOODS_LIST);
//        if (!StringUtils.isEmpty(html)){
//            return html;
//        }
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
//        model.addAttribute("goodsList", goodsList);
//        SpringWebContext context =  new SpringWebContext(request,response,request.getServletContext(),
//                request.getLocale(),model.asMap(),applicationContext);
//        html = thymeleafViewResolver.getTemplateEngine().process("goods_list", IContext);
//        redisTemplate.opsForValue().set(RedisKey.GOODS_LIST,html);
        //查询商品列表
//        return html;
        ModelAndView view = new ModelAndView("goods_list");
        view.addObject("user",user);
        view.addObject("goodsList",goodsList);
        return view;
    }

    @RequestMapping(value="/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user,
                                        @PathVariable("goodsId")long goodsId) {
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        long startAt = goods.getStartDate().getTime();
        long endAt = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();
        int miaoshaStatus = 0;
        int remainSeconds = 0;
        if(now < startAt ) {//秒杀还没开始，倒计时
            miaoshaStatus = 0;
            remainSeconds = (int)((startAt - now )/1000);
        }else  if(now > endAt){//秒杀已经结束
            miaoshaStatus = 2;
            remainSeconds = -1;
        }else {//秒杀进行中
            miaoshaStatus = 1;
            remainSeconds = 0;
        }
        GoodsDetailVo vo = new GoodsDetailVo();
        vo.setGoods(goods);
        vo.setUser(user);
        vo.setRemainSeconds(remainSeconds);
        vo.setMiaoshaStatus(miaoshaStatus);
        return Result.success(vo);
    }
}
