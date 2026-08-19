package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

@RestController
@Slf4j
@Api(tags = "报表的接口")
@RequestMapping("/admin/report")
public class ReportController {
    @Autowired
    private ReportService reportService;


    @GetMapping("/turnoverStatistics")
    @ApiOperation(value = "统计营业额")
    public Result<TurnoverReportVO> turnoversStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ){
        log.info("我要统计营业额了");
        return Result.success(reportService.getTurnoverStatistics(begin,end));
    }


    @GetMapping("/userStatistics")
    @ApiOperation(value = "用户统计")
    public Result<UserReportVO> getUserReportStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ){
        log.info("我要查有多少用户");
        return Result.success(reportService.getUserReportStatistics(begin,end));
    }

    @GetMapping("/ordersStatistics")
    @ApiOperation("订单统计")
    public Result<OrderReportVO> orderReportStatistics(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ){
        log.info("订单统计");
        return Result.success(reportService.getOrderReportStatistics(begin,end));
    }

    @GetMapping("/top10")
    @ApiOperation("菜品的top10")
    public Result<SalesTop10ReportVO> getTop10Report(
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate begin,
            @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end
    ){
        log.info("我要看看我的哪个菜最好吃");
        return Result.success(reportService.getTop10Report(begin,end));
    }

    @GetMapping("/export")
    @ApiOperation("导出运营数据")
    public Result export(HttpServletResponse response){
        log.info("导出运营数据");
        reportService.exportData(response);
        return Result.success();
    }
}
