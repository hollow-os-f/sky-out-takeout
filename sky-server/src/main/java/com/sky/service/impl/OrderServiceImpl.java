package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Override
    @Transactional
    public OrderSubmitVO submit(OrdersSubmitDTO submitDTO) {
        //处理异常情况
        Long addressBookId=submitDTO.getAddressBookId();
        AddressBook addressBook=addressBookMapper.getById(addressBookId);
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }
        Long userId= BaseContext.getCurrentId();
        ShoppingCart shoppingCart=new ShoppingCart();
        shoppingCart.setUserId(userId);
        List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
        if(list==null||list.size()<=0){
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }
        //插入order表
        Orders orders=new Orders();
        BeanUtils.copyProperties(submitDTO,orders);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPayStatus(Orders.UN_PAID);
        orders.setOrderTime(LocalDateTime.now());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(userId);

        orderMapper.insert(orders);
        //向order_detail插入n个数据

        List<OrderDetail> orderDetails=new ArrayList<>();
        for(ShoppingCart cart:list){
            OrderDetail orderDetail=new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetails.add(orderDetail);
        }

        orderDetailMapper.insertBatch(orderDetails);


        //清空当前用户购物车
        shoppingCartMapper.deleteByUserId(userId);

        OrderSubmitVO orderSubmitVO=OrderSubmitVO.builder()
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .orderTime(orders.getOrderTime())
                .id(orders.getId())
                .build();
        return orderSubmitVO;
    }

    @Override
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        /*Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));*/

        // —— 模拟支付：跳过真实微信支付调用 ——
        // 直接构造支付参数返回给前端（前端拿到后可直接当作支付完成处理）
        OrderPaymentVO vo = OrderPaymentVO.builder()
                .nonceStr("simulated_nonce")
                .paySign("simulated_sign")
                .timeStamp(String.valueOf(System.currentTimeMillis() / 1000))
                .signType("RSA")
                .packageStr("prepay_id=simulated")
                .build();

        // 模拟支付成功，直接推进订单状态（真实模式下由微信回调驱动，不在这里调）
        paySuccess(ordersPaymentDTO.getOrderNumber());


        return vo;
    }

    @Override
    public void paySuccess(String outTradeNo) {
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());

        Page<Orders> page=orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> list=getOderVos(page);

        return new  PageResult(page.getTotal(),list);
    }

    @Override
    public void cancelOrder(OrdersCancelDTO ordersCancelDTO) {
        Orders orders=orderMapper.getById(ordersCancelDTO.getId());
        Integer payStatus=orders.getPayStatus();
        if(payStatus==1){

            //调用wechatUtil的refund方法来退款
            log.info("退款");
        }
        Orders orders1=new Orders();
        orders1.setId(orders.getId());
        orders1.setStatus(Orders.CANCELLED);
        orders1.setCancelReason(ordersCancelDTO.getCancelReason());
        orders1.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);

    }

    @Override
    public OrderStatisticsVO getStatistics() {
        Integer toBeConfirmedNum=orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmedNum=orderMapper.countStatus(Orders.CONFIRMED);
        Integer delibeyInProGress=orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);

        OrderStatisticsVO orderStatisticsVO=new OrderStatisticsVO();
        orderStatisticsVO.setConfirmed(confirmedNum);
        orderStatisticsVO.setDeliveryInProgress(delibeyInProGress);
        orderStatisticsVO.setToBeConfirmed(toBeConfirmedNum);
        return orderStatisticsVO;
    }

    @Override
    public OrderVO detail(Long id) {
        Orders orders=orderMapper.getById(id);
        OrderVO orderVO=new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        List<OrderDetail> list=orderDetailMapper.getByOrderId(id);
        String orderDish=getOrderDishesStr(orders);
        orderVO.setOrderDishes(orderDish);
        orderVO.setOrderDetailList(list);

        return orderVO;

    }

    @Override
    public PageResult historyQuery(int page, int pageSize, Integer status) {
        PageHelper.startPage(page,pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO=new OrdersPageQueryDTO();
        ordersPageQueryDTO.setStatus(status);
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());

        Page<Orders> page1=orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> list=new ArrayList<>();

        if(page1!=null&&page1.size()>0){
            for(Orders o:page1){
                OrderVO orderVO=new OrderVO();
                BeanUtils.copyProperties(o,orderVO);
                List<OrderDetail> orderDetails=orderDetailMapper.getByOrderId(o.getId());
                orderVO.setOrderDetailList(orderDetails);
                list.add(orderVO);
            }
        }

        return new PageResult(page1.getTotal(),list);
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders=Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }

    @Override
    public void reject(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders=orderMapper.getById(ordersRejectionDTO.getId());
        Integer status=orders.getStatus();

        if (orders == null || !orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        if(status==Orders.PAID){
            log.info("退款");
        }

        Orders orders1=new Orders();
        orders1.setId(orders.getId());
        orders1.setStatus(Orders.CANCELLED);
        orders1.setRejectionReason(ordersRejectionDTO.getRejectionReason());
        orders1.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    @Override
    public void delivery(Long id) {
        Orders orders=orderMapper.getById(id);
        if (orders!=null||orders.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders1=new Orders();
        orders1.setId(id);
        orders1.setStatus(Orders.DELIVERY_IN_PROGRESS);
        orderMapper.update(orders1);

    }

    @Override
    public void complete(Long id) {
        Orders orders=orderMapper.getById(id);
        if(orders==null||!orders.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }
        Orders orders1=new Orders();
        orders1.setId(id);
        orders1.setStatus(Orders.COMPLETED);
        orderMapper.update(orders1);

    }


    private List<OrderVO> getOderVos(Page<Orders> page) {
        List<Orders> list=page.getResult();

        List<OrderVO> orderVOList=new ArrayList<>();

        for(Orders o:list){
            OrderVO orderVO=new OrderVO();
            BeanUtils.copyProperties(o,orderVO);
            String str=getOrderDishesStr(o);

            orderVO.setOrderDishes(str);
            orderVOList.add(orderVO);

        }
        return orderVOList;
    }

    public String getOrderDishesStr(Orders orders){
        List<OrderDetail> orderDetails=orderDetailMapper.getByOrderId(orders.getId());

        List<String> orderDishList=orderDetails.stream().map(x->{
            String orderDish=x.getName()+"*"+x.getNumber()+";";
            return orderDish;
        }).collect(Collectors.toList());

        return String.join("",orderDishList);
    }

}
