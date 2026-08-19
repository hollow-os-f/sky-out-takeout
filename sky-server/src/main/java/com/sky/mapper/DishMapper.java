package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);

    Page<DishVO> pageQuery(DishPageQueryDTO dto);

    @Select("select * from dish where id=#{id}")
    Dish getById(Long id);

    @Delete("delete from dish where id=#{id}")
    void deleteById(Long id);

    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    @Select("select * from dish where category_id=#{categoryId}")
    List<Dish> getDishByCategoryId(Long categoryId);

    @Select("select d.* from dish d left join setmeal_dish s on d.id=s.dish_id where s.setmeal_id=#{id}")
    List<Dish> getBySetmealId(Long id);

    List<Dish> list(Dish dish);

    Integer countByMap(Map map);
}
