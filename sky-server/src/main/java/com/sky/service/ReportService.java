package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {
    TurnoverReportVO getTurnoverStatistics(LocalDate beginTime, LocalDate endTime);

    UserReportVO getUserReportStatistics(LocalDate begin, LocalDate end);

    OrderReportVO getOrderReportStatistics(LocalDate begin, LocalDate end);

    SalesTop10ReportVO getTop10Report(LocalDate begin, LocalDate end);

    void exportData(HttpServletResponse response);
}
