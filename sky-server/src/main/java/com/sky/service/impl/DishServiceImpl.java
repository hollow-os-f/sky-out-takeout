package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavourMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavourMapper dishFlavourMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品
     * @param dto
     */
    @Transactional
    public void saveDishWithFlavour(DishDTO dto) {
        Dish dish=new Dish();

        BeanUtils.copyProperties(dto,dish);

        dishMapper.insert(dish);
        Long id=dish.getId();

        List<DishFlavor> dishFlavors=dto.getFlavors();
        if(dishFlavors!=null&&dishFlavors.size()!=0){
            dishFlavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(id);
            });
            dishFlavourMapper.insertBatch(dishFlavors);
        }



    }

    /**
     * 分页查询
     * @param dto
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dto) {
        PageHelper.startPage(dto.getPage(),dto.getPageSize());
        Page<DishVO> page=dishMapper.pageQuery(dto);

        return new PageResult(page.getTotal(),page.getResult());
    }

    /**
     * 删除菜品
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        for(Long id:ids){
             Dish dish= dishMapper.getById(id);
            if(dish.getStatus().equals(StatusConstant.ENABLE)){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        List<Long> dishIds=setmealDishMapper.getSetMealIdsByDishIds(ids);
        if(dishIds!=null && dishIds.size()>0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        for(Long id:ids){
            dishMapper.deleteById(id);
            dishFlavourMapper.deleteByDishId(id);
        }



    }

    @Override
    public DishVO getByIdWithFlavor(Long id) {
        Dish dish=dishMapper.getById(id);

        List<DishFlavor> flavors=dishFlavourMapper.getFlavorsByDishId(id);

        DishVO dishVO=new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(flavors);

        return dishVO;
    }

    /**
     * 更新菜品和对应的口味
     * @param dto
     */
    @Transactional
    public void updateDishWithFlavour(DishDTO dto) {
        Dish dish=new Dish();
        BeanUtils.copyProperties(dto,dish);
        dishMapper.update(dish);

        dishFlavourMapper.deleteByDishId(dto.getId());
        List<DishFlavor> flavors=dto.getFlavors();
        if(flavors!=null&&flavors.size()!=0){
            flavors.forEach(flavor -> {
                flavor.setDishId(dish.getId());
            });
            dishFlavourMapper.insertBatch(flavors);
        }

    }

    @Override
    public List<Dish> getDishByCategoryId(Long categoryId) {
        List<Dish> dishes= dishMapper.getDishByCategoryId(categoryId);

        return dishes;
    }

    @Override
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishes=dishMapper.list(dish);

        List<DishVO> dishVOS=new ArrayList<>();

        for (Dish d:dishes){
            DishVO dish1= new DishVO();
            BeanUtils.copyProperties(d,dish1);

            List<DishFlavor>dishFlavors=dishFlavourMapper.getFlavorsByDishId(d.getId());

            dish1.setFlavors(dishFlavors);
            dishVOS.add(dish1);
        }
        return dishVOS;
    }

    @Override
    public void updateStatus(Integer status,Long id) {
        Dish dish=dishMapper.getById(id);
        dish.setStatus(status);
        dishMapper.update(dish);
    }
}
