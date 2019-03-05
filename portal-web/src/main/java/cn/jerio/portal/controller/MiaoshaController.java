package cn.jerio.portal.controller;

import cn.jerio.annotation.AccessLimit;
import cn.jerio.constant.RedisKey;
import cn.jerio.oder.service.OrderService;
import cn.jerio.pojo.MiaoshaOrder;
import cn.jerio.pojo.MiaoshaUser;
import cn.jerio.portal.init.InitRateLimiter;
import cn.jerio.product.service.GoodsService;
import cn.jerio.product.service.MiaoshaService;
import cn.jerio.result.CodeMsg;
import cn.jerio.result.Result;
import cn.jerio.user.service.MiaoShaUserService;
import cn.jerio.util.ImageUtil;
import cn.jerio.vo.GoodsVo;
import com.alibaba.dubbo.config.annotation.Reference;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static cn.jerio.util.ImageUtil.calc;

/**
 * Created by Jerio on 2019/03/04
 */
@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {

    @Reference
    MiaoShaUserService userService;


    @Reference
    GoodsService goodsService;

    @Reference
    OrderService orderService;

    @Reference
    MiaoshaService miaoshaService;

    @Resource(name = "myRedisTemplate")
    private RedisTemplate redisTemplate;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private HashMap<Long, Boolean> localOverMap =  new HashMap<Long, Boolean>();

    /**
     * 系统初始化
     * */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goodsList = goodsService.listGoodsVo();
        if(goodsList == null) {
            return;
        }
        for(GoodsVo goods : goodsList) {
            stringRedisTemplate.opsForValue().set(RedisKey.MiaoshaGoodsStock+goods.getId(), goods.getStockCount().toString());
            localOverMap.put(goods.getId(), false);
        }
    }

    @HystrixCommand(fallbackMethod = "miaoshaFallback")
    @AccessLimit
    @RequestMapping(value="/{path}/do_miaosha", method= RequestMethod.POST)
    @ResponseBody
    public Result<Integer> miaosha(Model model,MiaoshaUser user,
                                   @PathVariable("path")String path,
                                   @RequestParam("goodsId")long goodsId) {
        model.addAttribute("user", user);

        //验证path
        boolean check = miaoshaService.checkPath(user, goodsId, path);
        if(!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        // 使用AtomicInteger预减库存
        if(InitRateLimiter.getGoodAtomicInteger(goodsId).decrementAndGet() < 1)
            return Result.error(CodeMsg.MIAO_SHA_OVER);

        /*
        内存标记，部分减少redis访问
        hashmap是有线程安全问题的，但是在这里，localOverMap初始化后并不会增加新的key
        而是不停地对已有的key 覆盖其值。
        同时如果线程A get操作后被挂起，而线程B修改了localOverMap的值，那么本次修改对线程A不可见.
        因此会出现秒杀结束但有线程能继续执行下面的代码，特别是线程数量特别多的情况下，
        所以此处只是部分减少redis请求，但仍能启动一定作用，特别是没有 “预减库存”时，能较大程度较少消息的数量
        */
//        boolean over = localOverMap.get(goodsId);
//        if(over) {
//            return Result.error(CodeMsg.MIAO_SHA_OVER);
//        }
        //判断是否已经秒杀到了
        MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if(order != null) {
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }

        /*
        预减库存
        此处会存在少买的可能性，即redis减库存成功，但DB减库存失败
        预减库存的好处是能极大地较少 消息的数量，提高系统的响应速度。
        reids是单线程的，所以decr是线程安全的，所以不用担心超卖问题，同时数据库也做了超卖的限制

        如果对库存要求能严格，不能少也不能多，则不能采用这种方式。
        */
        long stock =  stringRedisTemplate.boundValueOps(RedisKey.MiaoshaGoodsStock+goodsId).increment(-1);

        if(stock < 0) {
            localOverMap.put(goodsId,true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }
        //入队
        miaoshaService.sendMsg(user,goodsId);
        return Result.success(0);//排队中
    }

    /**
     * 秒杀降级方法
     */
    private Result<Integer> miaoshaFallback(Model model,MiaoshaUser user,
                                    @PathVariable("path")String path,
                                    @RequestParam("goodsId")long goodsId){
        return Result.error(CodeMsg.FALL_BACK);
    }

    @AccessLimit
    @RequestMapping(value="/verifyCode", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaVerifyCod(HttpServletResponse response, MiaoshaUser user,
                                              @RequestParam("goodsId")long goodsId) {
        if (user == null || goodsId <= 0) {
            return null;
        }
        try {
            String verifyCode = ImageUtil.generateVerifyCode();
            BufferedImage image = ImageUtil.createVerifyCode(verifyCode);
            OutputStream out = response.getOutputStream();
            ImageIO.write(image, "JPEG", out);
            out.flush();
            out.close();
            int result = calc(verifyCode);
            redisTemplate.opsForValue().set(RedisKey.MiaoshaVerifyCode+user.getId() + ":" + goodsId, result,300, TimeUnit.SECONDS);
            return null;
        }catch(Exception e) {
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }

    @HystrixCommand(fallbackMethod = "getMiaoshaPathFallback")
    @AccessLimit(rateLimiter = true,rateLimiterName = "getMiaoshaPath",rateLimiterValue = 200.0)
    @RequestMapping(value="/path", method=RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoshaPath(HttpServletRequest request, MiaoshaUser user,
                                         @RequestParam("goodsId")long goodsId,
                                         @RequestParam(value="verifyCode", defaultValue="0")int verifyCode) {

        if(user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        //验证秒杀是否开始
        //未开始 或 已结束，都不暴露秒杀地址
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        Date endTime = goods.getEndDate();
        Date startTime = goods.getStartDate();
        Date nowTime = new Date();
        if (endTime.getTime() < nowTime.getTime() || startTime.getTime() > nowTime.getTime()){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        boolean check = checkVerifyCode(user, goodsId, verifyCode);
        if(!check) {
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }
        String path = miaoshaService.createMiaoshaPath(user, goodsId);
        return Result.success(path);
    }

    /**
     * getMiaoshaPath的降级方法
     *
     */
    private Result<String> getMiaoshaPathFallback(HttpServletRequest request, MiaoshaUser user,
                                                  @RequestParam("goodsId")long goodsId,
                                                  @RequestParam(value="verifyCode", defaultValue="0")int verifyCode){
        return Result.error(CodeMsg.FALL_BACK);
    }

    /**
     * orderId：成功
     * -1：秒杀失败
     * 0： 排队中
     * */
    @AccessLimit
    @RequestMapping(value="/result", method= RequestMethod.GET)
    @ResponseBody
    public Result<String> miaoshaResult(Model model, MiaoshaUser user,
                                        @RequestParam("goodsId")long goodsId) {
        model.addAttribute("user", user);

        long result  = orderService.getMiaoshaResult(user.getId(), goodsId);
        //long类型在前端会出现精度丢失问题，故采用string类型传输
        return Result.success(String.valueOf(result));
    }

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
}
