export interface PlaylistSong {
    songId: number
    songName: string
    artistName: string
    album: string
    duration: string
    coverUrl: string | null
    audioUrl: string
    likeStatus: number
    releaseTime: string | null
}

export interface PlaylistComment {
    commentId: number
    username: string
    userAvatar: string | null
    content: string
    createTime: string
    likeCount: number
}

export interface PlaylistDetail {
    playlistId: number
    title: string
    coverUrl: string | null
    introduction: string
    songs: PlaylistSong[]
    likeStatus: number
    comments: PlaylistComment[]
    isCollected: boolean
}

// 导出 Song 类型
export interface Song {
    songId: number
    songName: string
    artistName: string
    album: string
    duration: string
    coverUrl: string
    audioUrl: string
    likeStatus: number
    releaseTime: string
}

export interface PlaylistComment {
    commentId: number
    username: string
    userAvatar: string | null
    content: string
    createTime: string
    likeCount: number
}

export interface PlaylistDetail {
    playlistId: number
    title: string
    coverUrl: string | null
    introduction: string
    songs: PlaylistSong[]
    likeStatus: number
    comments: PlaylistComment[]
    isCollected: boolean
}

export interface Comment {
    commentId: number
    username: string
    userAvatar: string | null
    content: string
    createTime: string
    likeCount: number
}

export interface SongDetail {
    songId: number
    songName: string
    artistName: string
    album: string
    lyric: string | null
    duration: string
    coverUrl: string
    audioUrl: string
    releaseTime: string
    likeStatus: boolean | null
    comments: Comment[]
}
