<script setup lang="ts">
import { getRecommendedPlaylists, getRecommendedSongs, getBanner } from '@/api/system'
import coverImg from '@/assets/cover.png'
import { formatTime, replaceUrlParams } from '@/utils'
import { ElNotification } from 'element-plus'
import { UserStore } from '@/stores/modules/user'
const router = useRouter()
const audio = AudioStore()
const user = UserStore()

const { loadTrack, play } = useAudioPlayer()

const bannerList = ref<{ bannerId: number; bannerUrl: string }[]>([])

// 推荐歌单
const recommendedPlaylist = ref([])
// 推荐歌曲
const recommendedSongList = ref([])

// 监听用户登录状态
watch(
  () => user.isLoggedIn,
  (newVal) => {
    if (newVal) {
      // 用户登录后重新获取推荐数据
      getRecommendedData()
    }
  }
)

// 获取轮播图数据
const fetchBannerData = async () => {
  try {
    const result = await getBanner()
    if (result.code === 0 && Array.isArray(result.data)) {
      bannerList.value = result.data
    } else {
      ElNotification({
        type: 'error',
        message: '获取轮播图失败',
        duration: 2000,
      })
    }
  } catch (error) {
    console.error('Error fetching banner data:', error)
    ElNotification({
      type: 'error',
      message: '获取轮播图时发生错误',
      duration: 2000,
    })
  }
}

// 获取推荐数据
const getRecommendedData = async () => {
  // 获取推荐歌单
  const result = await getRecommendedPlaylists()
  if (result.code === 0 && Array.isArray(result.data)) {
    recommendedPlaylist.value = result.data.map((item) => ({
      playlistId: item.playlistId,
      title: item.title,
      coverUrl: item.coverUrl ?? coverImg,
    }))
  } else {
    ElNotification({
      type: 'error',
      message: '获取推荐歌单失败',
      duration: 2000,
    })
  }

  // 获取推荐歌曲
  handleRefreshSongs()
}

onMounted(async () => {
  // 获取轮播图数据
  fetchBannerData()
  // 初始化时获取推荐数据
  getRecommendedData()
})

const handleRefreshSongs = async () => {
  const result = await getRecommendedSongs()
  if (result.code === 0 && Array.isArray(result.data)) {
    recommendedSongList.value = result.data.map((item) => ({
      id: item.songId,
      name: item.songName,
      artists: [
        {
          name: item.artistName,
        },
      ],
      album: {
        name: item.album,
        picUrl: item.coverUrl,
      },
      duration: item.duration,
      audioUrl: item.audioUrl,
      likeStatus: item.likeStatus || 0  // 从服务端获取收藏状态
    }))
  } else {
    ElNotification({
      type: 'error',
      message: '获取推荐歌曲失败',
      duration: 2000,
    })
  }
}

// 转换歌曲实体
const convertToTrackModel = (song: any) => {
  return {
    id: song.id.toString(),
    title: song.name,
    artist: song.artists.map((artist: any) => artist.name).join(', '),
    album: song.album.name,
    cover: song.album.picUrl || '',
    url: song.audioUrl,
    duration: song.duration,
    likeStatus: song.likeStatus || 0  // 保持收藏状态
  }
}

const handlePlaylclick = async (row: any) => {
  // 将所有推荐歌曲转换为 trackModel
  const allTracks = recommendedSongList.value
    .map(song => convertToTrackModel(song))
    .filter(track => track !== null)

  // 找到当前选中歌曲的索引
  const selectedIndex = recommendedSongList.value.findIndex(song => song.id === row.id)

  // 清空现有播放列表并添加所有歌曲
  audio.setAudioStore('trackList', allTracks)
  // 设置当前播放索引为选中的歌曲
  audio.setAudioStore('currentSongIndex', selectedIndex)

  // 播放
  await loadTrack()
  play()
}

// 判断是否是当前播放的歌曲
const isCurrentPlaying = (songId: number) => {
  const currentTrack = audio.trackList[audio.currentSongIndex]
  return currentTrack && Number(currentTrack.id) === songId
}
</script>
<template>
  <div class="flex gap-6 p-4 w-full">
    <div class="flex-1">
      <div class="w-full flex flex-col overflow-hidden mb-8">
        <!-- banner -->
        <el-carousel :interval="4000" type="card" height="260px">
          <el-carousel-item v-for="item in bannerList" :key="item.bannerId">
            <img :src="item.bannerUrl" class="w-full h-full object-cover rounded-lg" />
          </el-carousel-item>
        </el-carousel>

        <!-- 推荐 -->
        <button class="mt-6">
          <div class="flex justify-between items-center mb-4">
            <h2 class="text-xl font-semibold">今日为你推荐</h2>
            <button @click="router.push('/playlist')"
              class="inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 [&amp;_svg]:pointer-events-none [&amp;_svg]:size-4 [&amp;_svg]:shrink-0 text-primary underline-offset-4 hover:underline h-10 px-4 py-2">
              <icon-hugeicons:more class="text-lg" />
              更多
            </button>
          </div>
          <div class="grid grid-cols-4 md:grid-cols-7 gap-4">
            <div
              class="rounded-2xl transition duration-300 hover:bg-hoverMenuBg bg-card text-card-foreground border-0 shadow-nonec cursor-pointer"
              v-for="i in recommendedPlaylist.slice(0, 7)" :key="i.playlistId"
              @click="router.push(`/playlist/${i.playlistId}`)">
              <div class="p-0">
                <div class="aspect-square rounded-t-2xl overflow-hidden">
                  <img :alt="i.title" loading="lazy" width="200" height="200" class="w-full h-full object-cover" :src="replaceUrlParams(i.coverUrl ?? coverImg, 'param=350y350')
                    " />
                  />
                </div>
                <div class="flex flex-col p-2">
                  <h3 class="line-clamp-2 font-medium mb-1 playlist-title">
                    {{ i.title }}
                  </h3>
                </div>
              </div>
            </div>
          </div>
        </button>
      </div>

      <!-- 歌曲 -->
      <div class="w-full">
        <div class="flex justify-between items-center mb-4">
          <h2 class="text-xl font-semibold mb-4">相似推荐</h2>
          <button @click="handleRefreshSongs()"
            class="inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 [&amp;_svg]:pointer-events-none [&amp;_svg]:size-4 [&amp;_svg]:shrink-0 text-primary underline-offset-4 hover:underline h-10 px-4 py-2">
            <icon-tabler:refresh class="text-lg" />
            刷新
          </button>
        </div>
        <el-scrollbar class="h-full" overflow-auto>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4 gap-x-16">
            <button v-for="item in recommendedSongList" :key="item.id"
              class="grid grid-cols-[auto_2fr_1fr] items-center gap-4 transition duration-300 rounded-2xl w-full group"
              :class="[
                isCurrentPlaying(item.id) ? 'bg-hoverMenuBg' : 'hover:bg-hoverMenuBg'
              ]" @click.stop="handlePlaylclick(item)">
              <!-- 专辑封面 -->
              <div class="w-16 h-16 rounded-2xl overflow-hidden relative">
                <el-image :alt="item.name" width="64" height="64" class="w-full h-full object-cover"
                  :src="item.album.picUrl + '?param=90y90'" />
                <!-- Play 按钮，使用 group-hover 控制透明度 -->
                <button @click.stop="handlePlaylclick(item)"
                  class="absolute inset-0 flex items-center justify-center text-white opacity-0 transition-opacity duration-300 z-10 group-hover:opacity-100 group-hover:bg-black/50">
                  <icon-tabler:player-play-filled class="text-lg" />
                </button>
              </div>

              <div class="truncate text-left ml-1">
                <!-- 歌曲名称 -->
                <h3 class="font-medium">{{ item.name }}</h3>
                <!-- 艺术家 -->
                <p class="text-sm text-muted-foreground line-clamp-1">
                  {{item.artists.map((item) => item.name).join(' ')}}
                </p>
              </div>

              <!-- 时长 -->
              <div class="text-right mr-5">
                <p class="text-sm text-muted-foreground line-clamp-1">
                  {{ formatTime(item.duration) }}
                </p>
              </div>
            </button>
          </div>
        </el-scrollbar>
      </div>
    </div>
  </div>
</template>

<style scoped>
:deep(.el-carousel__item) {
  --el-carousel-item-scale: 1.2;
  /* 默认是 0.83，可以调整 */
}

/* 让所有图片撑满 */
.el-carousel__item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 20px;
  /* 圆角 */
}

.playlist-title {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  text-align: left;
  min-height: 2.5em;
  /* 固定两行高度 */
  line-height: 1.25;
  /* 行高 */
  overflow: hidden;
  text-overflow: ellipsis;
  display: flex;
  align-items: center;
  /* 单行时垂直居中 */
}
</style>
