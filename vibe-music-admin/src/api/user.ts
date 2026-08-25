import { http } from "@/utils/http";

// export type UserResult = {
//   success: boolean;
//   data: {
//     /** 头像 */
//     avatar: string;
//     /** 用户名 */
//     username: string;
//     /** 昵称 */
//     nickname: string;
//     /** 当前登录用户的角色 */
//     roles: Array<string>;
//     /** 按钮级别权限 */
//     permissions: Array<string>;
//     /** `token` */
//     accessToken: string;
//     /** 用于调用刷新`accessToken`的接口时所需的`token` */
//     refreshToken: string;
//     /** `accessToken`的过期时间（格式'xxxx/xx/xx xx:xx:xx'） */
//     expires: Date;
//   };
// };

export type LoginResult = {
  code: number;
  message: string;
  data: string | null;
};

export type RefreshTokenResult = {
  success: boolean;
  data: {
    /** `token` */
    accessToken: string;
    /** 用于调用刷新`accessToken`的接口时所需的`token` */
    refreshToken: string;
    /** `accessToken`的过期时间（格式'xxxx/xx/xx xx:xx:xx'） */
    expires: Date;
  };
};

/** 登录 */
// export const getLogin = (data?: object) => {
//   return http.request<LoginResult>("post", "/admin/login", { data });
// };

import { jwtDecode } from "jwt-decode";
import { setToken, type DataInfo, getToken } from "@/utils/auth";

export const getLogin = async (data?: object) => {
  const response = await http.request<LoginResult>("post", "/admin/login", {
    data
  });

  if (response.data) {
    const decodedToken: any = jwtDecode(response.data); // 解码 JWT
    const { role, username } = decodedToken.claims; // 提取用户信息
    const expires = new Date(decodedToken.exp * 1000); // 将时间戳转换为 Date 对象

    // 构建新的数据结构
    const userData: DataInfo<Date> = {
      accessToken: response.data, // 将 JWT 字符串作为 accessToken
      expires,
      refreshToken: "", // 后端没有返回 refreshToken，这里设置为空字符串
      roles: [role], // 将 role 转换为数组
      username
    };

    setToken(userData); // 调用 setToken 函数
  }

  return response;
};

/** 登出 */
export const getLogout = () => {
  const userData = getToken(); // 获取 token 数据
  return http.request("post", "/admin/logout", {
    headers: { Authorization: userData.accessToken } // 设置请求头
  });
};

/** 刷新`token` */
// export const refreshTokenApi = (data?: object) => {
//   return http.request<RefreshTokenResult>("post", "/refresh-token", { data });
// };
