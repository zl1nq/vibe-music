<script setup lang="ts">
import { ref, watch } from "vue";

const props = defineProps({
  songName: String,
  audioUrl: String,
  visible: Boolean
});
const emit = defineEmits(["update:visible"]);

// 根据 visible 控制显示隐藏
const isVisible = ref(props.visible);

watch(
  () => props.visible,
  newVal => {
    isVisible.value = newVal;
  }
);

// 确保关闭时通知父组件更新 visible 状态
const closeDialog = () => {
  emit("update:visible", false);
};
</script>

<template>
  <el-dialog
    v-model="isVisible"
    title="预览音频"
    width="600px"
    @close="closeDialog"
  >
    <div class="text-center">
      <span class="text-lg font-bold">{{ songName }}</span>
    </div>
    <el-divider />
    <audio v-if="audioUrl" :src="audioUrl" controls class="w-full" />
    <div v-else class="text-center">
      <span class="text-gray-500 font-bold">请先上传音频文件</span>
    </div>
  </el-dialog>
</template>

<style scoped></style>
