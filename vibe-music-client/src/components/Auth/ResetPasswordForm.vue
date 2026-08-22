<script setup lang="ts">
import { ref, reactive, onUnmounted } from 'vue'
import { Message, Lock, Key } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { sendEmailCode, resetPassword } from '@/api/system'

const emit = defineEmits(['success', 'switch-tab'])

const loading = ref(false)
const countdown = ref(0)
const resetFormRef = ref<FormInstance>()
let timer: NodeJS.Timeout | null = null

const resetForm = reactive({
  email: '',
  verificationCode: '',
  newPassword: '',
  repeatPassword: '',
})

// 表单验证规则
const resetRules = reactive<FormRules>({
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' },
  ],
  verificationCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    {
      pattern: /^[0-9a-zA-Z]{6}$/,
      message: '验证码格式：6位字符（大小写字母、数字）',
      trigger: 'blur',
    },
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    {
      pattern: /^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z\W]{8,18}$/,
      message: '密码格式：8-18位数字、字母、符号的任意两种组合',
      trigger: 'blur',
    },
  ],
  repeatPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: Function) => {
        if (value !== resetForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
})

// 发送验证码
const handleSendCode = async () => {
  try {
    if (!resetForm.email) {
      ElMessage.warning('请先输入邮箱')
      return
    }
    const response = await sendEmailCode(resetForm.email)
    if (response.code === 0) {
      ElMessage.success('验证码已发送')
      countdown.value = 60
      timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          if (timer) {
            clearInterval(timer)
            timer = null
          }
        }
      }, 1000)
    } else {
      ElMessage.error(response.message)
    }
  } catch (error: any) {
    ElMessage.error(error.message || '发送验证码失败')
  }
}

// 重置密码处理
const handleReset = async () => {
  if (!resetFormRef.value) return
  await resetFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      loading.value = true
      try {
        const response = await resetPassword(resetForm)
        if (response.code === 0) {
          ElMessage.success('密码重置成功，请登录')
          emit('switch-tab', 'login')
        } else {
          ElMessage.error(response.message)
        }
      } catch (error: any) {
        ElMessage.error(error.message || '密码重置失败')
      } finally {
        loading.value = false
      }
    } else {
      console.log('验证失败:', fields)
    }
  })
}

function switchToLogin() {
  // 通知父组件切换到登录标签
  emit('switch-tab', 'login')
}

// 组件卸载时清除定时器
onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>

<template>
  <div class="reset-container">
    <p class="form-subtitle">我们将向您的邮箱发送验证码以重置密码</p>

    <el-form ref="resetFormRef" :model="resetForm" :rules="resetRules" label-width="0" size="large"
      @keyup.enter="handleReset">
      <el-form-item prop="email">
        <el-input v-model="resetForm.email" placeholder="邮箱" :prefix-icon="Message">
          <template #append>
            <el-button :disabled="!!countdown || loading" @click="handleSendCode">
              {{ countdown ? `${countdown}s后重试` : '获取验证码' }}
            </el-button>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item prop="verificationCode" class="mt-6">
        <el-input v-model="resetForm.verificationCode" placeholder="验证码" :prefix-icon="Key" />
      </el-form-item>

      <el-form-item prop="newPassword" class="mt-6">
        <el-input v-model="resetForm.newPassword" type="password" placeholder="新密码" :prefix-icon="Lock" show-password />
      </el-form-item>

      <el-form-item prop="repeatPassword" class="mt-6">
        <el-input v-model="resetForm.repeatPassword" type="password" placeholder="确认密码" :prefix-icon="Lock"
          show-password />
      </el-form-item>

      <el-form-item class="mt-6">
        <el-button class="submit-btn" type="primary" :loading="loading" @click="handleReset">
          重置密码
        </el-button>
      </el-form-item>
    </el-form>

    <p class="login-text">
      记起密码了？
      <a href="#" @click.prevent="switchToLogin">返回登录</a>
    </p>
  </div>
</template>

<style scoped>
.reset-container {
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
  padding: 20px;
}

.form-subtitle {
  color: #666;
  margin-bottom: 24px;
  font-size: 14px;
}

:deep(.el-form-item) {
  margin-bottom: 20px;
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
}

.submit-btn {
  width: 100%;
  border-radius: 8px;
  height: 40px;
  font-size: 16px;
}

.login-text {
  text-align: center;
  margin-top: 16px;
  color: #666;
}

.login-text a {
  color: #2a68fa;
  font-weight: 600;
  text-decoration: none;
}

.login-text a:hover {
  text-decoration: underline;
}
</style>
