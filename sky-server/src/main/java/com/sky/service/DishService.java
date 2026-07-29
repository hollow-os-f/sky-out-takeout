package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {
    /**
     * 新增菜品
     * @param dto
     */
    void saveDishWithFlavour(DishDTO dto);

    /**
     * 分页查询
     * @param dto
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dto);

    /**
     * 删除菜品
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    DishVO getByIdWithFlavor(Long id);

    void updateDishWithFlavour(DishDTO dto);

    List<Dish> getDishByCategoryId(Long categoryId);
}
