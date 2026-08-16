package com.sky.task;

import com.sky.constant.MessageConstant;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;


    @Scheduled(cron = "0 * * * * ?")
    public void processTimeoutOrder(){
        log.info("处理超时订单");
        LocalDateTime time=LocalDateTime.now().plusMinutes(-15);

        List<Orders> timeLT = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);

        if(timeLT!=null&&timeLT.size()>0){
            for(Orders o:timeLT){
                o.setStatus(Orders.CANCELLED);
                o.setCancelReason("太久没付钱了，给你退了，气不气");
                o.setCancelTime(LocalDateTime.now());
                orderMapper.update(o);
            }
        }

    }

    @Scheduled(cron = "0 0 1 * * ?")
    //@Scheduled(cron = "0/5 * * * * *")
    public void processDeliveryOrder(){
        log.info("凌晨1点来处理一下昨天的派送订单");
        LocalDateTime time=LocalDateTime.now().plusMinutes(-60);

        List<Orders> timeLT = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);

        if(timeLT!=null&&timeLT.size()>0){
            for(Orders o:timeLT){
                o.setStatus(Orders.COMPLETED);
                orderMapper.update(o);
            }
        }

    }
}
