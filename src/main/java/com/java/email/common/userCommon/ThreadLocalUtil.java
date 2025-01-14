package com.java.email.common.userCommon;

import com.java.email.esdao.repository.user.UserRepository;
import com.java.email.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * ThreadLocal 工具类
 */
@SuppressWarnings("all")
@Component
public class ThreadLocalUtil {
    private static UserRepository userRepository;
    
    private static final LogUtil logUtil = LogUtil.getLogger(ThreadLocalUtil.class);
    private static final ThreadLocal<Object> THREAD_LOCAL = new ThreadLocal<>();

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        ThreadLocalUtil.userRepository = userRepository;
    }

    //提供get方法，拿取线程存储的值
    public static <T> T get(){
        try {
            return (T) THREAD_LOCAL.get();
        }catch (Exception e){
            logUtil.error("ThreadLocalUtil get value error : " + e);
            return null;
        }
    }

    //getUserId，拿取线程存储的UserId
    public static String getUserId() {
        try {
            Map<String, Object> userMap = get();
            if (userMap == null || !userMap.containsKey("id")) {
                logUtil.error("ThreadLocalUtil getUserId error: userMap is null or id not found");
                return null;
            }
            String userId = (String) userMap.get("id");
            if (userId == null || userId.isEmpty()) {
                logUtil.error("ThreadLocalUtil getUserId error: userId is null or empty");
                return null;
            }
            return userId;
        } catch (Exception e) {
            logUtil.error("ThreadLocalUtil getUserId error: " + e);
            return null;
        }
    }

     // 获取用户角色
     public static Integer getUserRole() {
        try {
            Map<String, Object> userMap = get();
            if (userMap == null || !userMap.containsKey("role")) {
                logUtil.error("ThreadLocalUtil getUserRole error: userMap is null or role not found");
                return null;
            }
            return (Integer) userMap.get("role");
        } catch (Exception e) {
            logUtil.error("ThreadLocalUtil getUserRole error: " + e);
            return null;
        }
    }

    // 获取用户名
    public static String getUserName() {
        try {
            Map<String, Object> userMap = get();
            if (userMap == null || !userMap.containsKey("name")) {
                logUtil.error("ThreadLocalUtil getUserName error: userMap is null or name not found");
                return null;
            }
            return (String) userMap.get("name");
        } catch (Exception e) {
            logUtil.error("ThreadLocalUtil getUserName error: " + e);
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

    


}
