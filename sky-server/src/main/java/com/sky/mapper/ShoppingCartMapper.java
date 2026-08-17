package com.sky.mapper;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {
    List<ShoppingCart> list(ShoppingCart shoppingCart);

    @Update("update shopping_cart set number=#{number} where id=#{id}")
    void update(ShoppingCart shoppingCart1);

    @Insert("insert into shopping_cart (name,image,user_id,setmeal_id,dish_id,dish_flavor,number,amount,create_time)"+
    "values(#{name},#{image},#{userId},#{setmealId},#{dishId},#{dishFlavor},#{number},#{amount},#{createTime})")
    void insert(ShoppingCart shoppingCart);

    @Delete("delete from shopping_cart where user_id=#{userId}")
    void deleteByUserId(Long userId);

    @Delete("delete from shopping_cart where id=#{id}")
    void deleteById(Long id);

    void insertBatch(List<ShoppingCart> shoppingCarts);
}
