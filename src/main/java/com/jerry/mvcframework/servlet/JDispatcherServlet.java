package com.jerry.mvcframework.servlet;

import com.jerry.mvcframework.annotation.JAutowired;
import com.jerry.mvcframework.annotation.JController;
import com.jerry.mvcframework.annotation.JRequestMapping;
import com.jerry.mvcframework.annotation.JService;
import lombok.Data;
import lombok.ToString;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Created by Yangjy on 2018/2/27.
 */
@Data
public class JDispatcherServlet extends HttpServlet {

    private Properties properties = new Properties();
    private List<String> classNames = new ArrayList<String>();
    private Map<String,Object> ioc = new HashMap<String, Object>();
    private Map<String,Handler> handlerMapping = new HashMap<String, Handler>();

    @Data
    @ToString
    private class Handler {
        protected Object controller;
        protected Method method;
        protected Pattern pattern;
        protected Map<String,Integer> paramIndexMapping;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {

        //1.加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //2.扫描所有的相关的类
        doScanner(properties.getProperty("scanPackage"));

        //3.初始化所有相关的类Class的实例,并且将其保存到IOC容器
        doInstance();

        //4.自动化的依赖注入
        doAutowired();

        //5.初始化HandlerMapping
        initHandlerMapping();

        System.out.println("Jerry MVC Framework is init");

    }



    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Object o = doDispatch(req, resp);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    private Object doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException {

        String url = req.getRequestURI();

        String contextPath = req.getContextPath();

        url = url.replace(contextPath,"").replaceAll("/+","/");

        if(!handlerMapping.containsKey(url)){
            resp.getWriter().print("404 NOT FOUND!!!");
            return null;
        }

        Handler handler = handlerMapping.get(url);

        System.out.println("获得对应的方法" + url );

        Object invoke = handler.getMethod().invoke(handler.getController(), null);

        return invoke;

    }

    private void doLoadConfig(String configLocation){
        //获取一个inputStream
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(configLocation);

        //加载配置文件
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(null != inputStream) {

                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    private void doScanner(String packageName){
        URL url = this.getClass().getClassLoader().getResource("/" + packageName.replaceAll("\\.", "/"));


        File classDir = new File(url.getFile());

        for (File file : classDir.listFiles()) {

            if(file.isDirectory()){
                doScanner(packageName + "." + file.getName());
            } else {
                classNames.add(packageName + "." + file.getName().replace(".class",""));
            }
        }



    }

    private void doInstance(){

        if(classNames.isEmpty()){
            return;
        }

        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                //进行实例化
                //判断,是否有Controller等注解
                if(clazz.isAnnotationPresent(JController.class)){

                    String beanName = lowerFirst(clazz.getSimpleName());
                    ioc.put(beanName,clazz.newInstance());


                }else if (clazz.isAnnotationPresent(JService.class)){
                    JService jService = clazz.getAnnotation(JService.class);
                    String beanName = jService.value();
                    //1.默认采用类名首字母小写 beanId
                    //2.如果自定义名字,优先使用自己定义的名字
                    if("".equals(beanName.trim())){
                        beanName = lowerFirst(clazz.getSimpleName());
                    }

                    Object instance = clazz.newInstance();
                    ioc.put(beanName,instance);
                    //3.根据类型匹配,利用实现类的接口名字作为key,实现类的类做为value
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> i : interfaces) {
                        ioc.put(lowerFirst(i.getSimpleName()),instance);
                    }


                }else {
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private String lowerFirst(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private void doAutowired(){
        if(ioc.isEmpty()){
            return;
        }
        for (Map.Entry<String,Object> entry : ioc.entrySet()) {

            //在Spring中,没有隐私
            Field[] fields = entry.getValue().getClass().getDeclaredFields();


            for (Field field : fields) {
                //找autowired
                if(!field.isAnnotationPresent(JAutowired.class)){
                    continue;
                }

                JAutowired jAutowired = field.getAnnotation(JAutowired.class);
                String beanName = jAutowired.value().trim();

                if("".equals(beanName)){
                    beanName = lowerFirst(field.getType().getSimpleName());
                }
                //暴力反射
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(),ioc.get(beanName));
                    System.out.println(entry.getValue()+" is autowired ,object is "+ioc.get(beanName));
                } catch (Exception e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }

    private void initHandlerMapping() {

        if(ioc.isEmpty()){
            return;
        }

        for (Map.Entry<String,Object> entry : ioc.entrySet()) {

            Class<?> clazz = entry.getValue().getClass();
            //HandlerMapping 只认JController
            if(!clazz.isAnnotationPresent(JController.class)){
                continue;
            }

            String url = "";
            if(clazz.isAnnotationPresent(JRequestMapping.class)){
                JRequestMapping jRequestMapping = clazz.getAnnotation(JRequestMapping.class);
                url = jRequestMapping.value().trim();
            }

            Method[] methods = clazz.getMethods();

            for (Method method : methods) {
                //如果没有JRequestMapping ,直接跳过
                if(!method.isAnnotationPresent(JRequestMapping.class)){
                    continue;
                }

                JRequestMapping jRequestMapping = method.getAnnotation(JRequestMapping.class);
                String murl = url + jRequestMapping.value().trim();

                Handler handler = new Handler();
                handler.setController(entry.getValue());
                handler.setMethod(method);



                handlerMapping.put(murl,handler);

                System.out.println("Mapping : "+ murl + "  " +handler);

            }


        }

    }

}
