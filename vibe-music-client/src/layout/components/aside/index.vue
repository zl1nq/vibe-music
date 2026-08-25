<script setup lang="ts">
import { MenuData } from './data'
import { Icon } from '@iconify/vue'
import { useRoute, useRouter } from 'vue-router'
import { ref, watch } from 'vue'
import { UserStore } from '@/stores/modules/user'
import { ElMessage } from 'element-plus'
import AuthTabs from '@/components/Auth/AuthTabs.vue'
import { useFavoriteStore } from '@/stores/modules/favorite'

const route = useRoute()
const router = useRouter()
const user = UserStore()
const favoriteStore = useFavoriteStore()
const authVisible = ref(false)

// 处理需要登录的路由
const handleProtectedRoute = (path: string) => {
  if (!user.isLoggedIn && (path === '/like' || path === '/user')) {
    ElMessage.warning('请先登录')
    authVisible.value = true
    return false
  }
  return true
}

// 监听用户登录状态
watch(
  () => user.isLoggedIn,
  (newVal) => {
    if (newVal) {
      favoriteStore.getFavoritePlaylists()
    } else {
      favoriteStore.clearFavoritePlaylists()
    }
  },
  { immediate: true }
)
</script>

<template>
  <aside
    class="w-64 hidden h-full overflow-hidden md:block border-r border-border animate-fade-in-left [animation-delay:1.25s]"
  >
    <nav
      class="menu-nav flex flex-col p-4 space-y-5 flex-1 h-full box-border overflow-hidden"
    >
      <div
        v-for="(item, index) in MenuData"
        :key="index"
        class="menu-section w-full flex flex-col gap-1"
      >
        <h3
          class="menu-section-title ml-4 text-xs font-semibold tracking-widest text-muted-foreground"
        >
          {{ item.title }}
        </h3>
        <div
          v-for="(item2, index2) in item.children"
          :key="index2"
          class="menu-item mx-2 rounded-lg transition-all text-base duration-300 py-2 px-3 flex items-center gap-2.5 text-primary-foreground cursor-pointer"
          :class="{
            'is-active bg-activeMenuBg': route.path === item2.router,
            'hover:bg-hoverMenuBg': route.path !== item2.router,
          }"
          @click="
            handleProtectedRoute(item2.router) && router.push(item2.router)
          "
        >
          <Icon :icon="item2.icon" class="text-lg" />
          <span>{{ item2.title }}</span>
        </div>
      </div>

      <!-- 收藏的歌单 -->
      <div
        class="menu-section w-full flex flex-col gap-1"
        v-if="user.isLoggedIn"
      >
        <h3
          class="menu-section-title ml-4 text-xs font-semibold tracking-widest text-muted-foreground"
        >
          收藏的歌单（{{ favoriteStore.favoritePlaylists.length }}）
        </h3>
        <el-scrollbar>
          <el-skeleton
            :loading="favoriteStore.loading"
            animated
            :count="3"
            v-if="favoriteStore.loading"
          >
            <template #template>
              <div class="flex items-center space-x-2 p-2 mx-2">
                <el-skeleton-item
                  variant="image"
                  style="width: 28px; height: 28px"
                />
                <el-skeleton-item variant="text" style="width: 130px" />
              </div>
            </template>
          </el-skeleton>

          <template v-else>
            <div
              v-for="item in favoriteStore.favoritePlaylists"
              :key="item.id"
              class="mx-2 my-1 rounded-lg transition text-sm duration-300 py-2 px-2 flex items-center gap-2 text-primary-foreground cursor-pointer"
              :class="{
                'bg-activeMenuBg': route.path === `/playlist/${item.id}`,
                'hover:bg-hoverMenuBg': route.path !== `/playlist/${item.id}`,
              }"
              @click="router.push(`/playlist/${item.id}`)"
            >
              <el-image
                lazy
                :src="item.coverImgUrl + '?param=50y50'"
                class="w-10 h-10 rounded-md flex-shrink-0"
                :alt="item.name"
              />
              <div class="flex-1 min-w-0">
                <span class="line-clamp-2 text-sm leading-normal">{{
                  item.name
                }}</span>
              </div>
            </div>
          </template>
        </el-scrollbar>
      </div>
    </nav>

    <!-- 登录对话框 -->
    <AuthTabs v-model="authVisible" />
  </aside>
</template>

<style scoped lang="scss">
.menu-nav {
  counter-reset: menu-section;
}

.menu-section {
  counter-increment: menu-section;
}

// 刊物式编号 + 英文小字标注
.menu-section-title::before {
  content: counter(menu-section, decimal-leading-zero);
  margin-right: 6px;
  font-family: Georgia, 'Times New Roman', serif;
  font-size: 10px;
  color: var(--primary);
}

.menu-section-title::after {
  margin-left: 8px;
  font-size: 9px;
  letter-spacing: 0.18em;
  color: hsl(var(--muted-foreground) / 0.6);
}

.menu-section:nth-of-type(1) .menu-section-title::after {
  content: 'RECOMMEND';
}

.menu-section:nth-of-type(2) .menu-section-title::after {
  content: 'EXPLORE';
}

.menu-section:nth-of-type(3) .menu-section-title::after {
  content: 'MINE';
}

.menu-section:nth-of-type(4) .menu-section-title::after {
  content: 'FAVORITES';
}

.menu-item {
  position: relative;
}

.menu-item.is-active {
  color: var(--primary);
  font-weight: 600;
}

// 选中项左侧珊瑚指示条
.menu-item.is-active::before {
  content: '';
  position: absolute;
  left: -2px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 18px;
  border-radius: 3px;
  background: var(--primary);
}
</style>
