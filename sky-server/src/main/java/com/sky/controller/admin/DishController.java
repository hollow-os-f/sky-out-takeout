package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@Api(tags = "菜品有关接口")
@RestController
@RequestMapping("/admin/dish")
public class DishController {

    @Autowired
    private DishService dishService;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     *
     * @param dto
     * @return
     */
    @PostMapping
    @ApiOperation(value = "新增菜品接口")
    public Result save(@RequestBody DishDTO dto) {
        log.info("新增菜品:{}", dto);
        redisTemplate.delete("dish_"+dto.getCategoryId());
        dishService.saveDishWithFlavour(dto);
        return Result.success();
    }

    @GetMapping("/page")
    @ApiOperation(value = "分页查询接口")
    public Result<PageResult> pageQuery(DishPageQueryDTO dto) {
        log.info("分页查询:{}", dto);
        PageResult result = dishService.pageQuery(dto);
        return Result.success(result);
    }

    @DeleteMapping
    @ApiOperation("删除菜品")
    public Result delete(@RequestParam List<Long> ids) {
        dishService.deleteBatch(ids);
        Set caIds=redisTemplate.keys("dish_**");
        redisTemplate.delete(caIds);
        return Result.success();
    }

    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("查询菜品数据:{}", id);
        return Result.success(dishService.getByIdWithFlavor(id));
    }

    @PutMapping
    @ApiOperation("修改菜品")
    public Result update(@RequestBody DishDTO dto) {
        log.info("修改菜品:{}", dto);
        redisTemplate.delete("dish_"+dto.getCategoryId());
        dishService.updateDishWithFlavour(dto);
        return Result.success();
    }

    @GetMapping("/list")
    @ApiOperation(value = "根据类型查询分类")
    public Result<List<Dish>> list(@RequestParam Long categoryId){
        List<Dish> dishes=dishService.getDishByCategoryId(categoryId);
        return Result.success(dishes);
    }

    @PostMapping("/status/{status}")
    @ApiOperation(value = "停售启售")
    public Result updateStatus(@PathVariable Integer status,Long id){
        log.info("修改状态:{}",status);
        dishService.updateStatus(status,id);
        Set ids=redisTemplate.keys("dish_**");
        redisTemplate.delete(ids);
        return Result.success();
    }

}
