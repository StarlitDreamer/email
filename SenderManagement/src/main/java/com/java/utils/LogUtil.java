package com.java.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUtil {
    private final Logger logger;
    private LogUtil(Logger logger){
        this.logger = logger;
    }
    public static LogUtil getLogger(Class<?> clazz){
        return new LogUtil(LoggerFactory.getLogger(clazz));
    }

    public void info(String msg,Object... args){
        logger.info(msg,args);
    }
    public void warn(String msg,Object... args){
        logger.warn(msg,args);
    }
    public void error(String msg,Object... args){
        logger.error(msg,args);
    }
    public void error(String msg,Throwable throwable){
        logger.error(msg,throwable);
    }
}