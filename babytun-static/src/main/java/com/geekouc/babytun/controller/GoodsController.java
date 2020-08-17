package com.geekouc.babytun.controller;

import com.geekouc.babytun.entity.Evaluate;
import com.geekouc.babytun.entity.Goods;
import com.geekouc.babytun.service.GoodsService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;import java.util.List;
import java.util.Map;

@Controller //ioc托管
public class GoodsController {
    //日志记录器;
    Logger logger = LoggerFactory.getLogger(GoodsController.class);
    @Resource
    private GoodsService goodsService;


    @GetMapping("/goods") //当访问网址为/goods时,返回goods.ftl页面
    public ModelAndView showGoods(Long gid){
        logger.info("gid:"+gid);//打印日志信息;
        ModelAndView mav = new ModelAndView("/goods");

        mav.addObject("goods",goodsService.getGoods(gid));//将查询到的goods信息放入页面中;
        mav.addObject("covers",goodsService.findCovers(gid));//将查询到的goods封面放入页面中;
        mav.addObject("details",goodsService.findDetails(gid));//将查询到的goods封面放入页面中;
        mav.addObject("params",goodsService.findParams(gid));//将查询到的goods封面放入页面中;

        return mav;
    }

    @GetMapping("/evaluate/{gid}")
    @ResponseBody //@ResponseBody注解表示该方法的返回的结果直接写入 HTTP 响应正文中，一般在异步获取数据时使用；
    //@PathVariable可以用来映射URL中的占位符到目标方法的参数中
    public List<Evaluate> findEvaluate(@PathVariable("gid") Long goodsId){
        return goodsService.findEvaluates(goodsId);
    }

    //freemarker的核心配置类,用于动态生成模板对象;
    //在springboot ioc容器初始化的时候,configuration对象自动就被实例化了.
    @Resource
    private Configuration freemarkerConfig;

    @GetMapping("/static/{gid}")
    @ResponseBody
    public String doStatic(@PathVariable("gid")  Long gid) throws IOException, TemplateException {
        Template template = freemarkerConfig.getTemplate("goods.ftlh");//获取模板对象;
        Map<String,Object> param = new HashMap<>();
        param.put("goods",goodsService.getGoods(gid));
        param.put("covers",goodsService.findCovers(gid));
        param.put("details",goodsService.findDetails(gid));
        param.put("params",goodsService.findParams(gid));

        File targetFile = new File("/Users/tianjirong/Documents/babytun-static/goods/"+gid+".html");//定义文件路径
        FileWriter out = new FileWriter(targetFile);//写入文件
        template.process(param,out);
        out.close();

        return targetFile.getPath(); //返回文件路径
    }


    //批量处理所有的静态页面;
    @GetMapping("/static_all")
    @ResponseBody
    public String doStaticAll() throws IOException, TemplateException {
        Template template = freemarkerConfig.getTemplate("goods.ftlh");//获取模板对象;
        List<Goods> allGoods = goodsService.findAllGoods();
        for (Goods goods : allGoods) {
            Long gid = goods.getGoodsId();
            Map<String,Object> param = new HashMap<>();
            param.put("goods",goodsService.getGoods(gid));
            param.put("covers",goodsService.findCovers(gid));
            param.put("details",goodsService.findDetails(gid));
            param.put("params",goodsService.findParams(gid));
            File targetFile = new File("/Users/tianjirong/Documents/babytun-static/goods/"+gid+".html");//定义文件路径
            FileWriter out = new FileWriter(targetFile);//写入文件
            template.process(param,out);
            out.close();
        }
        return "ok!"; //处理完成,返回ok
    }
}
