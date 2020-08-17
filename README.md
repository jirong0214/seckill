# seckill
#### 基于 SpringBoot+Mybatis+Redis的高并发秒杀API

- **业务需求**：

  ​	此项目模拟的是电商活动中一个商品的高并发秒杀场景。商品列表中有N种商品，每个商品都有自己的库存数目，每个商品的秒杀开始时间也不相同。每个用户只可以秒杀成功一次某种商品，不同的商品可以分别秒杀。

- **业务逻辑**：

  **数据优化**：

  - **热点数据缓存**：启用SpringBoot的声明式缓存支持，将热门商品信息等热点数据放入Redis缓存，降低服务器压力；
  - **内容分发网络**：使用CDN存储静态资源，解决Tomcat服务器带宽瓶颈；
  - **动静数据分离**：前端采用Freemarker模板引擎将页面静态化，并使用ajax动态加载后端产生的数据。

  **并发优化**：

  - **避免超卖**：商品库存信息放入Redis，在Redis缓存中预减库存，完成秒杀用户确权与避免商品超卖问题；
  - **削峰限流**：使用RabbitMQ消息队列，用户秒杀成功确权与订单信息生成这两个过程异步执行，实现削峰限流和业务逻辑解耦。
  - **负载均衡**：使用Nginx反向代理服务器，将用户请求转发到多台服务器，实现负载均衡及更高的并发量；同时使用分布式Session，将 Session转存到Redis中实现 Session 共享访问。

  **安全优化**：

  - **URL加盐**：后端动态生成商品秒杀的路径，等到商品秒杀的时间获取秒杀路径的接口才会返回秒杀路径；

  - **防刷与反爬**：在Redis中保存当前客户端的访问次数，对于在设定时间内超过访问次数限制的IP，使用AOP拦截器对其拒绝服务。

  - **服务降级和服务熔断**：当某个服务单元发生故障监控，向调用方法返回一个符合预期的、可处理的备选响应。**（未实现）**

#### 技术栈：

- SpringBoot 2.x
- Mybatis
- Redis
- RabbitMQ
- Nginx
- MySQL

#### 1. 配置依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.3.2.RELEASE</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
    <groupId>com.geekouc</groupId>
    <artifactId>babytun-seckill</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>babytun-seckill</name>
    <description>Demo project for Spring Boot</description>

    <properties>
        <java.version>1.8</java.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-freemarker</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
            <version>2.1.3</version>
        </dependency>

        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
            <exclusions>
                <exclusion>
                    <groupId>org.junit.vintage</groupId>
                    <artifactId>junit-vintage-engine</artifactId>
                </exclusion>
            </exclusions>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>

</project>

```

```xml
mapper头模板：
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
  PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
  "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
```

#### 2. 分层编写展示商品信息的后端和前端页面： view -> controller -> service -> dao



#### 3. 压力测试：Jmeter

启动命令：sh jmeter

![image-20200811231556871](https://i.loli.net/2020/08/11/vBLh8yTw6gGJzqH.png)

模拟100用户、每个用户进行100次访问；

未使用redis缓存时：

![image-20200811232126997](https://i.loli.net/2020/08/11/M8bmnruvPWTaezs.png)

- **Throughput** **:吞吐量：每秒489次请求**

- Average/Min/Max 响应时间：192/s



### 高并发问题分析：

​		在电商应用中,90%数据处理是用于**读取**数据,在海量数据的情况下,**数据库**最有可能成为**高并发的瓶颈**。因此**提高数据库效率**戓者**降低数据库交互**就是我们高并发首先要考虑的向题。

​		电商应用中,很大ー部分数据是在一段时间内稳定不变的, 其中很大ー部分数据是一段时间內稳定不变的,例如”商 信息"、"”会员信息“、“网站基本信息”等；对于稳定数据,常用两种方式进行高并发处理：

- 利用**缓存**( Redis、 Ehcache、 Memcached..) 
- 利用**静态化技术**(staticize)转化为Html



#### 4. 静态数据优化：Redis缓存

**Redis在Springboot中的使用：**

**4.1 在主程序中开启声明式缓存注解支持**

![image-20200812000958909](https://i.loli.net/2020/08/12/tdoGh3carImNRYv.png)

**4.2 对service层中的方法,利用@Cacheable注解开启缓存**

![image-20200812001021759](https://i.loli.net/2020/08/12/AutzQpOKCnRIyJj.png)

**4.3 在yml中配置Redis信息：**

![image-20200812001736099](https://i.loli.net/2020/08/12/N4ksY7B95TtE6Dj.png)

**4.4 使用redis后的JMeter吞吐量:**

![image-20200812005356116](https://i.loli.net/2020/08/12/pLEWIlcMh4FujmO.png)



#### 5. 页面静态化技术：[以空间换时间]

​		页面静态化是指将动态页面(jsp/ freemarker,…)变成html静态页面。**动态页面便于管理**,但是访问网页时还需要程序先处理一遍,所以导致访问速度相对较慢。而**静态页面访问速度快**,却又不便于管理。**静态化可以将两种页面的好处集中到一起**。

![image-20200812010528716](https://i.loli.net/2020/08/12/Y3BevfmSyuWcph5.png)

动态生成模板对象：

![image-20200812112335667](https://i.loli.net/2020/08/12/V4z5U6iB72oQCAJ.png)

注：可以使用大循环直接对所有商品页面进行静态化即可；

#### Nginx(反向代理服务器)

​		Nginx是一款轻量级的**Web服务器/反向代理服务器**, 其特点是**占内存少,并发能力强**,事实上 nginx的并发能力确实在同类型的网页服务器中表现较好。

安装：brew install nginx

启动：brew services start nginx

路径：/usr/local/etc/nginx/nginx.conf

配置：在39行设置需要映射的根目录

![image-20200812131929792](https://i.loli.net/2020/08/12/jgrJukMKb3YQNxf.png)

**使用nginx+静态化页面的并发表现：**

![image-20200812133244195](https://i.loli.net/2020/08/12/dor42AWTXg6Sz93.png)



#### 静态化后的额外处理：

**5.1 自动计划任务静态化**

使用Springboot的计划任务自动生成静态化页面：		

![image-20200812133838882](https://i.loli.net/2020/08/12/7hxvEYtidUk8PQG.png)

找到数据库中上次修改不超过5分钟的数据：

![image-20200812135715368](https://i.loli.net/2020/08/12/vypADPTGR1bhqeZ.png)

制定间隔5分钟的计划任务：

![image-20200812140403359](https://i.loli.net/2020/08/12/egyAh9nvZfuHIJR.png)



**5.2 动静态数据分离：**

​	页面静态化执行效率固然高,但往往在页面中也存在动态数据。例如“评论”的内容就一直在不断变化肯定不能对其静态化处理。**遇到这种动态数据需要在静态页面中使用AJAX动态加载后端产生的数据**。

​	![image-20200812141917901](https://i.loli.net/2020/08/12/FRUkviKuDQEgtqN.png)

- 使用ajax 加载动态的评论内容，写在goods.ftlh中：

![image-20200812232744872](https://i.loli.net/2020/08/12/UFaR3uqochsnTbm.png)

再重新生成全部的nginx静态化页面；

注意：此时，从nginx服务器访问网页，还不能找到对应的评论内容:

![image-20200812234200597](https://i.loli.net/2020/08/12/5EvgojeKDYPzJ1U.png)

**因此需要配置Nginx代理**：

![image-20200812235453829](https://i.loli.net/2020/08/12/GK3D965RzEtbpaC.png)

​		通过此配置可以将 nginx的 /evaluate/页面代理到tomcat服务器的http://locathost:8080/evaluate; 注意要保持开启tomcat服务器，否则无法成功代理。

**总结动静分离的效果：**访问静态化的页面时，如果需要访问动态数据：可以通过配置Nginx代理，将某页面代理到tomcat服务器，实现获取动态数据。



#### 6. 秒杀问题分析：

​		秒杀我们日常开发中最常见的高并发场景。**秒杀的特点**：1）瞬超高访问量;  2)商品总量有限,先到先得;  3)有明确的开始、结束时间。

秒杀活动常见**两个挑战**:

- **高并发:** 基本主流电商的秒杀QPS峰值都在100万+。
- **避免超卖:** 如何避免购买商品人数不超过商品数量上限,这是要面临的难题。

<img src="https://i.loli.net/2020/08/13/JBqLMRbHmCD6SKr.png" alt="image-20200813141732943" style="zoom:50%;" />

<img src="https://i.loli.net/2020/08/13/TxRobN2tv5E1KYM.png" alt="image-20200813141751633 " style="zoom:50%;" />

**商品库存count的改变未保证对其他线程的可见性，因此发生超卖问题。**

解决方法：有多种方案来解决这个问题，我们主要看3种方案：

- 悲观锁：影响性能；

- 乐观锁：高并发下失败率高，可引入重入机制在失败后重复尝试；

- **Redis+ Lua**

- #### 使用Redis预减库存-> 解决超卖问题：

**为什么选择 Redis**：

- 单线程模型
- 内存存储,高达10WQPS 
- 天生分布式支持

<img src="https://i.loli.net/2020/08/13/Na9RSKfYqx7lkQg.png" alt="image-20200813215318480 " style="zoom:50%;" />



#### 实现过程：

**6.1 编写mapper 数据库操作**：

```xml
//查询出符合秒杀时间的商品：
<mapper namespace="com.geekouc.babytunseckill.dao.PromotionSecKillDAO">
    <select id="findUnstartSecKill" resultType="com.geekouc.babytunseckill.entity.PromotionSecKill">
        select * from t_promotion_seckill 
      	where now() BETWEEN start_time AND end_time and status = 0
    </select>
</mapper>
//更新秒杀状态：
<update id="update" parameterType="com.geekouc.babytunseckill.entity.PromotionSecKill">
        update t_promotion_seckill
        set goods_id = #{goodsId},ps_count = #{psCount},
  					start_time = #{startTime}, end_time = #{endTime},
            status = #{status}, current_price = #{currentPrice} 
  			where ps_id = #{psId}
    </update>
```



**6.2 秒杀调度任务**：每隔5秒检查符合秒杀时间的商品，使其进入秒杀状态,并将其放入Redis的List：

```java
@Component
public class SecKillTask {
    @Resource
    private PromotionSecKillDAO promotionSecKillDAO;
    @Resource
    private RedisTemplate redisTemplate;
    //RedisTemplate是Spring封装的Redis操作类,提供了一系列操作redis的模板方法
    
    @Scheduled(cron = "0/5 * * * * ?")
    public void startSecKill(){
        List<PromotionSecKill> list = promotionSecKillDAO.findUnstartSecKill();
        for (PromotionSecKill ps : list) {
          //删除以前重复的活动任务缓存
            redisTemplate.delete("seckill:count:" + ps.getPsId());
            System.out.println(ps.getPsId() + "秒杀活动已启动 !");
            for (int i = 0; i < ps.getPsCount(); i++) {
              //有几个库存商品,则初始化几个list对象;list中先存入商品ID;
              redisTemplate.opsForList().rightPush("seckill:count:" + ps.getPsId(),ps.getGoodsId());
            }
            ps.setStatus(1);
            promotionSecKillDAO.update(ps);
        }
    }
}
```

**6.3 使用Redis实现库存预减**：如果能从秒杀商品队列中获取有效的goodsId,就将psId和对应的userId放入Redis的set中:

```java
@Service
public class PromotionSecKillService {
    @Resource
    private PromotionSecKillDAO promotionSecKillDAO;
    @Resource
    private RedisTemplate<Object,Object> redisTemplate;

    public void processSecKill(Long psId, String userid, Integer num) throws SecKillException {
    //如果能从秒杀商品队列中获取有效的goodsId,就将psId和对应的userId放入Redis的set中;
        PromotionSecKill ps = promotionSecKillDAO.findById(psId);
        if (ps == null) {
            //秒杀活动不存在:
            throw new SecKillException("该秒杀活动不存在!");
        }
        if (ps.getStatus() == 0) {
            throw new SecKillException("该秒杀活动还未开始!");
        }
        if (ps.getStatus() == 2) {
            throw new SecKillException("该秒杀活动已结束!");
        }
        Integer goodsId = (Integer) redisTemplate.opsForList().leftPop("seckill:count:" + ps.getPsId());
        if (goodsId != null) {
            //先判断用户id的set集合中是否已经存在此id,若已存在则不允许再次抢购
            boolean isExisted = redisTemplate.opsForSet().isMember("seckill:users:" + ps.getPsId(), userid);
            if (!isExisted) {
                System.out.println("恭喜" + userid + "抢到商品了,快去下单吧!");
                redisTemplate.opsForSet().add("seckill:users:" + ps.getPsId(), userid);
            }else {
                //若该用户已抢购过，抛出自定义异常，并再将此商品加回队列的尾部；
                redisTemplate.opsForList().rightPush("seckill:count:" + ps.getPsId(),ps.getGoodsId());
                throw new SecKillException("抱歉,您已经参加过此活动,请勿重复抢购!");
            }
        } else {
            throw new SecKillException("抱歉,该商品已被抢光,下次再来吧!");
        }
    }
}
```

**6.4 Controller层**：控制前端页面访问相应页面时调用秒杀方法，并返回结果：

```java
@RestController
public class SecKillController {
    @Resource
    PromotionSecKillService promotionSecKillService;

    @RequestMapping("/seckill")
    public Map<String, String> processSecKill(Long psid, String userid) {
        Map<String, String> result = new HashMap<>();
        try {
            promotionSecKillService.processSecKill(psid, userid, 1);
            result.put("code", "0");
            result.put("message", "success");
        } catch (SecKillException e) {
            result.put("code", "500");
            result.put("message", e.getMessage());
        }
        return result;
    }
}
```

**6.5** 设置前端seckill.html的抢购按钮入口，告诉用户抢购结果：

![image-20200814175009551](https://i.loli.net/2020/08/14/1PCdutoH9sNxJDK.png)

**6.6** 秒杀活动结束后：

将此秒杀任务状态设为已过期,并更新此状态,并清除redis中已过秒杀时间的商品：

```java
@Scheduled(cron = "0/5 * * * * ?")
    public void endSecKill(){
        List<PromotionSecKill> psList = promotionSecKillDAO.findExpireSecKill();
        for (PromotionSecKill ps : psList) {
            System.out.println(ps.getPsId()+"秒杀活动已结束!");
            //秒杀结束后,将此秒杀任务状态设为已过期,并更新此状态
            ps.setStatus(2);
            promotionSecKillDAO.update(ps);
            //删除redis中已过秒杀时间的商品;
            redisTemplate.delete("seckill:count" + ps.getPsId());
        }
    }
```

#### 7. 使用RabbitMQ：削峰、限流

<img src="https://i.loli.net/2020/08/15/tskMaEXieUHcpBD.png" alt="image-20200815003046510" style="zoom:50%;" />





<img src="https://i.loli.net/2020/08/15/M5pc3rfOYlAHmX4.png" alt="image-20200815003054692 " style="zoom:50%;" />

<img src="https://i.loli.net/2020/08/15/PyB7XD2A4kaS3wJ.png" alt="image-20200815140119283 " style="zoom:50%;" />

7.0 配置好RabbitMQ的环境，创建一个Exchange和一个队列；

7.1 编写Service **向MQ队列发送订单号（速度快）**，并在Controller层调用此方法：

```java
public String sendOrderToQueue(String userid){
        System.out.println("准备向队列发送信息...");
        //订单基本信息;
        HashMap<String,String> data = new HashMap<>();
        data.put("userid",userid);
        String orderNo = UUID.randomUUID().toString();
        data.put("orderNo",orderNo);
        //可附加额外的订单信息,如电话 地址等;
        rabbitTemplate.convertAndSend("exchange-order",null,data);
        return orderNo;
    }
```

```java
@RequestMapping("/seckill")
    public Map<String,Object> processSecKill(Long psid, String userid) {
        Map<String, Object> result = new HashMap<>();
        try {
            promotionSecKillService.processSecKill(psid, userid, 1);
            String orderNo = promotionSecKillService.sendOrderToQueue(userid);
            HashMap<String,String> data = new HashMap<>();
            data.put("orderNo",orderNo); //生成订单编号
            result.put("code", "0");
            result.put("message", "success");
            result.put("data",data);
        } catch (SecKillException e) {
            result.put("code", "500");
            result.put("message", e.getMessage());
        }
        return result;
    }
```

7.2 同时，将**生成的订单号利用ajax回调给前端（速度快）**：

<img src="https://i.loli.net/2020/08/15/qsf5XM6z1ovdJKN.png" alt="image-20200815114423021 " style="zoom:50%;" />

7.3 **配置Rabbit消费者信息**，并新建一个消费者类：

```yml
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /
    listener:
      simple:
        #定义消费者最多同时处理10个消息
        prefetch: 10
        #消息手动确认
        acknowledge-mode: manual
```

7.4 @RabbitHandler注解：**自动从RabbitMQ队列中获取订单号，并实例化一个订单写入数据库中（速度慢）**：

```java
@Component
public class OrderConsumer {
    @Resource
    private OrderDAO orderDAO;

    @RabbitListener(    //绑定创建好的rabbitmq交换机和队列
            bindings = @QueueBinding(
                  value = @Queue(value = "queue-order"),
                  exchange = @Exchange(value = "exchange-order",type = "fanout")
            )
    )
    @RabbitHandler  //消费者获取订单数据,插入到数据库中;
    public void handleMessage(@Payload Map<String,Object> data, Channel channel,
                              @Headers  Map<String,Object> headers){
        System.out.println("========获取到订单数据"+data+"========");
        try {
            try {
                //sleep 500ms,模拟对接支付、物流系统、日志登记...
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Order order = new Order();
            order.setOrderNo(data.get("orderNo").toString());
            order.setOrderStatus(0);
            order.setUserid(data.get("userid").toString());
            order.setRecvName("xxx");
            order.setRecvAddress("xxx");
            order.setRecvMobile("138********");
            order.setAmount(19.8f);
            order.setPostage(0f);
            order.setCreateTime(new Date());
            orderDAO.insert(order);
            Long tag = (Long)headers.get(AmqpHeaders.DELIVERY_TAG);
            channel.basicAck(tag,false);//消息确认,false:只进行单个接收 不进行批量接收
            System.out.println(data.get("orderNo"+"订单已创建"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

7.5 由于生产者和消费者是异步的关系，因此创建一个"正在创建订单,请稍后..."的页面，作为过渡：

<img src="https://i.loli.net/2020/08/15/kTVd79Kz4Pyhmnr.png" alt="image-20200815145349034 " style="zoom:50%;" />

同时，**检查订单号是否已经在数据库中成功生成**

```java
    //检查订单号是否已经在数据库中成功生成
    @GetMapping("/checkorder")
    public ModelAndView checkOrder(String orderNo){
        ModelAndView mav = new ModelAndView();
        Order order = promotionSecKillService.checkOrder(orderNo);
        if(order != null){
            //代表订单已在数据库中创建好了
            mav.addObject("order",order);
            mav.setViewName("/order");
            //跳转到order页面,显示订单信息;
        }else{
            mav.addObject("orderNo",orderNo);
            mav.setViewName("/waiting");
            //跳转到等待页面...在等待页面等待三秒后再次尝试检查订单号...
        }
        return mav;
    }
```



#### 8. Nginx 负载均衡

<img src="https://i.loli.net/2020/08/15/ovnwH7kT5FK3g1P.png" alt="image-20200815155440698 " style="zoom:50%;" />

Nginx**六种负载均衡策略**：

- **Default - 轮询策略** 
- **Least connected - 最少连接策略**
- **Weighted - 权重策略**
- **IP Hash - IP绑定策略** ：高并发下不推荐使用，因为会使负载不均衡
- fair-按响应时间(第三方) 
- url hash-url分配策略(第三方)

**8.1 使用Nginx代理后端服务器**：

```conf
    #后端服务器池
    upstream babytun {
    	#least_conn; #最少连接策略
    	#ip_hash;	#ip_hash策略

    	server 192.168.1.3:8001 weight=5; #按照权重分配
    	server 192.168.1.3:8002 weight=2;
    	server 192.168.1.3:8003 weight=1;
    	server 192.168.1.3:8004 weight=2;
    }

    server {
    		#nginx通过80端口提供服务
        listen       80;  
        #使用babytun服务器池进行后端处理
        location /{
        	proxy_pass http://babytun;
        	proxy_set_header Host $host;
        	proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        }
```

​		配置完成后，只需访问192.168.1.3/goods?gid=1234 即可访问我们的页面，nginx默认按照轮询策略在4台server之间切换服务器。



**8.2 Nginx分布式Session同步问题：**

​		例如使用Nginx负载均衡时,用户登录后,再刷新页面可能会代理到另一台server,而此台server没有之前的session,从而导致丢失登录状态。

**解决办法**：**将 Session转存到 Redis中实现 Session 共享访问**:



![image-20200815215155079 ](https://i.loli.net/2020/08/15/vSYuaK24FeN63yi.png)

- pom中引入Spring-Session依赖以及Redis依赖
- 主程序启用@EnableRedisHttpSession即可；

**Spring-Session将自动监听Session，并将其保存到Redis中**！

​		这样，用户登录上去之后，之后无论是再代理到哪台服务器都会一直拥有这个Session,从而保持登录状态。



#### 9. Nginx缓存静态资源降低Tomcat压力：

**9.1 图片、样式等静态资源**不再经过Tomcat服务器，直接由Nginx服务器指向指定文件夹：

<img src="https://i.loli.net/2020/08/16/7wy9ZbANBcmxMpz.png" alt="image-20200816002658224 " style="zoom:50%;" />

在/usr/local/etc/nginx/nginx.conf中修改配置即可：

```conf
#临时文件夹
    proxy_temp_path /Users/tianjirong/Documents/babytun-lb/nginx-temp;
    #设置缓存目录;
    #levels代表采用1:2,即采用两级目录的形式保存静态缓存文件，同时文件名进行了MD5编码;
    #keys_zone 定义缓存名称 以及 内存大小使用100M交换空间;
    #如果某个缓存文件超过7天未使用,则删除之;
    #文件夹最大不超过20g,超过后自动删除访问频率最低的缓存文件;
    proxy_cache_path /Users/tianjirong/Documents/babytun-lb/nginx-cache levels=1:2 keys_zone=babytun-cache:100m inactive=7d max_size=20g;
```

```conf
    server {
    	#nginx通过80端口提供服务
        listen       80;  

        #静态资源缓存,利用正则表达式匹配url,匹配成功的则执行内部逻辑,~*表示不区分大小写:
        location ~* \.(gif|jpg|css|png|js|woff|html)(.*){
            proxy_pass http://babytun;
            proxy_set_header Host $host;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_cache babytun-cache;
            #如果资源相应状态码为200->成功; 302->暂时性重定向时,资源缓存文件有效期1天;
            proxy_cache_valid 200 302 24h;
            proxy_cache_valid 301 24h;
        }
```

注意：当需要缓存**.html**的页面时，需要保证该页面是不经常变动的，否则不应该缓存它。

**9.2 Nginx使用Gzip资源压缩：节省带宽**：

- 利用浏览器支持的Gzip压缩, **nginx打包压缩并传输 css、js等静态资源**,可将带宽压力降低30%~70%

![image-20200816132844932](https://i.loli.net/2020/08/16/5TFjhuVRGq314zd.png)

在/usr/local/etc/nginx/nginx.conf中开启Gzip即可：

```conf
    #开启nginx Gzip压缩
    gzip  on;
    #超过1K的文件才压缩
    gzip_min_length 1k;
    #压缩哪些类型:对文本类型压缩效果很好 对图片效果不好
    gzip_types text/plain application/javascript text/css application/x-javascript;
    #当使用低版本IE浏览器时禁用压缩
    gzip_disable "MSIE [1-6]\.";
    #压缩使用的缓存,每个内存也为4K,申请32倍;一般这样写就可以
    gzip_buffers 32 4k;
    #最重要: 设置压缩级别: 1-9 越大压缩比越高,但浪费CPU资源,建议1-4即可
    gzip_comp_level 1;
```

**9.3 使用CDN：解决带宽瓶颈、加速访问速度**

​		可将整个layui文件夹上传到CDN中,这样传输这些资源的时候,流量就不会走本地服务器,而是直接走CDN的服务器,实现解决带宽瓶颈和加速访问速度！

**使用方法**：

- 开通阿里云等**OSS**服务,向OSS中上传我们的资源
- 开通阿里云**CDN**服务,将OSS中的资源分发到各个CDN服务器
- 将自己的**域名绑定映射**到CDN服务器
- 在前端页面中将所用资源的本地路径改为自己域名中的**远程路径**即可



#### 10. 流量防刷与反爬虫：

**实现思路**：

- Redist提供了TTL有效期特性(**设置超时时间**)

- 对于每一个用户,**在 Redisi记录访向次数**：

  例如​ key:188.38.12.33  value:39 超时时间:60s

- **用户每访问1次,对应计数器+1**,超过上限(30)则停止服务

- 如计数器**超过100则认为爬虫攻击**,永久加入黑名单

- 1分钟后key销毁,重新开始计数

**实现过程**：

1. 编写SpringBoot的AOP拦截器,

```java
//AOP拦截器功能:流量防刷
@Component
public class AntiRefreshInterceptor implements HandlerInterceptor {

    @Resource //RedisTemplate,用于筒化 Redis操作,在IOC容器中自动被初始化
    private RedisTemplate<Object,Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.setContentType("text/html;charset=utf-8"); //设置提示信息的字符集
        String clientIp = request.getRemoteAddr();//获取客户端IP
        String userAgent = request.getHeader("User-Agent");//获取客户端浏览器信息
        String client = "anti-refresh:" + DigestUtils.md5Hex(clientIp + "_" + userAgent);//用MD5摘要来标识一个用户

        //若此IP在黑名单中,则直接返回false;
        if(redisTemplate.hasKey("anti-refresh:blackList")){
            if(redisTemplate.opsForSet().isMember("anti-refresh:blackList",client)){
                response.getWriter().println("检测到您的IP访问异常,您已被加入黑名单!");
                return false;
            }
        }
        Integer num = (Integer) redisTemplate.opsForValue().get(client);//记录1分钟内的访问次数
        if(num == null){//第一次访问
            redisTemplate.opsForValue().set(client,1,60, TimeUnit.SECONDS); //放入redis,有效期60S
        }else{
            if(num > 20 && num < 40){
                response.getWriter().println("请求过于频繁,请1分钟后重试!");
                redisTemplate.opsForValue().increment(client,1); //每访问一次redis的值+1;
                return false;
            }else if(num >= 40){
                redisTemplate.opsForSet().add("anti-refresh:blackList",clientIp);
                response.getWriter().println("检测到您的IP访问异常,您已被加入黑名单!");
                System.out.println("IP"+clientIp+"访问异常,已被加入黑名单!");
                return false;
            }else {
                redisTemplate.opsForValue().increment(client,1); //每访问一次redis的值+1;
            }
        }
        return true;
    }
}

```

2. 注入拦截器：写一个@Configuration类即可.可选择需要拦截的页面,如:"/goods"

```java
@Configuration
public class  WebConfig implements WebMvcConfigurer {
    @Resource
    private AntiRefreshInterceptor antiRefreshInterceptor;

    @Override
    //注入拦截器
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(antiRefreshInterceptor).addPathPatterns("/goods");//作用的URL;
    }
}
```

