import { defineStore } from 'pinia'
import type { Song, PlaylistComment } from '@/api/interface'

interface PlaylistInfo {
  name: string
  description: string
  coverImgUrl: string | null
  creator: {
    nickname: string
    avatarUrl: string
  }
  trackCount: number
  tracks: Song[]
  commentCount: number
  tags: string[]
  comments?: PlaylistComment[]
}

export const usePlaylistStore = defineStore('PlaylistStore', {
  state: () => ({
    playlist: null as PlaylistInfo | null,
    songs: [] as Song[],
  }),
  actions: {
    setPlaylistInfo(info: PlaylistInfo | null) {
      this.playlist = info
    },
    setSongs(songs: Song[]) {
      this.songs = songs
    },
  },
}) 