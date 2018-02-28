package com.jerry.mvcframework.servlet;

import lombok.Data;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Yangjy on 2018/2/27.
 */
@Data
public class JDispatcherServlet extends HttpServlet {

    private String contextConfigLocation;

    @Override
    public void init(ServletConfig config) throws ServletException {
        //初始化
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }


}
