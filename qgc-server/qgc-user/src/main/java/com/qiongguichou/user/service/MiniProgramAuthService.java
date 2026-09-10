package com.qiongguichou.user.service;

import java.util.Map;

public interface MiniProgramAuthService {
    Map<String, Object> login(String code, String nickname, String avatar);
}
