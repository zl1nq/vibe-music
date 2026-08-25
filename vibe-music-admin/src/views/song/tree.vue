<script setup lang="ts">
import { ref, watch } from "vue";

import Artist from "@iconify-icons/ri/user-3-fill";

interface Tree {
  artistId: number;
  artistName: string;
  highlight?: boolean;
  children?: Tree[];
}

defineProps({
  treeLoading: Boolean,
  treeData: Array
});

const emit = defineEmits(["tree-select"]);

const treeRef = ref();
const searchValue = ref("");
const highlightMap = ref({});

const filterNode = (value: string, data: any) => {
  if (!value) return true;
  if (!data || typeof data.label !== "string") return false;
  return data.label.toLowerCase().includes(value.toLowerCase());
};

function nodeClick(value) {
  const nodeId = value.value;
  if (!highlightMap.value[nodeId]) {
    highlightMap.value[nodeId] = { artistId: nodeId, highlight: false };
  }

  highlightMap.value[nodeId].highlight = !highlightMap.value[nodeId].highlight;

  Object.keys(highlightMap.value).forEach(key => {
    if (Number(key) !== nodeId) {
      highlightMap.value[key].highlight = false;
    }
  });

  emit("tree-select", {
    artistId: nodeId,
    selected: highlightMap.value[nodeId]?.highlight
  });
}

watch(searchValue, val => {
  treeRef.value!.filter(val);
});
</script>

<template>
  <div
    v-loading="treeLoading"
    class="h-full bg-bg_color overflow-hidden relative"
    :style="{ minHeight: `calc(103.45vh - 141px)` }"
  >
    <div class="flex items-center h-[34px]">
      <el-input
        v-model="searchValue"
        class="ml-2"
        size="small"
        placeholder="请输入歌手"
        clearable
      >
        <template #suffix>
          <el-icon class="el-input__icon">
            <IconifyIconOffline
              v-show="searchValue.length === 0"
              icon="ri:search-line"
            />
          </el-icon>
        </template>
      </el-input>
      <div class="mr-1">&ensp;</div>
    </div>
    <el-divider />
    <el-scrollbar height="calc(93vh - 88px)">
      <el-tree
        ref="treeRef"
        :data="treeData"
        node-key="value"
        size="small"
        default-expand-all
        :expand-on-click-node="false"
        :filter-node-method="filterNode"
        :props="{
          label: 'label',
          children: 'children'
        }"
        @node-click="nodeClick"
      >
        <template #default="{ node }">
          <div
            :class="[
              'rounded',
              'flex',
              'items-center',
              'select-none',
              'hover:text-primary',
              searchValue.trim().length > 0 &&
                node.label.includes(searchValue) &&
                'text-red-500',
              highlightMap[node.data.value]?.highlight
                ? 'dark:text-primary'
                : ''
            ]"
            :style="{
              color: highlightMap[node.data.value]?.highlight
                ? 'var(--el-color-primary)'
                : '',
              background: highlightMap[node.data.value]?.highlight
                ? 'var(--el-color-primary-light-7)'
                : 'transparent'
            }"
          >
            <IconifyIconOffline :icon="Artist" />
            <span class="!w-[120px] !truncate" :title="node.label">
              &ensp;{{ node.label }}
            </span>
          </div>
        </template>
      </el-tree>
    </el-scrollbar>
  </div>
</template>

<style lang="scss" scoped>
:deep(.el-divider) {
  margin: 0;
}

:deep(.el-tree) {
  --el-tree-node-hover-bg-color: transparent;
}
</style>
