<script setup lang="ts">
import Header from './components/header/index.vue'
import Aside from './components/aside/index.vue'
import Main from './components/main/index.vue'
import Footer from './components/footer/index.vue'
// import BG from './components/bg/index.vue'
import AgentChat from '@/components/Agent/AgentChat.vue'
import { AudioPlayer } from '@/hooks/useAudioPlayer'

provide('audioPlayer', AudioPlayer())
</script>
<template>
  <!-- <BG /> -->
  <SplashScreen />
  <div class="absolute w-full flex flex-col h-full">
    <div class="w-full flex flex-col h-full overflow-hidden bg-background">
      <Header />
      <div class="flex flex-1 overflow-hidden">
        <Aside />
        <Main />
      </div>
      <Footer />
    </div>
    <!-- 全局悬浮的 VibeAgent 聊天窗 -->
    <AgentChat />
  </div>
</template>
