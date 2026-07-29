package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;

    @Transactional
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal=new Setmeal();

        BeanUtils.copyProperties(setmealDTO,setmeal);

        setmealMapper.save(setmeal);
        Long d=setmeal.getId();

        List<SetmealDish> list=setmealDTO.getSetmealDishes();

        if(list!=null&&list.size()>0) {

            list.forEach(setmealDish -> {
                setmealDish.setSetmealId(d);
            });

            setmealDishMapper.insertBatch(list);
        }


    }

    @Override
    public SetmealVO getById(Long id) {
        Setmeal setmeal=setmealMapper.getById(id);
        SetmealVO setmealVO=new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);

        List<SetmealDish> dishes=setmealDishMapper.getBySetmealId(id);

        setmealVO.setSetmealDishes(dishes);

        return setmealVO;
    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());


        Page<SetmealVO> setmealVOPage=setmealMapper.pageQuery(setmealPageQueryDTO);

        return new PageResult(setmealVOPage.getTotal(),setmealVOPage.getResult());

    }

    @Transactional
    public void deleteByIds(List<Long> ids) {
        for(Long id:ids){
            Setmeal setmeal = setmealMapper.getById(id);
            if(setmeal!=null&&setmeal.getStatus()== StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        for(Long id:ids){
            setmealMapper.deleteByIds(id);
            setmealDishMapper.deleteBySetmealId(id);
        }

    }

    @Transactional
    public void update(SetmealDTO setmealDTO) {

        Setmeal setmeal=new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.updateSetmealById(setmeal);

        List<SetmealDish> setmealDishes=setmealDTO.getSetmealDishes();

        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());

        if(setmealDishes!=null&&setmealDishes.size()>0){
            setmealDishes.forEach(setmealDish -> {
                setmealDish.setSetmealId(setmealDTO.getId());
            });
            setmealDishMapper.insertBatch(setmealDishes);
        }


    }

    @Override
    public void updateStatus(Integer status,Long id) {
        if(status==StatusConstant.ENABLE){
            List<Dish> dishes=dishMapper.getBySetmealId(id);
            for(Dish d:dishes){
                if(d.getStatus()==StatusConstant.DISABLE){
                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            }
        }

        Setmeal setmeal=new Setmeal();
        setmeal.setStatus(status);
        setmeal.setId(id);

        setmealMapper.updateSetmealById(setmeal);
    }
}
