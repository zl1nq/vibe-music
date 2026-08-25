import { http } from "@/utils/http";
import type { Result } from "@/api/system";
import { getToken } from "@/utils/auth";

/** 获取用户数量 */
export const getAllUsersCount = () => {
  const userData = getToken();
  return http.request<Result>("get", "/admin/getAllUsersCount", {
    headers: {
      "Content-Type": "application/json",
      Authorization: userData.accessToken
    }
  });
};

/** 获取歌手数量 */
export const getAllArtistsCount = (gender?: number, area?: string) => {
  const userData = getToken();
  return http.request<Result>("get", "/admin/getAllArtistsCount", {
    headers: {
      "Content-Type": "application/json",
      Authorization: userData.accessToken
    },
    params: { gender, area }
  });
};

/** 获取歌曲数量 */
export const getAllSongsCount = (style?: string) => {
  const userData = getToken();
  return http.request<Result>("get", "/admin/getAllSongsCount", {
    headers: {
      "Content-Type": "application/json",
      Authorization: userData.accessToken
    },
    params: { style }
  });
};

/** 获取歌单数量 */
export const getAllPlaylistsCount = (style?: string) => {
  const userData = getToken();
  return http.request<Result>("get", "/admin/getAllPlaylistsCount", {
    headers: {
      "Content-Type": "application/json",
      Authorization: userData.accessToken
    },
    params: { style }
  });
};
