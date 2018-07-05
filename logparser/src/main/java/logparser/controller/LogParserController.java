package logparser.controller;

import com.google.gson.Gson;
import logparser.service.LogParserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import sun.misc.IOUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;

@RestController
public class LogParserController {
    @Autowired
    LogParserService logParserService;

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "**", method ={RequestMethod.GET})
    public void processLogItem(HttpServletRequest request){
        System.out.println("Hello, Angus! This is GET request!");
        System.out.println("=======GET Process=======");

        Map<String,String[]> requestMsg = request.getParameterMap();
        Enumeration<String> requestHeader = request.getHeaderNames();

        System.out.println("------- header -------");
        while(requestHeader.hasMoreElements()){
            String headerKey=requestHeader.nextElement().toString();
            //打印所有Header值

            System.out.println("headerKey="+headerKey+";value="+request.getHeader(headerKey));
        }

        System.out.println("------- parameter -------");
        for(String key :requestMsg.keySet())
        {
            for(int i=0;i<requestMsg.get(key).length;i++)
            {
                //打印所有请求参数值

                System.out.println("key="+key+";value="+requestMsg.get(key)[i].toString());
            }
        }
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "**", method ={RequestMethod.POST})
    public void processLogItem2(HttpServletRequest request){
        System.out.println("Hello, Angus! This is POST Request!");
        System.out.println("=======POST Process=======");


        Map<String,String[]> requestMsg = request.getParameterMap();
        Enumeration<String> requestHeader = request.getHeaderNames();
        InputStream io = null;
        String body;
        System.out.println("------- body -------");
        try{
            io = request.getInputStream();
            byte[] bytes = new byte[io.available()];
            io.read(bytes);
            body = new String(bytes);
            //打印BODY内容
            System.out.println("Request Body="+body);
        }catch(IOException e){
            e.printStackTrace();
        }

        System.out.println("------- header -------");
        while(requestHeader.hasMoreElements()){
            String headerKey=requestHeader.nextElement().toString();
            //打印所有Header值

            System.out.println("headerKey="+headerKey+";value="+request.getHeader(headerKey));
        }

        System.out.println("------- parameters -------");
        for(String key :requestMsg.keySet())
        {
            for(int i=0;i<requestMsg.get(key).length;i++)
            {
                //打印所有请求参数值
                System.out.println("key="+key+";value="+requestMsg.get(key)[i].toString());
            }
        }
    }
}
