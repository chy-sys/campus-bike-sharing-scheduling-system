package com.campus.bike.modules.user.controller;

import com.campus.bike.common.R;
import com.campus.bike.common.constant.Constants;
import com.campus.bike.modules.user.dto.LoginRequest;
import com.campus.bike.modules.user.dto.LoginResponse;
import com.campus.bike.modules.user.dto.LoginResponse;
import com.campus.bike.modules.user.dto.RegisterRequest;
import com.campus.bike.modules.user.dto.WeChatBindRequest;
import com.campus.bike.modules.user.dto.WeChatLoginRequest;
import com.campus.bike.modules.user.entity.User;
import com.campus.bike.modules.user.service.UserService;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /** 微信小程序凭证，未配置时微信登录不可用 */
    @Value("${wx.appid:}")
    private String wxAppid;

    @Value("${wx.secret:}")
    private String wxSecret;

    @PostMapping("/register")
    public R<Void> register(@Valid @RequestBody RegisterRequest request) {
        // 开发阶段：验证码固定为 "123456"
        if (!"123456".equals(request.getVerifyCode())) {
            return R.fail("验证码错误");
        }
        boolean success = userService.register(request.getPhone(), request.getPassword(), request.getUsername());
        return success ? R.ok("注册成功", null) : R.fail("手机号已存在");
    }

    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        String token = userService.login(request.getPhone(), request.getPassword());
        if (token == null) {
            return R.fail(401, "手机号或密码错误");
        }
        User user = userService.getUserByPhone(request.getPhone());
        Integer role = user.getRole() == null ? Constants.ROLE_USER : user.getRole();
        LoginResponse data = new LoginResponse(token, user.getId(), user.getUsername(), user.getPhone(), role);
        return R.ok("登录成功", data);
    }

    /**
     * 微信小程序登录：用 wx.login 的 code 换取 openid
     * 已绑定账号直接返回 token；未绑定返回 openid，前端引导绑定手机号
     */
    @PostMapping("/wechat/login")
    public R<Object> wechatLogin(@Valid @RequestBody WeChatLoginRequest request) {
        if (!StringUtils.hasText(wxAppid) || !StringUtils.hasText(wxSecret)) {
            return R.fail("未配置微信凭证，请在 application.yml 中配置 wx.appid 与 wx.secret");
        }

        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + wxAppid
                + "&secret=" + wxSecret + "&js_code=" + request.getCode()
                + "&grant_type=authorization_code";

        JSONObject result;
        try {
            result = JSONUtil.parseObj(HttpUtil.get(url, 5000));
        } catch (Exception e) {
            return R.fail("微信接口调用失败：" + e.getMessage());
        }

        String openid = result.getStr("openid");
        if (!StringUtils.hasText(openid)) {
            return R.fail("微信登录失败：" + result.getStr("errmsg", "code 无效或已过期"));
        }

        User user = userService.getUserByOpenid(openid);
        if (user == null) {
            Map<String, Object> data = new HashMap<>();
            data.put("needBind", true);
            data.put("openid", openid);
            return R.ok("该微信未绑定账号，请先绑定手机号", data);
        }

        String token = userService.loginByOpenid(openid);
        Integer role = user.getRole() == null ? Constants.ROLE_USER : user.getRole();
        LoginResponse data = new LoginResponse(token, user.getId(), user.getUsername(), user.getPhone(), role);
        return R.ok("登录成功", data);
    }

    /**
     * 绑定微信：校验手机号密码后关联 openid，之后可免密登录
     */
    @PostMapping("/wechat/bind")
    public R<LoginResponse> wechatBind(@Valid @RequestBody WeChatBindRequest request) {
        String token = userService.login(request.getPhone(), request.getPassword());
        if (token == null) {
            return R.fail(401, "手机号或密码错误");
        }
        userService.bindOpenid(request.getOpenid(), request.getPhone());

        User user = userService.getUserByPhone(request.getPhone());
        Integer role = user.getRole() == null ? Constants.ROLE_USER : user.getRole();
        LoginResponse data = new LoginResponse(token, user.getId(), user.getUsername(), user.getPhone(), role);
        return R.ok("绑定成功", data);
    }
}
