package com.vibemusic.service.impl;


import com.vibemusic.constant.JwtClaimsConstant;
import com.vibemusic.constant.MessageConstant;
import com.vibemusic.enumeration.RoleEnum;
import com.vibemusic.enumeration.UserStatusEnum;
import com.vibemusic.mapper.UserMapper;
import com.vibemusic.model.dto.*;
import com.vibemusic.model.entity.User;
import com.vibemusic.model.vo.UserManagementVO;
import com.vibemusic.model.vo.UserVO;
import com.vibemusic.result.PageResult;
import com.vibemusic.result.Result;
import com.vibemusic.service.EmailService;
import com.vibemusic.service.IUserService;
import com.vibemusic.service.MinioService;
import com.vibemusic.util.JwtUtil;
import com.vibemusic.util.ThreadLocalUtil;
import com.vibemusic.util.TypeConversionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
@Service
@CacheConfig(cacheNames = "userCache")
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private EmailService emailService;
    @Autowired
    private MinioService minioService;

    /**
     * 发送验证码
     *
     * @param email 用户邮箱
     * @return 结果
     */
    @Override
    public Result sendVerificationCode(String email) {
        String verificationCode = emailService.sendVerificationCodeEmail(email);
        if (verificationCode == null) {
            return Result.error(MessageConstant.EMAIL_SEND_FAILED);
        }

        // 将验证码存储到Redis中，设置过期时间为5分钟
        stringRedisTemplate.opsForValue().set("verificationCode:" + email, verificationCode, 5, TimeUnit.MINUTES);
        return Result.success(MessageConstant.EMAIL_SEND_SUCCESS);
    }

    /**
     * 验证验证码
     *
     * @param email            用户邮箱
     * @param verificationCode 验证码
     * @return 验证结果
     */
    @Override
    public boolean verifyVerificationCode(String email, String verificationCode) {
        String storedCode = stringRedisTemplate.opsForValue().get("verificationCode:" + email);
        return storedCode != null && storedCode.equals(verificationCode);
    }

    /**
     * 用户注册
     *
     * @param userRegisterDTO 用户注册信息
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result register(UserRegisterDTO userRegisterDTO) {
        // 删除Redis中的验证码
        stringRedisTemplate.delete("verificationCode:" + userRegisterDTO.getEmail());

        User userByUsername = userMapper.selectOne(new QueryWrapper<User>().eq("username", userRegisterDTO.getUsername()));
        if (userByUsername != null) {
            return Result.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
        }

        User userByEmail = userMapper.selectOne(new QueryWrapper<User>().eq("email", userRegisterDTO.getEmail()));
        if (userByEmail != null) {
            return Result.error(MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
        }

        String passwordMD5 = DigestUtils.md5DigestAsHex(userRegisterDTO.getPassword().getBytes());
        User user = new User();
        user.setUsername(userRegisterDTO.getUsername()).setPassword(passwordMD5).setEmail(userRegisterDTO.getEmail())
                .setCreateTime(LocalDateTime.now()).setUpdateTime(LocalDateTime.now())
                .setUserStatus(UserStatusEnum.ENABLE);

        if (userMapper.insert(user) == 0) {
            return Result.error(MessageConstant.REGISTER + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.REGISTER + MessageConstant.SUCCESS);
    }

    /**
     * 用户登录
     *
     * @param userLoginDTO 用户登录信息
     * @return 结果
     */
    @Override
    public Result login(UserLoginDTO userLoginDTO) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("email", userLoginDTO.getEmail()));
        if (user == null) {
            return Result.error(MessageConstant.EMAIL + MessageConstant.ERROR);
        }
        if (user.getUserStatus() != UserStatusEnum.ENABLE) {
            return Result.error(MessageConstant.ACCOUNT_LOCKED);
        }

        if (DigestUtils.md5DigestAsHex(userLoginDTO.getPassword().getBytes()).equals(user.getPassword())) {
            // 登录成功
            Map<String, Object> claims = new HashMap<>();
            claims.put(JwtClaimsConstant.ROLE, RoleEnum.USER.getRole());
            claims.put(JwtClaimsConstant.USER_ID, user.getUserId());
            claims.put(JwtClaimsConstant.USERNAME, user.getUsername());
            claims.put(JwtClaimsConstant.EMAIL, user.getEmail());
            String token = JwtUtil.generateToken(claims);

            // 将token存入redis
            stringRedisTemplate.opsForValue().set(token, token, 6, TimeUnit.HOURS);

            return Result.success(MessageConstant.LOGIN + MessageConstant.SUCCESS, token);
        }

        return Result.error(MessageConstant.PASSWORD + MessageConstant.ERROR);
    }

    /**
     * 用户信息
     *
     * @return 结果
     */
    @Override
    public Result<UserVO> userInfo() {
        Map<String, Object> map = ThreadLocalUtil.get();
        Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
        Long userId = TypeConversionUtil.toLong(userIdObj);
        User user = userMapper.selectById(userId);
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);

        return Result.success(userVO);
    }

    /**
     * 更新用户信息
     *
     * @param userDTO 用户信息
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result updateUserInfo(UserDTO userDTO) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
        Long userId = TypeConversionUtil.toLong(userIdObj);

        User userByUsername = userMapper.selectOne(new QueryWrapper<User>().eq("username", userDTO.getUsername()));
        if (userByUsername != null && !userByUsername.getUserId().equals(userId)) {
            return Result.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
        }

        User userByPhone = userMapper.selectOne(new QueryWrapper<User>().eq("phone", userDTO.getPhone()));
        if (userByPhone != null && !userByPhone.getUserId().equals(userId)) {
            return Result.error(MessageConstant.PHONE + MessageConstant.ALREADY_EXISTS);
        }

        User userByEmail = userMapper.selectOne(new QueryWrapper<User>().eq("email", userDTO.getEmail()));
        if (userByEmail != null && !userByEmail.getUserId().equals(userId)) {
            return Result.error(MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
        }

        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        user.setUpdateTime(LocalDateTime.now());

        if (userMapper.updateById(user) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 更新用户头像
     *
     * @param avatarUrl 用户头像
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result updateUserAvatar(String avatarUrl) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
        Long userId = TypeConversionUtil.toLong(userIdObj);

        User user = userMapper.selectById(userId);
        String userAvatar = user.getUserAvatar();
        if (userAvatar != null && !userAvatar.isEmpty()) {
            minioService.deleteFile(userAvatar);
        }

        if (userMapper.update(new User().setUserAvatar(avatarUrl).setUpdateTime(LocalDateTime.now()),
                new QueryWrapper<User>().eq("id", userId)) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 更新用户密码
     *
     * @param userPasswordDTO 用户密码信息
     * @param token           认证token
     * @return 结果
     */
    @Override
    public Result updateUserPassword(UserPasswordDTO userPasswordDTO, String token) {
        Map<String, Object> map = ThreadLocalUtil.get();
        Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
        Long userId = TypeConversionUtil.toLong(userIdObj);
        User user = userMapper.selectById(userId);
        if (!user.getPassword().equals(DigestUtils.md5DigestAsHex(userPasswordDTO.getOldPassword().getBytes()))) {
            return Result.error(MessageConstant.OLD_PASSWORD_ERROR);
        }

        if (user.getPassword().equals(DigestUtils.md5DigestAsHex(userPasswordDTO.getNewPassword().getBytes()))) {
            return Result.error(MessageConstant.NEW_PASSWORD_ERROR);
        }

        if (!userPasswordDTO.getRepeatPassword().equals(userPasswordDTO.getNewPassword())) {
            return Result.error(MessageConstant.PASSWORD_NOT_MATCH);
        }

        if (userMapper.update(new User().setPassword(DigestUtils.md5DigestAsHex(userPasswordDTO.getNewPassword().getBytes())).setUpdateTime(LocalDateTime.now()),
                new QueryWrapper<User>().eq("id", userId)) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        // 注销token
        stringRedisTemplate.delete(token);

        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 重置用户密码
     *
     * @param userResetPasswordDTO 用户密码信息
     * @return 结果
     */
    @Override
    public Result resetUserPassword(UserResetPasswordDTO userResetPasswordDTO) {
        // 删除Redis中的验证码
        stringRedisTemplate.delete("verificationCode:" + userResetPasswordDTO.getEmail());

        User user = userMapper.selectOne(new QueryWrapper<User>().eq("email", userResetPasswordDTO.getEmail()));
        if (user == null) {
            return Result.error(MessageConstant.EMAIL + MessageConstant.NOT_EXIST);
        }

        if (!userResetPasswordDTO.getRepeatPassword().equals(userResetPasswordDTO.getNewPassword())) {
            return Result.error(MessageConstant.PASSWORD_NOT_MATCH);
        }

        if (userMapper.update(new User().setPassword(DigestUtils.md5DigestAsHex(userResetPasswordDTO.getNewPassword().getBytes())).setUpdateTime(LocalDateTime.now()),
                new QueryWrapper<User>().eq("id", user.getUserId())) == 0) {
            return Result.error(MessageConstant.PASSWORD + MessageConstant.RESET + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.PASSWORD + MessageConstant.RESET + MessageConstant.SUCCESS);
    }

    /**
     * 登出
     *
     * @param token 认证token
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = {"userCache", "userFavoriteCache", "songCache", "artistCache", "playlistCache"}, allEntries = true)
    public Result logout(String token) {
        // 注销token
        Boolean result = stringRedisTemplate.delete(token);
        if (result != null && result) {
            return Result.success(MessageConstant.LOGOUT + MessageConstant.SUCCESS);
        } else {
            return Result.error(MessageConstant.LOGOUT + MessageConstant.FAILED);
        }
    }

    /**
     * 注销账户
     *
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result deleteAccount() {
        Map<String, Object> map = ThreadLocalUtil.get();
        Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
        Long userId = TypeConversionUtil.toLong(userIdObj);

        // 查询用户信息，获取头像 URL
        User user = userMapper.selectById(userId);
        if (user == null) {
            return Result.error(MessageConstant.USER + MessageConstant.NOT_EXIST);
        }

        // 删除头像
        String userAvatar = user.getUserAvatar();
        if (userAvatar != null && !userAvatar.isEmpty()) {
            minioService.deleteFile(userAvatar);
        }

        // 删除用户
        if (userMapper.deleteById(userId) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * 获取所有用户数量
     *
     * @return 用户数量
     */
    @Override
    public Result<Long> getAllUsersCount() {
        return Result.success(userMapper.selectCount(new QueryWrapper<>()));
    }

    /**
     * 分页查询所有用户
     *
     * @param userSearchDTO 用户查询条件
     * @return 用户分页信息
     */
    @Override
    @Cacheable(key = "#userSearchDTO.pageNum + '-' + #userSearchDTO.pageSize + '-' + #userSearchDTO.username + '-' + #userSearchDTO.phone + '-' + #userSearchDTO.userStatus")
    public Result<PageResult<UserManagementVO>> getAllUsers(UserSearchDTO userSearchDTO) {
        // 分页查询
        Page<User> page = new Page<>(userSearchDTO.getPageNum(), userSearchDTO.getPageSize());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 根据 userSearchDTO 的条件构建查询条件
        if (userSearchDTO.getUsername() != null) {
            queryWrapper.like("username", userSearchDTO.getUsername());
        }
        if (userSearchDTO.getPhone() != null) {
            queryWrapper.like("phone", userSearchDTO.getPhone());
        }
        if (userSearchDTO.getUserStatus() != null) {
            queryWrapper.eq("status", userSearchDTO.getUserStatus().getId());
        }

        // 倒序排序
        queryWrapper.orderByDesc("create_time");

        IPage<User> userPage = userMapper.selectPage(page, queryWrapper);
        if (userPage.getRecords().size() == 0) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }

        // 转换为 UserManagementVO
        List<UserManagementVO> userVOList = userPage.getRecords().stream()
                .map(user -> {
                    UserManagementVO userVO = new UserManagementVO();
                    BeanUtils.copyProperties(user, userVO);
                    return userVO;
                }).toList();

        return Result.success(new PageResult<>(userPage.getTotal(), userVOList));
    }

    /**
     * 添加用户
     *
     * @param userAddDTO 用户信息
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result addUser(UserAddDTO userAddDTO) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", userAddDTO.getUsername())
                .or()
                .eq("phone", userAddDTO.getPhone())
                .or()
                .eq("email", userAddDTO.getEmail());

        List<User> existingUsers = userMapper.selectList(queryWrapper);
        if (existingUsers != null && !existingUsers.isEmpty()) {
            for (User user : existingUsers) {
                if (user.getUsername().equals(userAddDTO.getUsername())) {
                    return Result.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
                }
                if (user.getPhone().equals(userAddDTO.getPhone())) {
                    return Result.error(MessageConstant.PHONE + MessageConstant.ALREADY_EXISTS);
                }
                if (user.getEmail().equals(userAddDTO.getEmail())) {
                    return Result.error(MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
                }
            }
        }

        String passwordMD5 = DigestUtils.md5DigestAsHex(userAddDTO.getPassword().getBytes());
        User user = new User();
        user.setUsername(userAddDTO.getUsername()).setPassword(passwordMD5).setPhone(userAddDTO.getPhone())
                .setEmail(userAddDTO.getEmail()).setIntroduction(userAddDTO.getIntroduction())
                .setCreateTime(LocalDateTime.now()).setUpdateTime(LocalDateTime.now());

        // 前端传递的用户状态（1：启用，0：禁用）需反转
        if (userAddDTO.getUserStatus().getId() == 1) {
            user.setUserStatus(UserStatusEnum.ENABLE);  // 数据库（0：启用）
        } else if (userAddDTO.getUserStatus().getId() == 0) {
            user.setUserStatus(UserStatusEnum.DISABLE);    // 数据库（1：禁用）
        }

        if (userMapper.insert(user) == 0) {
            return Result.error(MessageConstant.ADD + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }

    /**
     * 更新用户
     *
     * @param userDTO 用户信息
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result updateUser(UserDTO userDTO) {
        Long userId = userDTO.getUserId();

        User userByUsername = userMapper.selectOne(new QueryWrapper<User>().eq("username", userDTO.getUsername()));
        if (userByUsername != null && !userByUsername.getUserId().equals(userId)) {
            return Result.error(MessageConstant.USERNAME + MessageConstant.ALREADY_EXISTS);
        }

        User userByPhone = userMapper.selectOne(new QueryWrapper<User>().eq("phone", userDTO.getPhone()));
        if (userByPhone != null && !userByPhone.getUserId().equals(userId)) {
            return Result.error(MessageConstant.PHONE + MessageConstant.ALREADY_EXISTS);
        }

        User userByEmail = userMapper.selectOne(new QueryWrapper<User>().eq("email", userDTO.getEmail()));
        if (userByEmail != null && !userByEmail.getUserId().equals(userId)) {
            return Result.error(MessageConstant.EMAIL + MessageConstant.ALREADY_EXISTS);
        }

        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        user.setUpdateTime(LocalDateTime.now());

        if (userMapper.updateById(user) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 更新用户状态
     *
     * @param userId     用户id
     * @param userStatus 状态
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result updateUserStatus(Long userId, Integer userStatus) {
        // 确保用户状态有效
        UserStatusEnum statusEnum;
        if (userStatus == 0) {
            statusEnum = UserStatusEnum.ENABLE;
        } else if (userStatus == 1) {
            statusEnum = UserStatusEnum.DISABLE;
        } else {
            return Result.error(MessageConstant.USER_STATUS_INVALID);
        }

        // 更新用户状态
        User user = new User();
        user.setUserStatus(statusEnum).setUpdateTime(LocalDateTime.now());

        int rows = userMapper.update(user, new QueryWrapper<User>().eq("id", userId));
        if (rows == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 删除用户
     *
     * @param userId 用户id
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result deleteUser(Long userId) {
        if (userMapper.deleteById(userId) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    /**
     * 批量删除用户
     *
     * @param userIds 用户id数组
     * @return 结果
     */
    @Override
    @CacheEvict(cacheNames = "userCache", allEntries = true)
    public Result deleteUsers(List<Long> userIds) {
        if (userMapper.deleteByIds(userIds) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

}
