package com.nowcoder.community.controller.advice;

import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.PrintWriter;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    private static final Logger logger = LoggerFactory.getLogger(Exception.class);

    @ExceptionHandler({Exception.class})
    // 处理所有异常的方法
    public void handleException(Exception e, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // 这三个参数可以解决绝大多数需求
        logger.error("服务器发生异常："+e.getMessage());
        for(StackTraceElement element: e.getStackTrace()){
            // 每个element记录一条异常信息
            logger.error(element.toString());
        }

        /*
            判断普通请求还是异步请求
            如果是普通请求，可以返回页面
            如果是异步请求，需要返回json
        */
        String xRequestedWith = request.getHeader("x-requested-with");
        if("XMLHttpRequest".equals(xRequestedWith)){  // 异步请求
            response.setContentType("application/plain;charset=utf-8");
            PrintWriter writer = response.getWriter();
            writer.write(CommunityUtil.getJSONString(1,"服务器异常！"));
        } else {
            response.sendRedirect(request.getContextPath() + "/error");
        }
    }
}
