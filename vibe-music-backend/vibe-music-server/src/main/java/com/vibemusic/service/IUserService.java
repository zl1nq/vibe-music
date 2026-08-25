package com.vibemusic.service;

import com.vibemusic.model.dto.*;
import com.vibemusic.model.entity.User;
import com.vibemusic.model.vo.UserManagementVO;
import com.vibemusic.model.vo.UserVO;
import com.vibemusic.result.PageResult;
import com.vibemusic.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
public interface IUserService extends IService<User> {

    // 发送验证码
    Result sendVerificationCode(String email);

    // 验证验证码
    boolean verifyVerificationCode(String email, String verificationCode);

    // 用户注册
    Result register(UserRegisterDTO userRegisterDTO);

    // 用户登录
    Result login(UserLoginDTO userLoginDTO);

    // 用户信息
    Result<UserVO> userInfo();

    // 更新用户信息
    Result updateUserInfo(UserDTO userDTO);

    // 更新用户头像
    Result updateUserAvatar(String avatarUrl);

    // 更新用户密码
    Result updateUserPassword(UserPasswordDTO userPasswordDTO, String token);

    // 重置用户密码
    Result resetUserPassword(UserResetPasswordDTO userResetPasswordDTO);

    // 退出登录
    Result logout(String token);

    // 注销账号
    Result deleteAccount();

    // 获取所有用户数量
    Result<Long> getAllUsersCount();

    // 获取所有用户
    Result<PageResult<UserManagementVO>> getAllUsers(UserSearchDTO userSearchDTO);

    // 添加用户
    Result addUser(UserAddDTO userAddDTO);

    // 更新用户
    Result updateUser(UserDTO userDTO);

    // 更新用户状态
    Result updateUserStatus(Long userId, Integer userStatus);

    // 删除用户
    Result deleteUser(Long userId);

    // 批量删除用户
    Result deleteUsers(List<Long> userIds);
}
