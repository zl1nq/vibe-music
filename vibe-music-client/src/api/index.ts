import { httpGet } from '@/utils/http'

// 获取音乐连接
export const urlV1 = (id: number | string) => {
  const audio = AudioStore()
  return httpGet<{ data: { url: string }[] }>(
    `song/url/v1?id=${id}&level=${audio.quality}`
  )
}

// 获取歌词
// export const lyric = (id: number | string) =>
//   httpGet<LyricsResponse>(`/lyric?id=${id}`)

// 获取歌词
// export const lyricNew = (id: number | string) =>
//   httpGet<LyricsResponse>(`/lyric/new?id=${id}`)
