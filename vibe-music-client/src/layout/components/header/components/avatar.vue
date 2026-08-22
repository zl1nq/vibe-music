<script setup lang="ts">
import { ref } from 'vue'
import { UserStore } from '@/stores/modules/user'
import AuthTabs from '@/components/Auth/AuthTabs.vue'
import FeedbackDialog from '@/components/Common/FeedbackDialog.vue'
import defaultAvatar from '@/assets/user.jpg'
import { ElMessage } from 'element-plus'
import { logout } from '@/api/system'
import { useRouter } from 'vue-router'

const showLogin = ref(false)
const user = UserStore()
const router = useRouter()
const feedbackDialogRef = ref<InstanceType<typeof FeedbackDialog> | null>(null)

const handleLogout = async () => {
  try {
    const response = await logout()
    if (response.code === 0) {
      user.clearUserInfo()
      ElMessage.success('退出登录成功')
    } else {
      ElMessage.error(response.message || '退出失败')
    }
  } catch (error: any) {
    console.error('退出登录错误:', error)
    ElMessage.error(error.message || '退出失败')
    // 即使请求失败也清除用户信息
    user.clearUserInfo()
  }
}

const openFeedbackDialog = () => {
  feedbackDialogRef.value?.openDialog()
}
</script>

<template>
  <el-dropdown v-if="user.userInfo && user.userInfo.userId" class="cursor-pointer">
    <span class="flex items-center">
      <el-avatar :src="user.userInfo.avatarUrl || defaultAvatar" class="mr-1" shape="circle" :size="32" />
      <span class="text-sm font-medium mr-2 ml-1">{{ user.userInfo.username }}</span>
      <icon-uiw:down />
    </span>

    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item @click="router.push('/user')">
          <icon-mi:user />&ensp;个人中心
        </el-dropdown-item>
        <el-dropdown-item @click="openFeedbackDialog">
          <icon-feather:edit />&ensp;意见反馈
        </el-dropdown-item>
        <el-dropdown-item @click="handleLogout">
          <icon-pajamas:power />&ensp;退出
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
  <el-button class="mr-3 rounded-full" v-else type="primary" @click="showLogin = true">
    <div class="flex items-center gap-1">
      <icon-ic:baseline-person-pin />
      登录
    </div>
  </el-button>
  <AuthTabs v-if="showLogin" v-model="showLogin" />
  <FeedbackDialog ref="feedbackDialogRef" />
</template>
