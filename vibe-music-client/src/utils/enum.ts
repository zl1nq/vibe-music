// 定义导航菜单的结构
interface Category {
    name: string
    isOpen: boolean
    subCategories: { id: string; label: string; value: string | number }[]
}

// 导航数据
export const categories = ref<Category[]>([
    {
        name: '歌手类型',
        isOpen: true,
        subCategories: [
            { id: '-1', label: '全部', value: null },
            { id: '1', label: '男歌手', value: 0 },
            { id: '2', label: '女歌手', value: 1 },
            { id: '3', label: '组合/乐队', value: 2 },
        ],
    },
    {
        name: '地区类型',
        isOpen: true,
        subCategories: [
            { id: '-1', label: '全部', value: null },
            { id: '1', label: '中国', value: "中国" },
            { id: '2', label: '美国', value: "美国" },
            { id: '3', label: '韩国', value: "韩国" },
            { id: '4', label: '日本', value: "日本" },
            { id: '5', label: '其他', value: null },
        ],
    },
])
