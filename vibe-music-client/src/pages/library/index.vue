<script setup lang="ts">
import { getAllSongs } from '@/api/system'
import { useLibraryStore } from '@/stores/modules/library'

const route = useRoute()
const libraryStore = useLibraryStore()

const props = defineProps({
    selected: {
        type: String,
        default: '1',
    },
})
const tableData = computed(() => libraryStore.tableData)

const currentPage = ref(1) // 当前页
const pageSize = ref(20) // 每页显示的数量

const state = reactive({
    size: 'default',
    disabled: false,
    background: false,
    layout: 'total, sizes, prev, pager, next, jumper',
    total: 0,
    pageSizes: [20, 30, 50],
})

// 监听分页变化
const handleSizeChange = () => {
    getSongs()
}
// 监听当前页变化
const handleCurrentChange = () => {
    getSongs()
}

const getSongs = () => {
    libraryStore.setTableData(null)
    getAllSongs({
        pageNum: currentPage.value,
        pageSize: pageSize.value,
        songName: route.query.query as string || '',
        artistName: '',
        album: '',
    }).then((res) => {
        if (res.code === 0 && res.data) {
            libraryStore.setTableData(res.data)
            state.total = res.data.total || 0
        }
    })
}

watch(
    () => [route.query.query, props.selected],
    (val) => {
        if (!val[1] || val[1] != '1') return
        getSongs()
    },
    {
        immediate: true,
    }
)
</script>

<template>
    <div class="flex-1 h-full flex flex-col overflow-hidden">
        <Table :data="tableData?.items" class="flex-1 overflow-x-hidden" />
        <nav class="mx-auto flex w-full justify-center mt-3">
            <el-pagination v-model:page-size="pageSize" v-model:currentPage="currentPage" v-bind="state"
                @size-change="handleSizeChange" @current-change="handleCurrentChange" class="mb-3" />
        </nav>
    </div>
</template>