export default {
  path: "/feedback",
  redirect: "/feedback/index",
  meta: {
    icon: "ri:feedback-fill",
    title: "反馈管理",
    rank: 5
  },
  children: [
    {
      path: "/feedback/index",
      name: "FeedbackManagement",
      component: () => import("@/views/feedback/index.vue"),
      meta: {
        title: "反馈管理"
      }
    }
  ]
} satisfies RouteConfigsTable;
