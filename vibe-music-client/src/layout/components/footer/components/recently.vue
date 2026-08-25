<script setup lang="ts">
import { formatMillisecondsToTime } from '@/utils'
import { trackModel } from '@/stores/interface'
const audio = AudioStore()
const { loadTrack, play, audioElement, isPlaying } = useAudioPlayer()

const mouseOverIndex = ref(-1) // 用于跟踪鼠标悬停的索引

const playMusic = async (song: trackModel) => {
  audio.addTracks(song)
  // 加载
  await loadTrack()
  play()
}

const handleClearAll = () => {
  audio.setAudioStore('trackList', [])
  if (audioElement.value) {
    audioElement.value.src = ''
  }
}
</script>
<template>
  <el-popover
    :width="450"
    trigger="click"
    placement="top-end"
    popper-class="!rounded-lg !p-0"
  >
    <template #reference>
      <div class="flex items-center">
        <button class="p-2 rounded-full hover:bg-hoverMenuBg transition w-9">
          <icon-ri-play-list-2-fill class="w-full h-full" />
        </button>
      </div>
    </template>
    <div class="bg-popover rounded-xl p-3">
      <div class="flex items-center justify-between mb-2 px-1">
        <span
          class="text-xs font-semibold tracking-widest text-muted-foreground"
          >播放列表 · UP NEXT</span
        >
        <button
          class="p-1.5 rounded-full hover:bg-hoverMenuBg hover:text-primary transition"
          @click="handleClearAll"
        >
          <icon-material-symbols:delete-outline-rounded
            class="text-sm text-muted-foreground"
          />
        </button>
      </div>
      <div class="flex flex-col">
        <div
          v-if="!audio.trackList.length"
          class="h-96 flex flex-col items-center justify-center gap-3"
        >
          <div class="w-10 h-px bg-border"></div>
          <p class="text-xs tracking-[0.25em] text-muted-foreground">
            暂无播放歌曲 · EMPTY
          </p>
          <div class="w-10 h-px bg-border"></div>
        </div>
        <el-scrollbar v-else class="h-96">
          <div
            v-for="(item, index) in audio.trackList"
            :key="index"
            @click="playMusic(item)"
            @mouseover="mouseOverIndex = index"
            @mouseleave="mouseOverIndex = -1"
            class="flex items-center gap-2 p-2 my-0.5 rounded-lg transition-all duration-200 group cursor-pointer"
            :class="`hover:bg-hoverMenuBg ${audio.currentSongIndex == index ? 'bg-activeMenuBg' : ''}`"
          >
            <div
              class="w-10 h-10 rounded-lg overflow-hidden relative flex-shrink-0"
            >
              <img
                :src="item.cover"
                alt=""
                class="w-full h-full object-cover"
              />
              <!-- Play 按钮，使用 group-hover 控制透明度 -->
              <div
                class="absolute inset-0 flex items-center justify-center text-white opacity-0 transition-opacity duration-300 z-10 group-hover:opacity-100 group-hover:bg-black/50"
              >
                <icon-tabler:player-play-filled class="text-lg" />
              </div>
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-1.5">
                <div
                  class="text-sm line-clamp-1"
                  :class="
                    audio.currentSongIndex == index
                      ? 'text-primary font-medium'
                      : ''
                  "
                >
                  {{ item.title }}
                </div>
                <PlayingIndicator
                  v-if="audio.currentSongIndex == index"
                  :paused="!isPlaying"
                  class="flex-shrink-0 scale-75 origin-left"
                />
              </div>
              <div class="text-xs text-muted-foreground line-clamp-1">
                {{ item.artist }}
              </div>
            </div>
            <div class="text-xs text-muted-foreground tabular-nums">
              {{ formatMillisecondsToTime(Number(item.duration) * 1000) }}
            </div>
            <el-button
              v-show="mouseOverIndex == index"
              type="primary"
              text
              circle
              @click="audio.deleteTrack(item.id)"
            >
              <icon-material-symbols:delete-outline-rounded class="text-lg" />
            </el-button>
          </div>
        </el-scrollbar>
      </div>
    </div>
  </el-popover>
</template>
