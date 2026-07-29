package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController("userShopController")
@RequestMapping("/user/shop")
@Api("用户端获取商铺信息相关接口")
@Slf4j
public class ShopController {
    private static final String SHOP_STATUS="SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;


    @GetMapping("/status")
    @ApiOperation("获取商铺状态")
    public Result<Integer> getStatus(){
        log.info("我要获取状态!!!");
        Integer status= (Integer) redisTemplate.opsForValue().get(SHOP_STATUS);
        return Result.success(status);
    }
}
