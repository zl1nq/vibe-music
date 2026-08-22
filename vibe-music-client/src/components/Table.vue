<script setup lang="ts">
import { Song } from '@/api/interface'
import { PropType, watch } from 'vue'
import { formatMillisecondsToTime } from '@/utils'
import default_album from '@/assets/default_album.jpg'
import { collectSong, cancelCollectSong } from '@/api/system'
import { ElMessage } from 'element-plus'
import { UserStore } from '@/stores/modules/user'

const audio = AudioStore()
const userStore = UserStore()
const { loadTrack, play } = useAudioPlayer()

const props = defineProps({
  data: {
    type: Array as PropType<Song[]>,
    default: () => [],
  },
})

// 监听数据变化，更新当前页面的歌曲列表
watch(() => props.data, (newData) => {
  audio.setCurrentPageSongs(newData)
}, { immediate: true })

// 转换歌曲实体
const convertToTrackModel = (song: Song) => {
  // console.log('原始歌曲数据:', song)
  if (!song.songId || !song.songName || !song.audioUrl) {
    console.error('歌曲数据不完整:', song)
    return null
  }
  return {
    id: song.songId.toString(),
    title: song.songName,
    artist: song.artistName,
    album: song.album,
    cover: song.coverUrl || default_album,
    url: song.audioUrl,
    duration: Number(song.duration) || 0,
    likeStatus: song.likeStatus || 0,
  }
}

// 播放音乐
const handlePlay = async (row: Song) => {
  // 先将所有表格数据转换为 trackModel
  const allTracks = props.data
    .map(song => convertToTrackModel(song))
    .filter(track => track !== null)

  // 找到当前选中歌曲的索引
  const selectedIndex = props.data.findIndex(song => song.songId === row.songId)

  // 清空现有播放列表并添加所有歌曲
  audio.setAudioStore('trackList', allTracks)
  // 设置当前播放索引为选中的歌曲
  audio.setAudioStore('currentSongIndex', selectedIndex)

  // 加载并播放选中的歌曲
  await loadTrack()
  play()
}

// 更新所有相同歌曲的喜欢状态
const updateAllSongLikeStatus = (songId: number, status: number) => {
  // 更新播放列表中的状态
  audio.trackList.forEach(track => {
    if (Number(track.id) === songId) {
      track.likeStatus = status
    }
  })

  // 更新当前页面的歌曲列表状态
  if (audio.currentPageSongs) {
    audio.currentPageSongs.forEach(song => {
      if (song.songId === songId) {
        song.likeStatus = status
      }
    })
  }

  // 更新原始数据
  if (props.data) {
    const song = props.data.find(song => song.songId === songId)
    if (song) {
      song.likeStatus = status
    }
  }
}

// 处理喜欢/取消喜欢
const handleLike = async (row: Song, e: Event) => {
  e.stopPropagation() // 阻止事件冒泡
  
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    if (row.likeStatus === 0) {
      // 收藏歌曲
      const res = await collectSong(row.songId)
      if (res.code === 0) {
        updateAllSongLikeStatus(row.songId, 1)
        ElMessage.success('已添加到我的喜欢')
      } else {
        ElMessage.error(res.message || '添加到我的喜欢失败')
      }
    } else {
      // 取消收藏
      const res = await cancelCollectSong(row.songId)
      if (res.code === 0) {
        updateAllSongLikeStatus(row.songId, 0)
        ElMessage.success('已取消喜欢')
      } else {
        ElMessage.error(res.message || '取消喜欢失败')
      }
    }
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

const downLoadMusic = (row: Song, e: Event) => {
  e.stopPropagation() // 阻止事件冒泡
  const link = document.createElement('a')
  link.href = row.audioUrl
  link.setAttribute('download', `${row.songName} - ${row.artistName}`)
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 判断是否是当前播放的歌曲
const isCurrentPlaying = (songId: number) => {
  const currentTrack = audio.trackList[audio.currentSongIndex]
  return currentTrack && Number(currentTrack.id) === songId
}
</script>

<template>
  <el-table :data="data" style="
      --el-table-border: none;
      --el-table-border-color: none;
      --el-table-tr-bg-color: none;
      --el-table-header-bg-color: none;
      --el-table-row-hover-bg-color: transparent;
    " class="!rounded-lg !h-full transition duration-300">
    <el-table-column>
      <template #header>
        <div class="grid grid-cols-[auto_4fr_3fr_3fr_1fr_2fr_1fr] items-center gap-6 w-full text-left mt-2">
          <div class="ml-3">标题</div>
          <div class="w-12"></div>
          <div class="ml-1">歌手</div>
          <div>专辑</div>
          <div>喜欢</div>
          <div class="ml-7">时长</div>
          <div>下载</div>
        </div>
      </template>
      <template #default="{ row }">
        <div
          class="grid grid-cols-[auto_4fr_3fr_3fr_1fr_2fr_1fr] items-center gap-6 w-full group transition duration-300 rounded-2xl p-2"
          :class="[
            isCurrentPlaying(row.songId) ? 'bg-[hsl(var(--hover-menu-bg))]' : 'hover:bg-[hsl(var(--hover-menu-bg))]',
            'cursor-pointer'
          ]"
          @click="handlePlay(row)">
          <!-- 标题和封面 -->
          <div class="w-10 h-10 relative" v-if="row.coverUrl">
            <el-image :src="row.coverUrl" fit="cover" lazy :alt="row.songName" class="w-full h-full rounded-md" />
            <!-- Play 按钮，使用 group-hover 控制透明度 -->
            <div
              class="absolute inset-0 flex items-center justify-center text-white opacity-0 transition-opacity duration-300 z-10 group-hover:opacity-100 group-hover:bg-black/50 rounded-md">
              <icon-tabler:player-play-filled class="text-lg" />
            </div>
          </div>

          <!-- 歌曲名称 -->
          <div class="text-left">
            <div class="flex-1 line-clamp-1">{{ row.songName }}</div>
          </div>

          <!-- 歌手 -->
          <div class="text-left">
            <div class="line-clamp-1 w-48">{{ row.artistName }}</div>
          </div>

          <!-- 专辑 -->
          <div class="text-left">{{ row.album }}</div>

          <!-- 喜欢 -->
          <div class="flex items-center ml-1">
            <el-button text circle @click="handleLike(row, $event)">
              <icon-mdi:cards-heart-outline v-if="!userStore.isLoggedIn || row.likeStatus === 0" class="text-lg" />
              <icon-mdi:cards-heart v-else class="text-lg text-red-500" />
            </el-button>
          </div>

          <!-- 时长 -->
          <div class="text-left ml-8">
            <span>{{ formatMillisecondsToTime(Number(row.duration) * 1000) }}</span>
          </div>

          <!-- 下载 -->
          <div class="flex items-center ml-1">
            <el-button text circle @click.stop="downLoadMusic(row, $event)">
              <icon-material-symbols:download class="text-lg" />
            </el-button>
          </div>
        </div>
      </template>
    </el-table-column>
  </el-table>
</template>

<style scoped>
:deep(.el-table__row) {
  background: transparent !important;
}

:deep(.el-table__row:hover) td {
  background: transparent !important;
}

:deep(.el-table__cell) {
  padding: 0 !important;
}
</style>