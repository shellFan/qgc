package com.qiongguichou.common.util;

/**
 * 用户上下文(线程本地)
 */
public class UserContext {

    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<String> OPENID = new ThreadLocal<>();
    private static final ThreadLocal<String> IP = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void setOpenid(String openid) {
        OPENID.set(openid);
    }

    public static String getOpenid() {
        return OPENID.get();
    }

    public static void setIp(String ip) {
        IP.set(ip);
    }

    public static String getIp() {
        return IP.get();
    }

    public static void clear() {
        USER_ID.remove();
        OPENID.remove();
        IP.remove();
    }
}