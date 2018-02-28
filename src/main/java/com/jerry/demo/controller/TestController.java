package com.jerry.demo.controller;

import com.jerry.demo.service.DemoService;
import com.jerry.mvcframework.annotation.JAutowired;
import com.jerry.mvcframework.annotation.JController;
import com.jerry.mvcframework.annotation.JRequestMapping;
import com.jerry.mvcframework.annotation.JRequestParam;
import com.sun.deploy.net.HttpRequest;
import com.sun.deploy.net.HttpResponse;

/**
 * Created by Yangjy on 2018/2/27.
 */
@JController
@JRequestMapping("/web")
public class TestController {

    @JAutowired
    private DemoService demoService;

    @JRequestMapping("/test")
    public void test(HttpRequest req, HttpResponse resp ,@JRequestParam String name){

    }


}
