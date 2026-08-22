<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UserStore } from '@/stores/modules/user'
import defaultAvatar from '@/assets/user.jpg'
import { updateUserInfo, updateUserAvatar, deleteUser, getUserInfo } from '@/api/system'
import 'vue-cropper/dist/index.css'
import { VueCropper } from "vue-cropper";
import { useRouter } from 'vue-router'
import AuthTabs from '@/components/Auth/AuthTabs.vue'

const router = useRouter()
const userStore = UserStore()
const loading = ref(false)
const userFormRef = ref<FormInstance>()
const cropperVisible = ref(false)
const cropperImg = ref('')
const cropper = ref<any>(null)
const authVisible = ref(false)

const userForm = reactive({
  userId: userStore.userInfo.userId,
  username: userStore.userInfo.username || '',
  phone: userStore.userInfo.phone || '',
  email: userStore.userInfo.email || '',
  introduction: userStore.userInfo.introduction || ''
})

// 表单验证规则
const userRules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_-]{4,16}$/,
      message: '用户名格式：4-16位字符（字母、数字、下划线、连字符）',
      trigger: 'blur',
    },
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  introduction: [
    { max: 100, message: '简介不能超过100个字符', trigger: 'blur' },
  ],
})

// 检查登录状态
onMounted(() => {
  if (!userStore.isLoggedIn) {
    authVisible.value = true
  }
})

// 处理头像上传
const handleAvatarClick = () => {
  const input = document.createElement('input')
  input.type = 'file'
  input.accept = 'image/*'
  input.onchange = (e: Event) => {
    const target = e.target as HTMLInputElement
    const file = target.files?.[0]
    if (file) {
      const reader = new FileReader()
      reader.onload = (e) => {
        const result = e.target?.result
        if (typeof result === 'string') {
          cropperImg.value = result
          cropperVisible.value = true
        }
      }
      reader.readAsDataURL(file)
    }
  }
  input.click()
}

// 重置裁剪
const reset = () => {
  if (cropper.value) {
    cropper.value.refresh()
  }
}

// 缩放
const changeScale = (num: number) => {
  if (cropper.value) {
    cropper.value.changeScale(num)
  }
}

// 向左旋转
const rotateLeft = () => {
  if (cropper.value) {
    cropper.value.rotateLeft()
  }
}

// 向右旋转
const rotateRight = () => {
  if (cropper.value) {
    cropper.value.rotateRight()
  }
}

// 确认裁剪
const handleCropConfirm = async () => {
  if (!cropper.value) return
  cropper.value.getCropData(async (base64: string) => {
    try {
      const response = await fetch(base64)
      const blob = await response.blob()

      const formData = new FormData()
      formData.append('avatar', blob, 'avatar.png')

      const res = await updateUserAvatar(formData)

      if (res.code === 0) {
        // 重新获取用户信息以更新头像URL
        const userInfoResponse = await getUserInfo()
        if (userInfoResponse.code === 0) {
          userStore.setUserInfo(userInfoResponse.data, userStore.userInfo.token)
          ElMessage.success('头像更新成功')
          cropperVisible.value = false
          cropperImg.value = ''
        } else {
          ElMessage.error(userInfoResponse.message || '获取用户信息失败')
        }
      } else {
        ElMessage.error(res.message || '头像更新失败')
      }
    } catch (error: any) {
      console.error('头像更新错误:', error)
      ElMessage.error(error.message || '头像更新失败')
    }
  })
}

// 处理表单提交
const handleSubmit = async () => {
  if (!userFormRef.value) return
  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const response = await updateUserInfo(userForm)
        if (response.code === 0) {
          const userInfoResponse = await getUserInfo()
          userStore.setUserInfo(userInfoResponse.data, userStore.userInfo.token)
          ElMessage.success('更新成功')
        } else {
          ElMessage.error(response.message || '更新失败')
        }
      } catch (error: any) {
        ElMessage.error(error.message || '更新失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 处理账号注销
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
      '注销账号后，所有数据将被清除且无法恢复，是否确认注销？',
      '警告',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    loading.value = true
    const response = await deleteUser()
    if (response.code === 0) {
      userStore.clearUserInfo()
      ElMessage.success('账号已注销')
      router.push('/')
    } else {
      ElMessage.error(response.message || '注销失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '注销失败')
    }
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="user-container">
    <h2 class="username">个人中心</h2>

    <div class="section">
      <div class="section-title">头像</div>
      <div class="user-header">
        <div class="avatar-wrapper" @click="handleAvatarClick">
          <el-avatar :src="userStore.userInfo.avatarUrl || defaultAvatar" :size="100" />
          <div class="avatar-hover">
            <icon-ic:outline-photo-camera class="camera-icon" />
            <span>更新头像</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 头像裁剪弹窗 -->
    <el-dialog v-model="cropperVisible" title="裁剪头像" width="600px" :close-on-click-modal="false"
      :close-on-press-escape="false">
      <div class="cropper-container">
        <vue-cropper ref="cropper" :img="cropperImg" :info="true" :canScale="true" :autoCrop="true" :fixedBox="true"
          :canMove="true" :canMoveBox="true" :centerBox="true" :infoTrue="true" :fixed="true" :fixedNumber="[1, 1]"
          :high="true" mode="cover" :round="true" />
      </div>
      <template #footer>
        <div class="dialog-footer">
          <div class="flex justify-between items-center w-full">
            <div class="flex">
              <el-button size="mini" type="info" @click="reset" class="mr-1">重置</el-button>
              <el-button size="mini" plain @click="changeScale(1)" class="mr-1">
                <icon-ph:magnifying-glass-plus-light class="mr-0.5" />放大
              </el-button>
              <el-button size="mini" plain @click="changeScale(-1)" class="mr-1">
                <icon-ph:magnifying-glass-minus-light class="mr-0.5" />缩小
              </el-button>
              <el-button size="mini" plain @click="rotateLeft" class="mr-1">
                <icon-grommet-icons:rotate-left class="mr-0.5" />左旋转
              </el-button>
              <el-button size="mini" plain @click="rotateRight" class="mr-1">
                <icon-grommet-icons:rotate-right class="mr-0.5" />右旋转
              </el-button>
            </div>
            <div class="flex">
              <el-button size="mini" type="warning" plain @click="cropperVisible = false" class="mr-1">取消</el-button>
              <el-button size="mini" type="primary" @click="handleCropConfirm">确认</el-button>
            </div>
          </div>
        </div>
      </template>
    </el-dialog>

    <el-form ref="userFormRef" :model="userForm" :rules="userRules" label-width="0" size="large" class="user-form">
      <div class="section">
        <div class="section-title">用户名</div>
        <el-form-item prop="username">
          <el-input v-model="userForm.username" placeholder="用户名" />
        </el-form-item>
      </div>

      <div class="section">
        <div class="section-title">邮箱</div>
        <el-form-item prop="email">
          <el-input v-model="userForm.email" placeholder="邮箱" />
        </el-form-item>
      </div>

      <div class="section">
        <div class="section-title">联系电话</div>
        <el-form-item prop="phone">
          <el-input v-model="userForm.phone" placeholder="联系电话" />
        </el-form-item>
      </div>

      <div class="section">
        <div class="section-title">简介</div>
        <el-form-item prop="introduction">
          <el-input v-model="userForm.introduction" type="textarea" :rows="4" placeholder="编辑个人简介" maxlength="100"
            show-word-limit />
        </el-form-item>
      </div>

      <el-form-item class="button-group">
        <div class="flex justify-between w-full">
          <el-button type="primary" :loading="loading" @click="handleSubmit" class="submit-btn">
            更新信息
          </el-button>
          <el-button type="danger" :loading="loading" @click="handleDelete" class="submit-btn">
            注销账号
          </el-button>
        </div>
      </el-form-item>
    </el-form>

    <!-- 登录对话框 -->
    <AuthTabs v-model="authVisible" />
  </div>
</template>

<style scoped>
.user-container {
  max-width: 1000px;
  margin: 30px auto;
  padding: 30px 40px 15px;
  background-color: var(--el-bg-color);
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.user-header {
  text-align: left;
  margin-bottom: 20px;
  display: flex;
}

.username {
  margin: 0 0 20px 0;
  font-size: 20px;
  color: var(--el-text-color-primary);
  font-weight: normal;
}

.user-form {
  max-width: 100%;
  margin: 0;
}

:deep(.el-form-item) {
  margin-bottom: 24px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  background-color: var(--el-fill-color-blank);
  box-shadow: 0 0 0 1px var(--el-border-color) inset !important;
}

:deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px var(--el-border-color-hover) inset !important;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset !important;
}

.submit-btn {
  border-radius: 8px;
  width: 140px;
}

:deep(.el-textarea__inner) {
  border-radius: 8px;
  resize: none;
  background-color: var(--el-fill-color-blank);
  box-shadow: 0 0 0 1px var(--el-border-color) inset !important;
}

:deep(.el-textarea__inner:hover) {
  box-shadow: 0 0 0 1px var(--el-border-color-hover) inset !important;
}

:deep(.el-textarea__inner:focus) {
  box-shadow: 0 0 0 1px var(--el-color-primary) inset !important;
}

:deep(.el-input.is-disabled .el-input__wrapper) {
  background-color: var(--el-fill-color-blank);
  box-shadow: 0 0 0 1px var(--el-border-color-light) inset !important;
  cursor: not-allowed;
}

.section {
  margin-bottom: 24px;
}

.section-title {
  margin-bottom: 8px;
  color: var(--el-text-color-regular);
  font-size: 14px;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
}

.avatar-hover {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s;
  color: white;
  font-size: 14px;
}

.avatar-hover .camera-icon {
  font-size: 24px;
  margin-bottom: 4px;
}

.avatar-wrapper:hover .avatar-hover {
  opacity: 1;
}

.button-group {
  margin-top: 40px;
}

.cropper-container {
  width: 100%;
  height: 400px;
}

:deep(.el-dialog__body) {
  padding-top: 10px;
}
</style>
