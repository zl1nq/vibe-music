import { defineStore } from 'pinia'
import piniaPersistConfig from '@/stores/helper/persist'
import { UserState } from '@/stores/interface'
import { login, logout, getUserInfo } from '@/api/system'
import { AudioStore } from './audio'

interface UserInfo {
  userId?: number
  username?: string
  phone?: string
  email?: string
  avatarUrl?: string
  introduction?: string
  token?: string
}

/**
 * 用户信息
 */
export const UserStore = defineStore('UserStore', {
  state: (): UserState => ({
    userInfo: {} as UserInfo,
    isLoggedIn: false,
  }),
  actions: {
    // 设置用户信息
    setUserInfo(userInfo: any, token?: string) {
      this.userInfo = {
        userId: userInfo.userId,
        username: userInfo.username,
        phone: userInfo.phone,
        email: userInfo.email,
        avatarUrl: userInfo.userAvatar,
        introduction: userInfo.introduction,
        token: token
      }
      this.isLoggedIn = true
    },
    // 更新头像
    updateUserAvatar(avatarUrl: string) {
      if (this.userInfo) {
        this.userInfo.avatarUrl = avatarUrl
      }
    },
    // 清除用户信息
    clearUserInfo() {
      this.userInfo = {}
      this.isLoggedIn = false

      // 清空所有歌曲的喜欢状态
      const audioStore = AudioStore()
      // 清空播放列表中的喜欢状态
      audioStore.trackList.forEach(track => {
        track.likeStatus = 0
      })
      // 清空当前页面歌曲列表中的喜欢状态
      if (audioStore.currentPageSongs) {
        audioStore.currentPageSongs.forEach(song => {
          song.likeStatus = 0
        })
      }
    },
    // 用户登录
    async userLogin(loginData: { email: string; password: string }) {
      try {
        const response = await login(loginData)

        if (response.code === 0) {
          // 先保存token
          const token = response.data
          
          // 设置token到userInfo
          this.userInfo = { token }

          try {
            // 再获取用户信息
            const userInfoResponse = await getUserInfo()
            
            if (userInfoResponse.code === 0) {
              this.setUserInfo(userInfoResponse.data, token)
              return { success: true, message: '登录成功' }
            }
            return { success: false, message: userInfoResponse.message || '获取用户信息失败' }
          } catch (error: any) {
            return { success: false, message: error.message || '获取用户信息失败' }
          }
        }
        return { success: false, message: response.message || '登录失败' }
      } catch (error: any) {
        return { success: false, message: error.message || '登录失败' }
      }
    },
    // 用户退出
    async userLogout() {
      try {
        const response = await logout()
        if (response.code === 0) {
          this.clearUserInfo()
          return { success: true, message: '退出成功' }
        }
        return { success: false, message: response.message }
      } catch (error: any) {
        return { success: false, message: error.message || '退出失败' }
      }
    }
  },
  persist: piniaPersistConfig('UserStore'),
})
