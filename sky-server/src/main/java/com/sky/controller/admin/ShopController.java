package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/shop")
@Api("商铺相关接口")
@Slf4j
public class ShopController {
    private static final String SHOP_STATUS="SHOP_STATUS";

    @Autowired
    private RedisTemplate redisTemplate;

    @PutMapping("/{status}")
    @ApiOperation("设置商铺状态")
    public Result setStatus(@PathVariable Integer status){
        log.info("我开始设置了:{}",status);
        redisTemplate.opsForValue().set(SHOP_STATUS,status);
        return Result.success();
    }

    @GetMapping("/status")
    @ApiOperation("获取商铺状态")
    public Result<Integer> getStatus(){
        log.info("我要获取状态!!!");
        Integer status= (Integer) redisTemplate.opsForValue().get(SHOP_STATUS);
        return Result.success(status);
    }
}
