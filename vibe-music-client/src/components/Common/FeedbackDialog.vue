<script setup lang="ts">
import { ref, reactive } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { addFeedback } from '@/api/system'

const dialogVisible = ref(false)
const loading = ref(false)
const formRef = ref<FormInstance>()

const formData = reactive({
  content: ''
})

const rules = reactive<FormRules>({
  content: [
    { required: true, message: '请输入反馈内容', trigger: 'blur' },
    { min: 10, message: '反馈内容不能少于 10 个字符', trigger: 'blur' },
    { max: 200, message: '反馈内容不能超过 200 个字符', trigger: 'blur' },
  ]
})

// 打开对话框
const openDialog = () => {
  formData.content = '' // 清空上次输入
  formRef.value?.resetFields() // 重置校验状态
  dialogVisible.value = true
}

// 关闭对话框
const closeDialog = () => {
  dialogVisible.value = false
}

// 提交反馈
const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await addFeedback({ content: formData.content })
        if (res.code === 0) {
          ElMessage.success('反馈提交成功，感谢您的意见！')
          closeDialog()
        } else {
          ElMessage.error(res.message || '提交失败，请稍后再试')
        }
      } catch (error: any) {
        console.error('Feedback submission error:', error)
        ElMessage.error(error.message || '提交反馈时发生错误')
      } finally {
        loading.value = false
      }
    }
  })
}

// 将打开对话框的方法暴露出去
defineExpose({ openDialog })

</script>

<template>
  <el-dialog v-model="dialogVisible" title="意见反馈" width="500px" :close-on-click-modal="false" @close="closeDialog">
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="0px">
      <el-form-item prop="content">
        <el-input v-model="formData.content" type="textarea" :rows="7" placeholder="请输入您的宝贵意见或建议（10-200字）"
          maxlength="200" show-word-limit />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button class="cancel-btn mr-3" @click="closeDialog">取消</el-button>
        <el-button class="submit-btn" type="primary" :loading="loading" @click="handleSubmit">
          提交反馈
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style scoped>
.dialog-footer {
  text-align: right;
}

.cancel-btn {
  width: 15%;
  border-radius: 8px;
  height: 33px;
  font-size: 14px;
}

.submit-btn {
  width: 20%;
  border-radius: 8px;
  height: 33px;
  font-size: 14px;
}

:deep(.el-textarea__inner) {
  margin-top: 6px;
  border-radius: 8px;
  resize: none;
}
</style>