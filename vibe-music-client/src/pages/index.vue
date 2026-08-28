<script setup lang="ts">
import {
  getRecommendedPlaylists,
  getRecommendedSongs,
  getBanner,
} from '@/api/system'
import coverImg from '@/assets/cover.png'
import { formatTime, replaceUrlParams } from '@/utils'
import { ElNotification } from 'element-plus'
import { UserStore } from '@/stores/modules/user'
const router = useRouter()
const audio = AudioStore()
const user = UserStore()

const { loadTrack, play, isPlaying } = useAudioPlayer()

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
      likeStatus: item.likeStatus || 0, // 从服务端获取收藏状态
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
    likeStatus: song.likeStatus || 0, // 保持收藏状态
  }
}

const handlePlaylclick = async (row: any) => {
  // 将所有推荐歌曲转换为 trackModel
  const allTracks = recommendedSongList.value
    .map((song) => convertToTrackModel(song))
    .filter((track) => track !== null)

  // 找到当前选中歌曲的索引
  const selectedIndex = recommendedSongList.value.findIndex(
    (song) => song.id === row.id
  )

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
  <div class="flex gap-6 px-8 py-6 w-full">
    <div class="flex-1 min-w-0">
      <!-- banner -->
      <section class="w-full mb-12" v-reveal>
        <el-carousel
          v-if="bannerList.length"
          :interval="4000"
          type="card"
          height="280px"
        >
          <el-carousel-item
            v-for="(item, index) in bannerList"
            :key="item.bannerId"
          >
            <img
              :src="item.bannerUrl"
              class="w-full h-full object-cover"
              decoding="async"
              :fetchpriority="index === 0 ? 'high' : 'auto'"
            />
          </el-carousel-item>
        </el-carousel>
        <div v-else class="h-[280px] rounded-xl bg-muted animate-pulse"></div>
      </section>

      <!-- 推荐歌单 -->
      <section class="w-full mb-12" v-reveal>
        <div
          class="flex justify-between items-end border-b border-border pb-4 mb-6"
        >
          <div>
            <p
              class="text-[10px] font-semibold tracking-[0.3em] text-primary mb-1.5"
            >
              WEEKLY SELECTION
            </p>
            <h2
              class="text-2xl font-bold font-serif-display tracking-tight flex items-baseline gap-3"
            >
              今日为你推荐
              <span
                class="text-[11px] font-sans font-normal tracking-[0.2em] text-muted-foreground"
                >No.01</span
              >
            </h2>
          </div>
          <button
            @click="router.push('/playlist')"
            class="inline-flex items-center gap-1.5 text-sm font-medium text-muted-foreground hover:text-primary transition-colors duration-200"
          >
            更多
            <icon-hugeicons:more class="text-base" />
          </button>
        </div>

        <div
          v-if="recommendedPlaylist.length"
          class="grid grid-cols-3 md:grid-cols-5 gap-5"
        >
          <div
            v-for="(i, index) in recommendedPlaylist.slice(0, 5)"
            :key="i.playlistId"
            class="group rounded-xl bg-card border border-border/60 shadow-paper cursor-pointer overflow-hidden transition-all duration-300 ease-out-soft hover:-translate-y-1 hover:shadow-paper-lg"
            v-reveal="index * 70"
            @click="router.push(`/playlist/${i.playlistId}`)"
          >
            <div class="aspect-square overflow-hidden relative">
              <img
                :alt="i.title"
                decoding="async"
                class="w-full h-full object-cover transition-transform duration-500 ease-out-soft group-hover:scale-105"
                :src="replaceUrlParams(i.coverUrl ?? coverImg, 'param=350y350')"
              />
              <div
                class="absolute inset-0 bg-black/0 group-hover:bg-black/15 transition-colors duration-300"
              ></div>
              <div
                class="absolute bottom-2.5 right-2.5 w-9 h-9 rounded-full bg-primary text-white flex items-center justify-center opacity-0 translate-y-1.5 group-hover:opacity-100 group-hover:translate-y-0 transition-all duration-300 ease-spring shadow-lg pointer-events-none"
              >
                <icon-tabler:player-play-filled class="text-sm ml-0.5" />
              </div>
            </div>
            <div class="p-3">
              <h3 class="line-clamp-2 text-sm font-medium playlist-title">
                {{ i.title }}
              </h3>
            </div>
          </div>
        </div>
        <!-- 加载态：纸感骨架 -->
        <div v-else class="grid grid-cols-3 md:grid-cols-5 gap-5">
          <div
            v-for="n in 5"
            :key="n"
            class="rounded-xl bg-card border border-border/60 overflow-hidden"
          >
            <div class="aspect-square w-full bg-muted animate-pulse"></div>
            <div class="p-3 space-y-2">
              <div class="h-3.5 w-4/5 rounded bg-muted animate-pulse"></div>
              <div class="h-3 w-1/2 rounded bg-muted animate-pulse"></div>
            </div>
          </div>
        </div>
      </section>

      <!-- 推荐歌曲 -->
      <section class="w-full" v-reveal>
        <div
          class="flex justify-between items-end border-b border-border pb-4 mb-6"
        >
          <div>
            <p
              class="text-[10px] font-semibold tracking-[0.3em] text-primary mb-1.5"
            >
              FOR YOUR TASTE
            </p>
            <h2
              class="text-2xl font-bold font-serif-display tracking-tight flex items-baseline gap-3"
            >
              相似推荐
              <span
                class="text-[11px] font-sans font-normal tracking-[0.2em] text-muted-foreground"
                >No.02</span
              >
            </h2>
          </div>
          <button
            @click="handleRefreshSongs()"
            class="inline-flex items-center gap-1.5 text-sm font-medium text-muted-foreground hover:text-primary transition-colors duration-200 group/refresh"
          >
            刷新
            <icon-tabler:refresh
              class="text-base transition-transform duration-500 group-hover/refresh:rotate-180"
            />
          </button>
        </div>

        <el-scrollbar
          v-if="recommendedSongList.length"
          class="h-full"
          overflow-auto
        >
          <div class="grid grid-cols-1 md:grid-cols-2 gap-x-16 gap-y-1">
            <div
              v-for="(item, index) in recommendedSongList"
              :key="item.id"
              class="grid grid-cols-[auto_auto_2fr_1fr] items-center gap-4 p-2 transition-all duration-200 rounded-xl w-full group cursor-pointer"
              v-reveal="index * 40"
              :class="[
                isCurrentPlaying(item.id)
                  ? 'bg-activeMenuBg'
                  : 'hover:bg-hoverMenuBg',
              ]"
              @click.stop="handlePlaylclick(item)"
            >
              <!-- 序号 -->
              <span
                class="text-xs font-serif-display text-muted-foreground w-6 text-right tabular-nums"
              >
                {{ String(index + 1).padStart(2, '0') }}
              </span>
              <!-- 专辑封面 -->
              <div
                class="w-14 h-14 rounded-lg overflow-hidden relative flex-shrink-0"
              >
                <el-image
                  :alt="item.name"
                  class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
                  :src="item.album.picUrl + '?param=90y90'"
                />
                <!-- Play 按钮，使用 group-hover 控制透明度 -->
                <button
                  @click.stop="handlePlaylclick(item)"
                  class="absolute inset-0 flex items-center justify-center text-white opacity-0 transition-opacity duration-300 z-10 group-hover:opacity-100 group-hover:bg-black/50"
                >
                  <icon-tabler:player-play-filled class="text-lg" />
                </button>
              </div>

              <div class="truncate text-left ml-1">
                <!-- 歌曲名称 -->
                <h3
                  class="font-medium flex items-center gap-2"
                  :class="isCurrentPlaying(item.id) ? 'text-primary' : ''"
                >
                  <span class="truncate">{{ item.name }}</span>
                  <PlayingIndicator
                    v-if="isCurrentPlaying(item.id)"
                    :paused="!isPlaying"
                    class="flex-shrink-0 scale-75 origin-left"
                  />
                </h3>
                <!-- 艺术家 -->
                <p class="text-sm text-muted-foreground line-clamp-1">
                  {{ item.artists.map((item) => item.name).join(' ') }}
                </p>
              </div>

              <!-- 时长 -->
              <div class="text-right mr-3">
                <p
                  class="text-sm text-muted-foreground line-clamp-1 tabular-nums"
                >
                  {{ formatTime(item.duration) }}
                </p>
              </div>
            </div>
          </div>
        </el-scrollbar>
        <!-- 加载态：纸感骨架 -->
        <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-x-16 gap-y-1">
          <div v-for="n in 8" :key="n" class="flex items-center gap-4 p-2">
            <div
              class="w-14 h-14 rounded-lg bg-muted animate-pulse flex-shrink-0"
            ></div>
            <div class="flex-1 space-y-2">
              <div class="h-3.5 w-3/5 rounded bg-muted animate-pulse"></div>
              <div class="h-3 w-2/5 rounded bg-muted animate-pulse"></div>
            </div>
          </div>
        </div>
      </section>
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
  border-radius: 12px;
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
}
</style>
