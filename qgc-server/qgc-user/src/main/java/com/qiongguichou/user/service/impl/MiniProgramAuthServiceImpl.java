package com.qiongguichou.user.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiongguichou.common.exception.BusinessException;
import com.qiongguichou.common.result.ErrorCode;
import com.qiongguichou.core.config.JwtUtil;
import com.qiongguichou.user.entity.User;
import com.qiongguichou.user.service.MiniProgramAuthService;
import com.qiongguichou.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class MiniProgramAuthServiceImpl implements MiniProgramAuthService {
    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${qgc.wechat.mp.appId:}")
    private String appId;
    @Value("${qgc.wechat.mp.secret:}")
    private String appSecret;

    public MiniProgramAuthServiceImpl(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Map<String, Object> login(String code, String nickname, String avatar) {
        if (code == null || code.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "微信登录code不能为空");
        }
        if (isBlank(appId) || isBlank(appSecret) || appSecret.startsWith("mock_") || appSecret.startsWith("your-")) {
            throw new BusinessException(ErrorCode.WECHAT_AUTH_FAIL, "小程序登录配置未完成");
        }
        JsonNode session = code2Session(code.trim());
        String openid = text(session, "openid");
        if (isBlank(openid)) throw new BusinessException(ErrorCode.WECHAT_AUTH_FAIL, "微信登录凭证无效");

        User user = userService.getByOpenid(openid);
        if (user == null) {
            user = new User();
            user.setOpenid(openid);
            user.setUnionid(text(session, "unionid"));
            user.setNickname(normalize(nickname));
            user.setAvatar(normalize(avatar));
            user.setStatus(1);
            user.setSubscribe(0);
            user.setSex(0);
        } else {
            if (!isBlank(text(session, "unionid"))) user.setUnionid(text(session, "unionid"));
            if (!isBlank(normalize(nickname))) user.setNickname(normalize(nickname));
            if (!isBlank(normalize(avatar))) user.setAvatar(normalize(avatar));
        }
        user = userService.createOrUpdate(user);
        userService.updateLastLoginTime(user.getId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", jwtUtil.generateUserToken(user.getId(), openid));
        result.put("userId", user.getId());
        result.put("openid", openid);
        result.put("nickname", user.getNickname());
        result.put("avatar", user.getAvatar());
        return result;
    }

    private JsonNode code2Session(String code) {
        HttpURLConnection connection = null;
        try {
            String query = "appid=" + URLEncoder.encode(appId, "UTF-8")
                    + "&secret=" + URLEncoder.encode(appSecret, "UTF-8")
                    + "&js_code=" + URLEncoder.encode(code, "UTF-8")
                    + "&grant_type=authorization_code";
            connection = (HttpURLConnection) new URL("https://api.weixin.qq.com/sns/jscode2session?" + query).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(8000);
            InputStream stream = connection.getResponseCode() < 300 ? connection.getInputStream() : connection.getErrorStream();
            JsonNode result = objectMapper.readTree(read(stream));
            if (result.has("errcode") && result.get("errcode").asInt() != 0) {
                log.warn("小程序code2Session失败: errcode={}, errmsg={}", result.get("errcode").asInt(), text(result, "errmsg"));
                throw new BusinessException(ErrorCode.WECHAT_AUTH_FAIL, "微信登录失败");
            }
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("小程序code2Session异常", e);
            throw new BusinessException(ErrorCode.WECHAT_AUTH_FAIL, "微信登录服务暂不可用");
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private static String read(InputStream stream) throws Exception {
        if (stream == null) return "{}";
        StringBuilder body = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) body.append(line);
        }
        return body.toString();
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node == null ? null : node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private static String normalize(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
