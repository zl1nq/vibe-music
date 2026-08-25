export default {
  path: "/banner",
  redirect: "/banner/index",
  meta: {
    icon: "bxs:carousel",
    title: "轮播图管理",
    rank: 6
  },
  children: [
    {
      path: "/banner/index",
      name: "BannerManagement",
      component: () => import("@/views/banner/index.vue"),
      meta: {
        title: "轮播图管理"
      }
    }
  ]
} satisfies RouteConfigsTable;
