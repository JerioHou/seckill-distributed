package cn.jerio.page;

import cn.jerio.product.service.GoodsService;
import cn.jerio.vo.GoodsVo;
import com.alibaba.dubbo.config.annotation.Reference;
import com.google.common.collect.Maps;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.io.Writer;
import java.util.HashMap;

/**
 * Created by Jerio on 2019/03/07
 */
@Component
public class PageGenerator {

    @Value("${pagedir}")
    private String pagedir;
    @Autowired
    private Configuration configuration;

    @Reference
    private GoodsService goodsService;

    public void generatorHtml(long goodsId)  {
        try {
            Template template = configuration.getTemplate("goods_detail.ftl");
            GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
            long startAt = goods.getStartDate().getTime();
            long endAt = goods.getEndDate().getTime();
            long now = System.currentTimeMillis();
            int miaoshaStatus = 0;
            int remainSeconds = 0;
            if(now < startAt ) {//秒杀还没开始，倒计时
                miaoshaStatus = 0;
                remainSeconds = (int)((startAt - now )/1000);
            }else if(now > endAt){//秒杀已经结束
                miaoshaStatus = 2;
                remainSeconds = -1;
            }else {//秒杀进行中
                miaoshaStatus = 1;
                remainSeconds = 0;
            }

            HashMap<Object, Object> dataModel = Maps.newHashMap();
            dataModel.put("goods",goods);
            dataModel.put("remainSeconds",remainSeconds);
            dataModel.put("miaoshaStatus",miaoshaStatus);
            String fileName = this.getClass().getResource("/").toURI().getPath()+goodsId+".html";
            Writer out=new FileWriter(new File(fileName));
            template.process(dataModel, out);
            out.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
