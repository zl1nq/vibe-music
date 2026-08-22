<script setup lang="ts">
import {
  getAllPlaylists,
  getFavoritePlaylists,
} from '@/api/system'
import coverImg from '@/assets/cover.png'
import { ElNotification } from 'element-plus'

// 路由
const router = useRouter()
// 播放列表
const playlists = ref([])

const selected = ref('all')
// 搜索关键词
const searchKeyword = ref('')

// 歌单类型列表
const playlistsList = [
  { name: '精选歌单', value: 'all' },
  { name: '我的收藏', value: 'favorite' }
]
// 歌单tag
const playTags = ref<{ name: string }[]>([])
const selectedTag = ref('全部')

// 分页组件状态
const currentPage = ref(1) // 当前页
const pageSize = ref(12) // 每页显示的数量
const state = reactive({
  size: 'default',
  disabled: false,
  background: false,
  layout: 'total, sizes, prev, pager, next, jumper',
  total: 0,
  pageSizes: [12, 24, 36, 48],
})

// 监听分页变化
const handleSizeChange = () => {
  getPlaylists()
}
// 监听当前页变化
const handleCurrentChange = () => {
  getPlaylists()
}

// 获取歌单
const getPlaylists = async () => {
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      title: searchKeyword.value || null,
      style: selectedTag.value === '全部' ? null : selectedTag.value
    }

    let res
    if (selected.value === 'favorite') {
      res = await getFavoritePlaylists(params)
    } else {
      res = await getAllPlaylists(params)
    }

    if (res.code === 0) {
      playlists.value = res.data.items.map(item => ({
        id: item.playlistId,
        name: item.title,
        coverImgUrl: item.coverUrl ?? coverImg,
        creator: {
          nickname: selected.value === 'favorite' ? 'Vibe Music' : 'Vibe Music',
          avatarUrl: coverImg
        },
        playCount: 0,
        subscribedCount: 0
      }))
      state.total = res.data.total
    } else {
      ElNotification({
        type: 'error',
        message: '获取歌单列表失败',
        duration: 2000,
      })
    }
  } catch (error) {
    ElNotification({
      type: 'error',
      message: '获取歌单列表失败',
      duration: 2000,
    })
  }
}

// 选择歌单
const selectPlaylist = (playlist: string) => {
  selected.value = playlist
  getPlaylists()
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1 // 重置页码
  getPlaylists()
}

// 处理搜索框按下回车
const handleKeyPress = (e: KeyboardEvent) => {
  if (e.key === 'Enter') {
    handleSearch()
  }
}

onMounted(() => {
  // 初始化歌单标签
  playTags.value = [
    { name: '全部' },
    { name: '节奏布鲁斯' },
    { name: '欧美流行' },
    { name: '华语流行' },
    { name: '粤语流行' },
    { name: '国风流行' },
    { name: '韩语流行' },
    { name: '日本流行' },
    { name: '嘻哈说唱' },
    { name: '非洲节拍' },
    { name: '原声带' },
    { name: '轻音乐' },
    { name: '摇滚' },
    { name: '朋克' },
    { name: '电子' },
    { name: '国风' },
    { name: '乡村' },
    { name: '古典' },
  ]
  getPlaylists()
})
</script>
<template>
  <div class="flex flex-col h-full flex-1 overflow-hidden bg-background px-4 py-2">
    <div class="py-4">
      <div class="flex flex-col sm:flex-row gap-4">
        <div class="relative flex-grow">
          <icon-mdi:magnify
            class="lucide lucide-search absolute left-2 top-1/2 transform -translate-y-1/2 text-muted-foreground" />
          <input v-model="searchKeyword" @keydown="handleKeyPress"
            class="flex h-10 rounded-lg border border-input transform duration-300 bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium file:text-foreground placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-0 pl-10 w-72"
            placeholder="搜索歌单..." type="search" />
        </div>
        <el-select class="w-48" v-model="selectedTag" @change="getPlaylists">
          <el-option v-for="item in playTags" :key="item.name" :label="item.name" :value="item.name" />
        </el-select>
      </div>
    </div>
    <div class="flex-grow flex flex-col overflow-x-hidden cursor-pointer">
      <div class="border-b pb-1">
        <div
          class="inline-flex h-10 items-center rounded-lg bg-muted/70 p-1 text-muted-foreground w-full justify-start mb-2 overflow-x-auto">
          <button v-for="playlist in playlistsList" :key="playlist.value" @click="selectPlaylist(playlist.value)"
            :class="{
              'bg-activeMenuBg text-foreground shadow-sm':
                selected === playlist.value,
            }"
            class="inline-flex items-center justify-center whitespace-nowrap rounded-sm px-3 py-1.5 text-sm font-medium transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50">
            {{ playlist.name }}
          </button>
        </div>
      </div>
      <div class="flex-1 overflow-x-hidden my-2">
        <div class="grid grid-cols-[repeat(auto-fill,minmax(200px,1fr))] gap-6">
          <div v-for="playlist in playlists" :key="playlist.id" @click="router.push('/playlist/' + playlist.id)"
            class="rounded-lg hover:bg-background transition duration-300 border bg-card text-card-foreground shadow-sm overflow-hidden hover:shadow-lg">
            <div class="flex flex-col space-y-1.5 p-0">
              <div class="relative">
                <el-image lazy :alt="playlist.name" class="w-full aspect-square object-cover"
                  :src="playlist.coverImgUrl + '?param=330y330'" />
                <button
                  class="inline-flex items-center justify-center gap-2 whitespace-nowrap text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 [&_svg]:pointer-events-none [&_svg]:size-4 [&_svg]:shrink-0 bg-secondary text-secondary-foreground hover:bg-secondary/80 h-10 w-10 absolute bottom-2 right-2 rounded-full">
                  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none"
                    stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"
                    class="lucide lucide-play h-4 w-4">
                    <polygon points="6 3 20 12 6 21 6 3"></polygon>
                  </svg>
                </button>
              </div>
            </div>
            <div class="px-4 pb-2">
              <h3 class="font-semibold tracking-tight text-base mb-2 line-clamp-1">
                {{ playlist.name }}
              </h3>
              <div class="flex items-center text-sm text-muted-foreground">
                <span class="relative flex shrink-0 overflow-hidden rounded-full w-6 h-6 mr-2">
                  <el-avatar class="aspect-square h-full w-full" :alt="playlist.creator.nickname"
                    :src="playlist.creator.avatarUrl" />
                </span>
                <span>{{ playlist.creator.nickname }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <nav class="mx-auto flex w-full justify-center mt-3">
      <el-pagination v-model:page-size="pageSize" v-model:currentPage="currentPage" v-bind="state"
        @size-change="handleSizeChange" @current-change="handleCurrentChange" class="mb-3" />
    </nav>
  </div>
</template>
