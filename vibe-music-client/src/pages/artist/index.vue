<script setup lang="ts">
import { categories } from '@/utils/enum'
import { getAllArtists } from '@/api/system'
import { ElNotification } from 'element-plus'

const router = useRouter()
const artistList = ref([])

const selectedGender = ref('-1')
const selectedArea = ref('-1')

// 分页相关
const currentPage = ref(1)
const pageSize = ref(12)
const total = ref(0)

const state = reactive({
  size: 'default',
  disabled: false,
  background: false,
  layout: 'total, sizes, prev, pager, next, jumper',
  total: 0,
  pageSizes: [12, 24, 36, 48],
})

const searchKeyword = ref('')

// 切换菜单显示
const toggleMenu = (index: number) => {
  categories.value[index].isOpen = !categories.value[index].isOpen
}

// 处理分页大小变化
const handleSizeChange = () => {
  currentPage.value = 1
  handleGetArtistList()
}

// 处理页码变化
const handleCurrentChange = () => {
  handleGetArtistList()
}

const handleSubCategoryClick = (id: string, index: number) => {
  if (index === 0) {
    selectedGender.value = id
  } else {
    selectedArea.value = id
  }
  currentPage.value = 1
  handleGetArtistList()
}

const handleGetArtistList = () => {
  const params = {
    pageNum: currentPage.value,
    pageSize: pageSize.value,
    name: null,
    gender: selectedGender.value === '-1' ? null : categories.value[0].subCategories.find(item => item.id === selectedGender.value)?.value,
    area: selectedArea.value === '-1' ? null : categories.value[1].subCategories.find(item => item.id === selectedArea.value)?.value
  }

  getAllArtists(params).then((res) => {
    if (res.code === 0 && res.data) {
      artistList.value = res.data.items.map(item => ({
        artistId: item.artistId,
        name: item.artistName,
        picUrl: item.avatar,
        alias: []
      }))
      total.value = res.data.total
      state.total = res.data.total
    } else {
      ElNotification({
        type: 'error',
        message: '获取歌手列表失败',
        duration: 2000,
      })
    }
  })
}

const handleSearch = () => {
  const params = {
    pageNum: currentPage.value,
    pageSize: pageSize.value,
    artistName: searchKeyword.value || null,
    gender: selectedGender.value === '-1' ? null : categories.value[0].subCategories.find(item => item.id === selectedGender.value)?.value,
    area: selectedArea.value === '-1' ? null : categories.value[1].subCategories.find(item => item.id === selectedArea.value)?.value
  }

  getAllArtists(params).then((res) => {
    if (res.code === 0 && res.data) {
      artistList.value = res.data.items.map(item => ({
        artistId: item.artistId,
        name: item.artistName,
        picUrl: item.avatar,
        alias: []
      }))
      total.value = res.data.total
      state.total = res.data.total
    } else {
      ElNotification({
        type: 'error',
        message: '获取歌手列表失败',
        duration: 2000,
      })
    }
  })
}

const handleReset = () => {
  searchKeyword.value = ''
  selectedGender.value = '-1'
  selectedArea.value = '-1'
  currentPage.value = 1
  handleGetArtistList()
}

onMounted(() => {
  handleGetArtistList()
})
</script>
<template>
  <div class="flex h-full">
    <div class="w-64 bg-background p-4">
      <div class="flex items-center justify-between mb-4">
        <h2 class="text-lg font-semibold">歌手分类</h2>
        <button @click="handleReset"
          class="inline-flex items-center text-sm text-muted-foreground hover:text-foreground">
          <icon-bx:reset class="mr-1 h-4 w-4" />
          重置
        </button>
      </div>

      <nav>
        <div class="relative">
          <icon-akar-icons:search
            class="lucide lucide-search absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground" />
          <input v-model="searchKeyword" @keyup.enter="handleSearch"
            class="flex h-10 rounded-lg border border-input transform duration-300 bg-background px-3 py-2 text-sm ring-offset-background file:border-0 file:bg-transparent file:text-sm file:font-medium file:text-foreground placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-primary focus-visible:ring-offset-0 pl-10 w-56"
            placeholder="搜索歌手" />
        </div>

        <div class="mb-2 mt-4">
          <button
            class="inline-flex items-center justify-between gap-2 whitespace-nowrap rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 hover:bg-accent hover:text-accent-foreground h-10 px-4 py-2 w-full"
            @click="toggleMenu(0)">
            {{ categories[0].name }}
            <icon-tabler:chevron-right :style="{
              transform: categories[0].isOpen ? 'rotate(90deg)' : 'rotate(0deg)',
              transition: 'transform 0.3s ease',
            }" />
          </button>
          <div v-show="categories[0].isOpen" class="ml-4 mt-1 space-y-1">
            <button v-for="(subCategory, subIndex) in categories[0].subCategories" :key="subIndex"
              @click="handleSubCategoryClick(subCategory.id, 0)"
              class="inline-flex items-center gap-2 whitespace-nowrap text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 hover:text-accent-foreground h-9 rounded-md px-3 w-48 justify-start"
              :class="selectedGender === subCategory.id ? 'bg-activeMenuBg text-accent-foreground' : 'hover:bg-hoverMenuBg text-foreground'">
              {{ subCategory.label }}
            </button>
          </div>
        </div>
        <div class="mb-2">
          <button
            class="inline-flex items-center justify-between gap-2 whitespace-nowrap rounded-md text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 hover:bg-accent hover:text-accent-foreground h-10 px-4 py-2 w-full"
            @click="toggleMenu(1)">
            {{ categories[1].name }}
            <icon-tabler:chevron-right :style="{
              transform: categories[1].isOpen ? 'rotate(90deg)' : 'rotate(0deg)',
              transition: 'transform 0.3s ease',
            }" />
          </button>
          <div v-show="categories[1].isOpen" class="ml-4 mt-1 space-y-1">
            <button v-for="(subCategory, subIndex) in categories[1].subCategories" :key="subIndex"
              @click="handleSubCategoryClick(subCategory.id, 1)"
              class="inline-flex items-center gap-2 whitespace-nowrap text-sm font-medium ring-offset-background transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50 hover:text-accent-foreground h-9 rounded-md px-3 w-48 justify-start"
              :class="selectedArea === subCategory.id ? 'bg-activeMenuBg text-accent-foreground' : 'hover:bg-hoverMenuBg text-foreground'">
              {{ subCategory.label }}
            </button>
          </div>
        </div>
      </nav>
    </div>
    <main class="flex-1">
      <div class="p-2 md:p-4 lg:p-6">
        <div class="w-[86%] mx-auto">
          <div class="grid grid-cols-4 gap-x-16 gap-y-8">
            <div v-for="(artist, index) in artistList" :key="index"
              class="group relative rounded-full text-card-foreground shadow-md hover:shadow-xl">
              <button @click="router.push(`/artist/${artist.artistId}`)" class="w-full h-full overflow-hidden rounded-full">
                <div class="w-full h-full relative">
                  <el-image lazy :alt="artist.name"
                    class="h-full w-full object-cover transition-transform duration-300 ease-out group-hover:scale-110"
                    :src="artist.picUrl + '?param=230y230'" />
                  <div
                    class="absolute inset-0 bg-black/40 opacity-0 transition-opacity duration-300 group-hover:opacity-100">
                  </div>
                  <div
                    class="absolute bottom-0 left-0 right-0 px-4 py-3 text-white opacity-0 transition-opacity duration-300 group-hover:opacity-100 z-10">
                    <h2 class="mb-1 text-xl font-semibold">{{ artist.name }}</h2>
                    <p class="mb-2 text-sm" v-if="artist.alias && artist.alias.length > 0">
                      {{ artist.alias.join() }}
                    </p>
                  </div>
                </div>
              </button>
            </div>
          </div>
        </div>
      </div>
      <!-- 分页 -->
      <nav class="mx-auto flex w-full justify-center mt-6">
        <el-pagination v-model:page-size="pageSize" v-model:currentPage="currentPage" v-bind="state"
          @size-change="handleSizeChange" @current-change="handleCurrentChange" />
      </nav>
    </main>
  </div>
</template>
