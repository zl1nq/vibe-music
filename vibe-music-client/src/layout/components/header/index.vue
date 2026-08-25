<script setup lang="ts">
import { Icon } from '@iconify/vue'
import Avatar from './components/avatar.vue'
const route = useRoute()
const router = useRouter()
const currentIcon = ref('material-symbols:wb-sunny-outline-rounded')
const theme = themeStore()
import { useDark, useToggle } from '@vueuse/core'

const searchText = ref('')

const isDark = useDark({
  selector: 'html',
  attribute: 'class',
  valueDark: 'dark',
  valueLight: 'light',
})
const toggleDark = useToggle(isDark)

const toggleMode = () => {
  theme.setDark(!isDark.value)
  toggleDark()
}

// 初始化时根据 store 设置图标
watch(
  () => theme.isDark,
  (newValue) => {
    currentIcon.value = newValue
      ? 'mdi:weather-night'
      : 'material-symbols:wb-sunny-outline-rounded'
  },
  { immediate: true }
)

// 赋值到搜索框
watch(
  () => route.query,
  (newValue) => {
    if (newValue.query) {
      searchText.value = newValue.query as string
    }
  },
  { immediate: true }
)
</script>
<template>
  <header class="px-5 py-2.5 border-b border-border flex items-center">
    <button
      class="flex relative w-60 items-center group"
      @click="router.push('/')"
    >
      <img
        src="\logo.svg"
        alt="logo"
        class="w-9 h-9 ml-2 transition-transform duration-500 ease-spring group-hover:rotate-12"
      />
      <span
        class="ml-2.5 text-xl font-bold font-serif-display tracking-tight flex justify-center items-center"
        >Vibe Music</span
      >
    </button>
    <!-- 输入框和头像 -->
    <div class="flex items-center gap-3">
      <div class="relative mr-6">
        <Icon
          icon="mdi:magnify"
          class="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground text-lg pointer-events-none"
        />
        <input
          v-model="searchText"
          type="text"
          class="mt-0.5 w-64 text-sm pl-9 pr-3 py-2 rounded-xl bg-white border border-border shadow-paper focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all duration-300 ease-out-soft focus:w-80 placeholder:text-muted-foreground"
          placeholder="搜索..."
          @keyup.enter="router.push('/library?query=' + searchText)"
        />
      </div>
      <button
        class="p-2 rounded-full text-inactive hover:text-primary hover:bg-hoverMenuBg transition-all duration-300 ease-spring hover:rotate-12"
        @click="toggleMode"
      >
        <Icon class="text-xl" :icon="currentIcon" />
      </button>
    </div>
    <div class="ml-auto flex items-center gap-3"><Avatar /></div>
  </header>
</template>
