<script setup lang="ts">
import { nextTick, ref } from 'vue'
import { Icon } from '@iconify/vue'
import { ElMessage } from 'element-plus'
import { streamAgentChat } from '@/api/agent'
import { extractVibeSongs, stripVibeSongsTag, VibeSong } from '@/utils/agent'
import { useAudioPlayer } from '@/hooks/useAudioPlayer'
import default_album from '@/assets/default_album.jpg'

interface ChatMessage {
  role: 'user' | 'assistant'
  content: string
  songs: VibeSong[]
  loading?: boolean
}

const audio = AudioStore()
const { loadTrack, play } = useAudioPlayer()

const open = ref(false)
const input = ref('')
const messages = ref<ChatMessage[]>([])
const messageListEl = ref<HTMLElement | null>(null)
const streaming = ref(false)
let abortController: AbortController | null = null

// 会话 id 持久化，刷新页面后仍能延续对话记忆
const conversationId = ref(
  localStorage.getItem('vibe-agent-conversation-id') || crypto.randomUUID()
)
localStorage.setItem('vibe-agent-conversation-id', conversationId.value)

const suggestions = [
  '推荐几首摇滚歌曲',
  '心情低落的时候听什么好？',
  '有哪些好听的流行歌',
]

const scrollToBottom = async () => {
  await nextTick()
  if (messageListEl.value) {
    messageListEl.value.scrollTop = messageListEl.value.scrollHeight
  }
}

const send = async () => {
  const text = input.value.trim()
  if (!text || streaming.value) return

  input.value = ''
  messages.value.push({ role: 'user', content: text, songs: [] })
  const assistantMsg: ChatMessage = {
    role: 'assistant',
    content: '',
    songs: [],
    loading: true,
  }
  messages.value.push(assistantMsg)
  streaming.value = true
  abortController = new AbortController()
  await scrollToBottom()

  try {
    await streamAgentChat(
      conversationId.value,
      text,
      (delta) => {
        assistantMsg.content += delta
        assistantMsg.songs = extractVibeSongs(assistantMsg.content)
        scrollToBottom()
      },
      abortController.signal
    )
  } catch (error: any) {
    if (error.name !== 'AbortError') {
      assistantMsg.content =
        assistantMsg.content || '抱歉，请求失败了，请稍后再试。'
      ElMessage.error('请求失败，请检查网络或服务状态')
    }
  } finally {
    assistantMsg.loading = false
    assistantMsg.songs = extractVibeSongs(assistantMsg.content)
    streaming.value = false
    abortController = null
    await scrollToBottom()
  }
}

// 播放推荐歌曲：加入播放队列并立即播放
const playSong = async (song: VibeSong) => {
  audio.addTracks({
    id: song.songId.toString(),
    title: song.songName,
    artist: song.artistName,
    album: song.album,
    cover: song.coverUrl || default_album,
    url: song.audioUrl,
    duration: Number(song.duration) || 0,
    likeStatus: song.likeStatus || 0,
  })
  await loadTrack()
  play()
  ElMessage.success(`正在播放：${song.songName} - ${song.artistName}`)
}

// 新建会话：清空记忆与聊天记录
const newConversation = () => {
  if (abortController) abortController.abort()
  messages.value = []
  conversationId.value = crypto.randomUUID()
  localStorage.setItem('vibe-agent-conversation-id', conversationId.value)
  ElMessage.success('已开启新会话')
}
</script>

<template>
  <!-- 悬浮入口按钮 -->
  <button
    v-if="!open"
    class="fixed bottom-24 right-6 w-14 h-14 rounded-full bg-primary text-white shadow-lg shadow-primary/30 flex items-center justify-center transition-all duration-300 ease-spring hover:scale-110 hover:rotate-6 z-[100]"
    aria-label="打开 VibeAgent"
    @click="open = true"
  >
    <Icon icon="mdi:robot-outline" class="text-2xl" />
  </button>

  <!-- 聊天面板 -->
  <Transition
    enter-active-class="transition duration-300 ease-spring"
    enter-from-class="opacity-0 translate-y-4 scale-95"
    leave-active-class="transition duration-200 ease-out-soft"
    leave-to-class="opacity-0 translate-y-4 scale-95"
  >
    <div
      v-if="open"
      class="fixed bottom-24 right-6 w-[400px] max-w-[calc(100vw-3rem)] h-[560px] max-h-[calc(100vh-9rem)] bg-card border border-border rounded-2xl shadow-xl flex flex-col overflow-hidden z-[100]"
    >
      <!-- 头部 -->
      <div
        class="flex items-center gap-2.5 px-4 py-3 border-b border-border bg-background"
      >
        <div
          class="w-9 h-9 rounded-full bg-primary/10 text-primary flex items-center justify-center"
        >
          <Icon icon="mdi:robot-outline" class="text-xl" />
        </div>
        <div class="leading-tight">
          <div class="font-serif-display font-bold tracking-tight">
            VibeAgent
          </div>
          <div class="text-xs text-muted-foreground">你的私人音乐向导</div>
        </div>
        <div class="ml-auto flex items-center gap-1">
          <el-tooltip content="新会话" placement="bottom">
            <button
              class="p-2 rounded-lg text-muted-foreground hover:text-primary hover:bg-hoverMenuBg transition-all duration-300"
              @click="newConversation"
            >
              <Icon icon="mdi:chat-plus-outline" class="text-lg" />
            </button>
          </el-tooltip>
          <button
            class="p-2 rounded-lg text-muted-foreground hover:text-primary hover:bg-hoverMenuBg transition-all duration-300"
            aria-label="关闭"
            @click="open = false"
          >
            <Icon icon="mdi:close" class="text-lg" />
          </button>
        </div>
      </div>

      <!-- 消息区 -->
      <div
        ref="messageListEl"
        class="flex-1 overflow-y-auto px-4 py-4 space-y-4"
      >
        <!-- 欢迎态 -->
        <div
          v-if="messages.length === 0"
          class="pt-6 flex flex-col items-center gap-4 text-center"
        >
          <div
            class="w-16 h-16 rounded-full bg-primary/10 text-primary flex items-center justify-center"
          >
            <Icon icon="mdi:music-note-eighth" class="text-3xl" />
          </div>
          <div>
            <div class="font-serif-display font-bold text-lg">
              Hi，想听点什么？
            </div>
            <div class="text-sm text-muted-foreground mt-1">
              告诉我你的心情、风格或歌手，我来为你推荐音乐
            </div>
          </div>
          <div class="flex flex-col gap-2 w-full px-6">
            <button
              v-for="(item, index) in suggestions"
              :key="index"
              class="text-sm text-primary border border-primary/30 rounded-full py-1.5 px-4 hover:bg-primary hover:text-white transition-all duration-300"
              @click="input = item"
            >
              {{ item }}
            </button>
          </div>
        </div>

        <!-- 消息列表 -->
        <template v-for="(msg, index) in messages" :key="index">
          <!-- 用户消息 -->
          <div v-if="msg.role === 'user'" class="flex justify-end">
            <div
              class="max-w-[80%] bg-primary text-white text-sm leading-relaxed rounded-2xl rounded-br-md px-4 py-2.5 whitespace-pre-wrap"
            >
              {{ msg.content }}
            </div>
          </div>

          <!-- 助手消息 -->
          <div v-else class="flex gap-2.5">
            <div
              class="w-8 h-8 mt-0.5 rounded-full bg-primary/10 text-primary flex items-center justify-center flex-shrink-0"
            >
              <Icon icon="mdi:robot-outline" class="text-base" />
            </div>
            <div class="flex-1 min-w-0 space-y-2">
              <div
                class="text-sm leading-relaxed whitespace-pre-wrap bg-hoverMenuBg rounded-2xl rounded-tl-md px-4 py-2.5"
              >
                <span v-if="!msg.content && msg.loading">正在思考...</span>
                <span v-else>{{ stripVibeSongsTag(msg.content) }}</span>
              </div>

              <!-- 推荐歌曲卡片 -->
              <div
                v-if="msg.songs.length"
                class="space-y-2 max-h-56 overflow-y-auto"
              >
                <div
                  v-for="song in msg.songs"
                  :key="song.songId"
                  class="group flex items-center gap-3 bg-background border border-border rounded-xl p-2 cursor-pointer hover:border-primary/40 hover:shadow-paper transition-all duration-300"
                  @click="playSong(song)"
                >
                  <el-image
                    lazy
                    :src="song.coverUrl || default_album"
                    class="w-10 h-10 rounded-lg flex-shrink-0"
                    :alt="song.songName"
                  />
                  <div class="flex-1 min-w-0">
                    <div class="text-sm line-clamp-1 font-medium">
                      {{ song.songName }}
                    </div>
                    <div class="text-xs text-muted-foreground line-clamp-1">
                      {{ song.artistName }} · {{ song.album }}
                    </div>
                  </div>
                  <button
                    class="w-8 h-8 rounded-full bg-primary text-white flex items-center justify-center opacity-0 group-hover:opacity-100 transition-all duration-300 hover:scale-110 flex-shrink-0"
                    aria-label="播放"
                    @click.stop="playSong(song)"
                  >
                    <Icon icon="mdi:play" class="text-base" />
                  </button>
                </div>
              </div>

              <!-- 流式加载指示 -->
              <div v-if="msg.loading && msg.content" class="flex gap-1 pl-1">
                <span
                  class="w-1.5 h-1.5 rounded-full bg-primary animate-bounce"
                  style="animation-delay: 0ms"
                ></span>
                <span
                  class="w-1.5 h-1.5 rounded-full bg-primary animate-bounce"
                  style="animation-delay: 150ms"
                ></span>
                <span
                  class="w-1.5 h-1.5 rounded-full bg-primary animate-bounce"
                  style="animation-delay: 300ms"
                ></span>
              </div>
            </div>
          </div>
        </template>
      </div>

      <!-- 输入区 -->
      <div class="px-4 py-3 border-t border-border bg-background">
        <div
          class="flex items-center gap-2 bg-card border border-border rounded-xl px-3 py-1.5 focus-within:ring-2 focus-within:ring-primary/50 transition-all duration-300"
        >
          <input
            v-model="input"
            type="text"
            class="flex-1 bg-transparent text-sm outline-none placeholder:text-muted-foreground"
            placeholder="告诉我你想听什么..."
            @keyup.enter="send"
          />
          <button
            class="w-8 h-8 rounded-lg bg-primary text-white flex items-center justify-center transition-all duration-300 disabled:opacity-40 disabled:cursor-not-allowed hover:scale-105 flex-shrink-0"
            :disabled="streaming || !input.trim()"
            aria-label="发送"
            @click="send"
          >
            <Icon icon="mdi:send" class="text-base" />
          </button>
        </div>
        <div class="text-[10px] text-muted-foreground/70 text-center mt-1.5">
          VibeAgent 由通义千问驱动，推荐结果来自真实曲库
        </div>
      </div>
    </div>
  </Transition>
</template>
