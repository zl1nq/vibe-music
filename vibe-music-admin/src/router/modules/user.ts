export default {
  path: "/user",
  redirect: "/user/index",
  meta: {
    icon: "ri:admin-fill",
    title: "用户管理",
    rank: 1
  },
  children: [
    {
      path: "/user/index",
      name: "UserManagement",
      component: () => import("@/views/user/index.vue"),
      meta: {
        title: "用户管理"
      }
    }
  ]
} satisfies RouteConfigsTable;
