package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WorkspaceService workspaceService;


    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate beginTime, LocalDate endTime) {
        List<LocalDate> list=new ArrayList<>();
        list.add(beginTime);
        while (!beginTime.equals(endTime)){
            beginTime=beginTime.plusDays(1);
            list.add(beginTime);
        }
        String days= StringUtils.join(list,",");


        List<Double> turnoverList=new ArrayList<>();
        for(LocalDate l:list){
            LocalDateTime b=LocalDateTime.of(l, LocalTime.MIN);
            LocalDateTime e=LocalDateTime.of(l,LocalTime.MAX);

            Map map=new HashMap<>();
            map.put("begin",b);
            map.put("end",e);
            map.put("status", Orders.COMPLETED);
            Double turnover=orderMapper.sumByMap(map);
            turnover=turnover==null?0:turnover;
            turnoverList.add(turnover);

        }

        return TurnoverReportVO.builder()
                .dateList(days)
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }

    @Override
    public UserReportVO getUserReportStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> list=new ArrayList<>();
        list.add(begin);
        while (!begin.equals(end)){
            begin=begin.plusDays(1);
            list.add(begin);
        }

        List<Integer> newUserList=new ArrayList<>();
        List<Integer> totalUserList=new ArrayList<>();

        for (LocalDate l:list){
            LocalDateTime beginTime=LocalDateTime.of(l,LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(l,LocalTime.MAX);

            Map map=new HashMap<>();

            map.put("end",endTime);
            Integer totalUsers=userMapper.countByMap(map);

            map.put("begin",beginTime);
            Integer newUsers=userMapper.countByMap(map);

            newUserList.add(newUsers);
            totalUserList.add(totalUsers);

        }
        return UserReportVO.builder()
                .dateList(StringUtils.join(list,","))
                .newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .build();


    }

    @Override
    public OrderReportVO getOrderReportStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> list=new ArrayList<>();
        list.add(begin);
        while (!begin.equals(end)){
            begin=begin.plusDays(1);
            list.add(begin);
        }

        List<Integer> orderCountList=new ArrayList<>();
        List<Integer> validOrderCountList=new ArrayList<>();

        for(LocalDate l:list){
            LocalDateTime beginTime=LocalDateTime.of(l,LocalTime.MIN);
            LocalDateTime endTime=LocalDateTime.of(l,LocalTime.MAX);

            Integer orderCounts=getOrderCountByMap(beginTime,endTime,null);
            Integer validOrderCounts=getOrderCountByMap(beginTime,endTime,Orders.COMPLETED);

            orderCountList.add(orderCounts);
            validOrderCountList.add(validOrderCounts);
        }

        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        Integer validTotalOrderCount=validOrderCountList.stream().reduce(Integer::sum).get();

        Double p=0.0;
        if(totalOrderCount!=0){
            p=validTotalOrderCount.doubleValue()/totalOrderCount;
        }

        return OrderReportVO.builder()
                .orderCompletionRate(p)
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validTotalOrderCount)
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderCountList,","))
                .dateList(StringUtils.join(list,","))
                .build();

    }

    @Override
    public SalesTop10ReportVO getTop10Report(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime=LocalDateTime.of(begin,LocalTime.MIN);
        LocalDateTime endTime=LocalDateTime.of(end,LocalTime.MAX);

        List<GoodsSalesDTO> top = orderMapper.getTop(beginTime, endTime);

        List<String> nameList = top.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numberList = top.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());


        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(nameList,","))
                .numberList(StringUtils.join(numberList,","))
                .build();
    }

    @Override
    public void exportData(HttpServletResponse response) {
        LocalDate beginTime=LocalDate.now().minusDays(30);
        LocalDate endTime=LocalDate.now().minusDays(1);

        BusinessDataVO data = workspaceService.getBusinessData(LocalDateTime.of(beginTime, LocalTime.MIN), LocalDateTime.of(endTime, LocalTime.MAX));

        InputStream in=getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");


        try {
            XSSFWorkbook excel=new XSSFWorkbook(in);

            XSSFSheet sheet=excel.getSheet("Sheet1");

            sheet.getRow(1).getCell(1).setCellValue("时间:"+beginTime+"至"+endTime);

            sheet.getRow(3).getCell(2).setCellValue(data.getTurnover());

            sheet.getRow(3).getCell(4).setCellValue(data.getOrderCompletionRate());

            sheet.getRow(3).getCell(6).setCellValue(data.getNewUsers());

            sheet.getRow(4).getCell(2).setCellValue(data.getValidOrderCount());

            sheet.getRow(4).getCell(4).setCellValue(data.getUnitPrice());


            for(int i=0;i<30;i++){
                LocalDate m=beginTime.plusDays(i);
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(m, LocalTime.MIN), LocalDateTime.of(m, LocalTime.MAX));

                XSSFRow row= sheet.getRow(7+i);

                row.getCell(1).setCellValue(m.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            ServletOutputStream outputStream=response.getOutputStream();

            excel.write(outputStream);


            in.close();
            excel.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Integer getOrderCountByMap(LocalDateTime begin,LocalDateTime end,Integer status){
        Map map=new HashMap();
        map.put("begin",begin);
        map.put("end",end);
        map.put("status",status);

        return orderMapper.countByMap(map);

    }
}
