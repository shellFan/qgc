package com.qiongguichou.core.service;

import com.qiongguichou.core.entity.BehaviorLog;
import com.qiongguichou.core.mapper.BehaviorLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户行为日志服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BehaviorLogService {

    private final BehaviorLogMapper behaviorLogMapper;

    /**
     * 记录用户行为
     */
    public void log(Long userId, String event, String targetId, String targetType, String source, HttpServletRequest request) {
        BehaviorLog behaviorLog = new BehaviorLog();
        behaviorLog.setUserId(userId);
        behaviorLog.setEvent(event);
        behaviorLog.setTargetId(targetId);
        behaviorLog.setTargetType(targetType);
        behaviorLog.setSource(source);
        if (request != null) {
            behaviorLog.setIp(getClientIp(request));
            behaviorLog.setUserAgent(request.getHeader("User-Agent"));
        }
        behaviorLogMapper.insert(behaviorLog);
    }

    /**
     * 记录用户行为(无request)
     */
    public void log(Long userId, String event, String targetId, String targetType, String source) {
        log(userId, event, targetId, targetType, source, null);
    }

    /**
     * 记录页面浏览
     */
    public void logPageView(Long userId, String targetId, String targetType, HttpServletRequest request) {
        log(userId, "PAGE_VIEW", targetId, targetType, null, request);
    }

    /**
     * 记录点击事件
     */
    public void logClick(Long userId, String targetId, String targetType, HttpServletRequest request) {
        log(userId, "CLICK", targetId, targetType, null, request);
    }

    /**
     * 记录分享事件
     */
    public void logShare(Long userId, Long campaignId, String source, HttpServletRequest request) {
        log(userId, "SHARE", String.valueOf(campaignId), "CAMPAIGN", source, request);
    }

    /**
     * 记录支付点击
     */
    public void logPayClick(Long userId, Long campaignId, HttpServletRequest request) {
        log(userId, "PAY_CLICK", String.valueOf(campaignId), "CAMPAIGN", null, request);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}