export default {
  path: "/artist",
  redirect: "/artist/index",
  meta: {
    icon: "ri:mic-fill",
    title: "歌手管理",
    rank: 2
  },
  children: [
    {
      path: "/artist/index",
      name: "ArtistManagement",
      component: () => import("@/views/artist/index.vue"),
      meta: {
        title: "歌手管理"
      }
    }
  ]
} satisfies RouteConfigsTable;
