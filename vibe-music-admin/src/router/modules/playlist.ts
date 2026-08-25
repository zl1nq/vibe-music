export default {
  path: "/playlist",
  redirect: "/playlist/index",
  meta: {
    icon: "ri:album-fill",
    title: "歌单管理",
    rank: 4
  },
  children: [
    {
      path: "/playlist/index",
      name: "playlistManagement",
      component: () => import("@/views/playlist/index.vue"),
      meta: {
        title: "歌单管理"
      }
    }
  ]
} satisfies RouteConfigsTable;
