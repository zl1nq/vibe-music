<script setup lang="ts">
import { ref } from 'vue'
import { useAudioPlayer } from '@/hooks/useAudioPlayer'
import DrawerMusic from '@/components/DrawerMusic/index.vue'

const { currentTrack, isPlaying } = useAudioPlayer()
const showDrawerMusic = ref(false)
</script>

<template>
  <div
    class="flex items-center gap-2 w-64 cursor-pointer select-none hover:bg-hoverMenuBg transition-colors rounded-xl p-1.5"
    @click="showDrawerMusic = !showDrawerMusic"
  >
    <div class="min-w-11 max-w-11 h-11">
      <img
        :src="currentTrack.cover + '?param=90y90'"
        :alt="currentTrack.title"
        class="w-full h-full object-cover rounded-full ring-2 ring-border"
        :class="isPlaying ? 'animate-spin-slow' : ''"
      />
    </div>
    <div class="min-w-0">
      <div class="flex items-center gap-2 mx-2 mb-0.5">
        <div
          class="text-base text-primary-foreground line-clamp-1"
          :title="currentTrack.title"
        >
          {{ currentTrack.title }}
        </div>
        <PlayingIndicator v-if="isPlaying" class="flex-shrink-0" />
      </div>
      <div class="text-xs text-muted-foreground line-clamp-1 h-4 mt-0.5 mx-2">
        {{ currentTrack.artist }}
      </div>
    </div>
    <DrawerMusic v-model="showDrawerMusic" />
  </div>
</template>
