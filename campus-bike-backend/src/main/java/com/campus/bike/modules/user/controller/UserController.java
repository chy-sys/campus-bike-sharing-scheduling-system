package com.campus.bike.modules.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.bike.common.R;
import com.campus.bike.common.constant.Constants;
import com.campus.bike.common.exception.BizException;
import com.campus.bike.modules.user.dto.UpdatePasswordRequest;
import com.campus.bike.modules.user.dto.UpdateProfileRequest;
import com.campus.bike.modules.user.entity.User;
import com.campus.bike.modules.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

/**
 * 用户模块：账号管理（管理员）、个人信息维护
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户列表，仅管理员
     */
    @GetMapping
    public R<IPage<User>> page(@RequestParam(defaultValue = "1") long pageNum,
                               @RequestParam(defaultValue = "10") long pageSize,
                               HttpServletRequest request) {
        requireAdmin(request);
        return R.ok(userService.page(pageNum, pageSize));
    }

    /**
     * 审核运维人员资质 / 分配角色，仅管理员
     * role：0普通用户 1运维人员 2管理员
     */
    @PutMapping("/{id}/role")
    public R<Void> updateRole(@PathVariable Long id,
                              @RequestParam Integer role,
                              HttpServletRequest request) {
        requireAdmin(request);
        userService.updateRole(id, role);
        return R.ok("角色更新成功", null);
    }

    /**
     * 当前登录用户资料
     */
    @GetMapping("/profile")
    public R<User> profile(HttpServletRequest request) {
        String phone = String.valueOf(request.getAttribute(Constants.ATTR_PHONE));
        User detail = userService.getUserByPhone(phone);
        if (detail == null) {
            throw new BizException("用户不存在");
        }
        detail.setPassword(null);
        return R.ok(detail);
    }

    /**
     * 修改个人资料
     */
    @PutMapping("/profile")
    public R<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest body,
                                 HttpServletRequest request) {
        userService.updateProfile(currentUserId(request), body.getUsername());
        return R.ok("资料修改成功", null);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public R<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest body,
                                  HttpServletRequest request) {
        userService.updatePassword(currentUserId(request), body.getOldPassword(), body.getNewPassword());
        return R.ok("密码修改成功", null);
    }

    private void requireAdmin(HttpServletRequest request) {
        Integer role = currentRole(request);
        if (role == null || role != Constants.ROLE_ADMIN) {
            throw new BizException("无权限操作，仅管理员可访问");
        }
    }

    private Integer currentRole(HttpServletRequest request) {
        Object role = request.getAttribute(Constants.ATTR_ROLE);
        return role == null ? Constants.ROLE_USER : ((Number) role).intValue();
    }

    private Long currentUserId(HttpServletRequest request) {
        Object userId = request.getAttribute(Constants.ATTR_USER_ID);
        if (userId == null) {
            throw new BizException("登录信息已失效，请重新登录");
        }
        return Long.valueOf(String.valueOf(userId));
    }
}
