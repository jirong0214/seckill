package com.geekouc.babytun.scheduler;

import com.geekouc.babytun.entity.Goods;
import com.geekouc.babytun.service.GoodsService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component //标记组件,spring ioc托管后自动加载
public class StaticTask {

    @Resource
    private Configuration freemarkerConfig;
    @Resource
    private GoodsService goodsService;
    @Scheduled(cron = "0 0/5 * * * ?")  //cron表达式:秒 分 小时 日 月 星期;这里是每隔5分钟执行一次;
    public void doStatic() throws IOException, TemplateException {
        Template template = freemarkerConfig.getTemplate("goods.ftlh");//获取模板对象;
        List<Goods> allGoods = goodsService.findLast5M();
        for (Goods goods : allGoods) {
            Long gid = goods.getGoodsId();
            Map<String, Object> param = new HashMap<>();
            param.put("goods", goodsService.getGoods(gid));
            param.put("covers", goodsService.findCovers(gid));
            param.put("details", goodsService.findDetails(gid));
            param.put("params", goodsService.findParams(gid));
            File targetFile = new File("/Users/tianjirong/Documents/babytun-static/goods/" + gid + ".html");//定义文件路径
            FileWriter out = new FileWriter(targetFile);//写入文件
            template.process(param, out);
            System.out.println(targetFile+"已生成!");
            out.close();
        }
    }
}
