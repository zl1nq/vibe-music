/**
 * VibeAgent 相关的解析工具
 */

/** agent 推荐的一首歌曲（与后端 SongVO 对应） */
export interface VibeSong {
  songId: number
  songName: string
  artistName: string
  album: string
  duration: string
  coverUrl: string
  audioUrl: string
  likeStatus?: number
  releaseTime?: string
}

const VIBE_SONGS_TAG = /<vibe-songs>([\s\S]*?)<\/vibe-songs>/g

/** 从回复文本中提取 <vibe-songs> 标签里的歌曲列表 */
export const extractVibeSongs = (text: string): VibeSong[] => {
  const songs: VibeSong[] = []
  let match: RegExpExecArray | null
  VIBE_SONGS_TAG.lastIndex = 0
  while ((match = VIBE_SONGS_TAG.exec(text)) !== null) {
    try {
      const parsed = JSON.parse(match[1])
      if (Array.isArray(parsed)) {
        songs.push(...parsed)
      } else if (parsed && typeof parsed === 'object') {
        songs.push(parsed)
      }
    } catch {
      // 忽略无法解析的标签，仅展示文本
    }
  }
  return songs
}

/** 去掉回复文本中的 <vibe-songs> 标签，只保留正文 */
export const stripVibeSongsTag = (text: string): string =>
  text.replace(VIBE_SONGS_TAG, '').trim()
