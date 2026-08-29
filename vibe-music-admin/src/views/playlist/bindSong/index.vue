<script setup lang="ts">
import { reactive, ref, toRaw, onMounted } from "vue";
import { message } from "@/utils/message";
import {
  getPlaylistDetail,
  addSongToPlaylist,
  removeSongFromPlaylist,
  getSongList
} from "@/api/system";

defineOptions({
  name: "BindSongForm"
});

const props = defineProps<{
  playlistId?: number;
  playlistTitle?: string;
}>();

/** 歌单内已有的歌曲 */
const boundSongs = ref([]);
const boundLoading = ref(false);
/** 候选歌曲（可搜索、分页） */
const searchForm = reactive({
  pageNum: 1,
  pageSize: 10,
  artistId: null,
  songName: null,
  album: null
});
const candidateSongs = ref([]);
const candidateTotal = ref(0);
const candidateLoading = ref(false);
/** 正在加入/移出的歌曲 id，用于行级 loading */
const addingSongId = ref<number | null>(null);
const removingSongId = ref<number | null>(null);

function isInPlaylist(songId: number) {
  return boundSongs.value.some(song => song.songId === songId);
}

/** 拉取歌单内歌曲列表 */
async function loadBoundSongs() {
  boundLoading.value = true;
  try {
    const res = await getPlaylistDetail(props.playlistId);
    if (res.code === 0 && res.data) {
      boundSongs.value = (res.data as { songs?: Array<any> }).songs || [];
    } else {
      boundSongs.value = [];
      message("获取歌单歌曲失败，" + res.message, { type: "error" });
    }
  } catch (error) {
    console.error("获取歌单歌曲失败：", error);
    boundSongs.value = [];
    message("获取歌单歌曲失败", { type: "error" });
  } finally {
    boundLoading.value = false;
  }
}

/** 搜索候选歌曲 */
async function loadCandidateSongs() {
  candidateLoading.value = true;
  try {
    const res = await getSongList(toRaw(searchForm));
    if (res.code === 0 && res.data && res.data.items) {
      candidateSongs.value = res.data.items;
      candidateTotal.value = res.data.total || 0;
    } else {
      candidateSongs.value = [];
      candidateTotal.value = 0;
    }
  } catch (error) {
    console.error("获取候选歌曲失败：", error);
    candidateSongs.value = [];
    candidateTotal.value = 0;
    message("获取候选歌曲失败", { type: "error" });
  } finally {
    candidateLoading.value = false;
  }
}

function handleSearch() {
  searchForm.pageNum = 1;
  loadCandidateSongs();
}

/** 候选歌曲分页 */
function handleCandidatePageChange(val: number) {
  searchForm.pageNum = val;
  loadCandidateSongs();
}

/** 把歌曲加入歌单 */
async function handleAdd(row) {
  if (isInPlaylist(row.songId)) {
    message(`歌曲「${row.songName}」已在该歌单中`, { type: "warning" });
    return;
  }
  addingSongId.value = row.songId;
  try {
    const res = await addSongToPlaylist(props.playlistId, row.songId);
    if (res.code === 0) {
      message(`已将「${row.songName}」加入歌单「${props.playlistTitle}」`, {
        type: "success"
      });
      loadBoundSongs();
    } else {
      message("添加失败，" + res.message, { type: "error" });
    }
  } catch (error) {
    console.error("添加失败：", error);
    message("添加失败，请重试", { type: "error" });
  } finally {
    addingSongId.value = null;
  }
}

/** 把歌曲移出歌单 */
async function handleRemove(row) {
  removingSongId.value = row.songId;
  try {
    const res = await removeSongFromPlaylist(props.playlistId, row.songId);
    if (res.code === 0) {
      message(`已将「${row.songName}」移出歌单「${props.playlistTitle}」`, {
        type: "success"
      });
      loadBoundSongs();
    } else {
      message("移出失败，" + res.message, { type: "error" });
    }
  } catch (error) {
    console.error("移出失败：", error);
    message("移出失败，请重试", { type: "error" });
  } finally {
    removingSongId.value = null;
  }
}

onMounted(() => {
  loadBoundSongs();
  loadCandidateSongs();
});
</script>

<template>
  <div>
    <el-alert
      :title="`歌单：${playlistTitle}（编号 ${playlistId}）`"
      type="info"
      :closable="false"
      class="mb-3"
    />
    <el-row :gutter="24">
      <!-- 歌单内歌曲 -->
      <el-col :span="12" :xs="24">
        <div class="mb-2 font-medium">歌单内歌曲（{{ boundSongs.length }}）</div>
        <el-table
          v-loading="boundLoading"
          :data="boundSongs"
          height="380"
          size="small"
          border
        >
          <el-table-column label="歌名" prop="songName" min-width="120" />
          <el-table-column label="歌手" prop="artistName" min-width="100" />
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ row }">
              <el-popconfirm
                :title="`是否确认将「${row.songName}」移出歌单？`"
                @confirm="handleRemove(row)"
              >
                <template #reference>
                  <el-button
                    link
                    type="danger"
                    size="small"
                    :loading="removingSongId === row.songId"
                  >
                    移出
                  </el-button>
                </template>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
      <!-- 候选歌曲 -->
      <el-col :span="12" :xs="24" class="mt-3 md:mt-0">
        <div class="mb-2 font-medium">添加歌曲</div>
        <el-form :inline="true" @submit.prevent>
          <el-form-item>
            <el-input
              v-model="searchForm.songName"
              placeholder="请输入歌名"
              clearable
              class="!w-[180px]"
              @keyup.enter="handleSearch"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="candidateLoading" @click="handleSearch">
              搜索
            </el-button>
          </el-form-item>
        </el-form>
        <el-table
          v-loading="candidateLoading"
          :data="candidateSongs"
          height="340"
          size="small"
          border
        >
          <el-table-column label="歌名" prop="songName" min-width="120" />
          <el-table-column label="歌手" prop="artistName" min-width="100" />
          <el-table-column label="操作" width="80" align="center">
            <template #default="{ row }">
              <el-button
                link
                type="primary"
                size="small"
                :disabled="isInPlaylist(row.songId)"
                :loading="addingSongId === row.songId"
                @click="handleAdd(row)"
              >
                {{ isInPlaylist(row.songId) ? "已加入" : "加入" }}
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          class="mt-3"
          background
          layout="total, prev, pager, next"
          :total="candidateTotal"
          :page-size="searchForm.pageSize"
          :current-page="searchForm.pageNum"
          @current-change="handleCandidatePageChange"
        />
      </el-col>
    </el-row>
  </div>
</template>
