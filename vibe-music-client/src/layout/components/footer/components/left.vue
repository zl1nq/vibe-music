<script setup lang="ts">
import { ref } from 'vue'
import { useAudioPlayer } from '@/hooks/useAudioPlayer'
import DrawerMusic from '@/components/DrawerMusic/index.vue'

const { currentTrack } = useAudioPlayer()
const showDrawerMusic = ref(false)
</script>

<template>
  <div 
    class="flex items-center gap-2 w-64 cursor-pointer select-none hover:bg-hoverMenuBg transition-colors rounded-lg p-1" 
    @click="showDrawerMusic = !showDrawerMusic"
  >
    <div class="min-w-12 max-w-12 h-full">
      <img
        :src="currentTrack.cover + '?param=90y90'"
        :alt="currentTrack.title"
        class="w-full h-full object-cover rounded-lg m-1"
      />
    </div>
    <div>
      <div
        class="text-base text-primary-foreground line-clamp-1 mb-0.5 mx-2"
        :title="currentTrack.title"
      >
        {{ currentTrack.title }}
      </div>
      <div class="text-xs text-muted-foreground line-clamp-1 h-4 mt-0.5 mx-2">
        {{ currentTrack.artist }}
      </div>
    </div>
    <DrawerMusic v-model="showDrawerMusic" />
  </div>
</template>
