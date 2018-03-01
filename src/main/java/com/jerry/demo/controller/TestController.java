package com.jerry.demo.controller;

import com.jerry.demo.service.DemoService;
import com.jerry.mvcframework.annotation.JAutowired;
import com.jerry.mvcframework.annotation.JController;
import com.jerry.mvcframework.annotation.JRequestMapping;
import com.jerry.mvcframework.annotation.JRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Created by Yangjy on 2018/2/27.
 */
@JController
@JRequestMapping("/web")
public class TestController {

    @JAutowired
    private DemoService demoService;

    @JRequestMapping("/test")
    public void test(HttpServletRequest req, HttpServletResponse resp , @JRequestParam String name){

    }

    @JRequestMapping("/test1")
    public void test1(HttpServletRequest req, HttpServletResponse resp ,@JRequestParam String name){

    }

    @JRequestMapping("/test2")
    public void test2(HttpServletRequest req, HttpServletResponse resp ,@JRequestParam String name){

    }


}
