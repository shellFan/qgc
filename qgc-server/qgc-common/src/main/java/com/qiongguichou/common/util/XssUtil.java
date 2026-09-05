package com.qiongguichou.common.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * XSS防护工具类
 */
public class XssUtil {

    private XssUtil() {
    }

    /**
     * 清除HTML标签和危险内容
     */
    public static String clean(String content) {
        if (content == null) {
            return null;
        }
        return Jsoup.clean(content, Safelist.none());
    }

    /**
     * 清除危险HTML，保留基本格式
     */
    public static String cleanBasic(String content) {
        if (content == null) {
            return null;
        }
        return Jsoup.clean(content, Safelist.basic());
    }
}