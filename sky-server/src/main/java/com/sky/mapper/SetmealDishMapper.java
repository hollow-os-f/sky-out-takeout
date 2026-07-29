package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    List<Long> getSetMealIdsByDishIds(List<Long> ids);

    void insertBatch(List<SetmealDish> list);

    @Select("select * from setmeal_dish where setmeal_id=#{id}")
    List<SetmealDish> getBySetmealId(Long id);

    @Delete("delete from setmeal_dish where setmeal_id=#{id}")
    void deleteBySetmealId(Long id);
}
