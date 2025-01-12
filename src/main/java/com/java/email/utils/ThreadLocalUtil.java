package com.example.rv.utils;

/**
 * ThreadLocal 工具类
 */
@SuppressWarnings("all")
public class ThreadLocalUtil {
    private static final LogUtil logUtil = LogUtil.getLogger(ThreadLocalUtil.class);
    //提供thread对象
    private static final ThreadLocal THREAD_LOCAL=new ThreadLocal<>();

    //提供get方法，拿取线程存储的值
    public static <T> T get(){
        try {
            return (T) THREAD_LOCAL.get();
        }catch (Exception e){
            logUtil.error("ThreadLocalUtil get value error : " + e);
            return null;
        }
    }

    //提供set方法，向线程内写入值
    public static void set(Object value){
        try {
            THREAD_LOCAL.set(value);
        }catch (Exception e){
            logUtil.error("ThreadLocalUtil set value error : " + e);
        }
    }

    //使用完毕，移除线程，防止内存泄漏
    public static void remove(){
        try {
            THREAD_LOCAL.remove();
        }catch (Exception e){
            logUtil.error("ThreadLocalUtil remove value error : " + e);
        }
    }


    /*//提供ThreadLocal对象,
    private static final ThreadLocal THREAD_LOCAL = new ThreadLocal();

    //根据键获取值
    public static <T> T get(){
        return (T) THREAD_LOCAL.get();
    }
	
    //存储键值对
    public static void set(Object value){
        THREAD_LOCAL.set(value);
    }

    //清除ThreadLocal 防止内存泄漏
    public static void remove(){
        THREAD_LOCAL.remove();
    }*/
}
