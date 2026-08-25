import "./reset.css";
import dayjs from "dayjs";
import { message } from "@/utils/message";
import type { PaginationProps } from "@pureadmin/table";
import { deviceDetection } from "@pureadmin/utils";
import { getFeedbackList, deleteFeedback, deleteFeedbacks } from "@/api/system";
import { ElForm, ElMessageBox } from "element-plus";
import { type Ref, ref, toRaw, reactive, onMounted } from "vue";

export function useFeedback(tableRef: Ref) {
  const form = reactive({
    pageNum: 1,
    pageSize: 20,
    keyword: null
  });
  const dataList = ref([]);
  const loading = ref(true);
  const selectedNum = ref(0);
  const pagination = reactive<PaginationProps>({
    total: 0,
    pageSize: 20,
    currentPage: 1,
    background: true
  });
  const columns: TableColumnList = [
    {
      label: "勾选列",
      type: "selection",
      fixed: "left",
      reserveSelection: true
    },
    {
      label: "反馈编号",
      prop: "feedbackId",
      minWidth: 100
    },
    {
      label: "用户编号",
      prop: "userId",
      minWidth: 100
    },
    {
      label: "反馈内容",
      prop: "feedback",
      minWidth: 300,
      width: 700,
      cellRenderer: ({ row }) => {
        const feedback = row.feedback;
        if (!feedback || typeof feedback !== "string") {
          return feedback === null || feedback === undefined ? "" : "无效数据";
        }

        const style = {
          "white-space": "normal",
          "word-break": "break-all"
        };

        return (
          <div style={style} title={feedback}>
            {feedback}
          </div>
        );
      }
    },
    {
      label: "反馈时间",
      minWidth: 200,
      prop: "createTime",
      formatter: ({ createTime }) =>
        dayjs(createTime).format("YYYY-MM-DD HH:mm:ss")
    },
    {
      label: "操作",
      fixed: "right",
      width: 100,
      slot: "operation"
    }
  ];

  function handleDelete(row) {
    deleteFeedback(row.feedbackId)
      .then(res => {
        if (res.code === 0) {
          message(`您删除了反馈编号为 ${row.feedbackId} 的这条数据`, {
            type: "success"
          });
          onSearch();
        } else {
          message("删除反馈失败: " + res.message, { type: "error" });
        }
      })
      .catch(err => {
        message("删除反馈失败", { type: "error" });
        console.error("删除反馈错误:", err);
      });
  }

  function handleSizeChange(val: number) {
    pagination.pageSize = val;
    form.pageSize = val;
    onSearch();
  }

  function handleCurrentChange(val: number) {
    pagination.currentPage = val;
    form.pageNum = val;
    onSearch();
  }

  function handleSelectionChange(val) {
    if (tableRef) {
      selectedNum.value = val.length;
      selectedFeedbackIds.value = val.map(item => item.feedbackId);
    }
  }

  const selectedFeedbackIds = ref<number[]>([]);

  function onSelectionCancel() {
    selectedNum.value = 0;
    selectedFeedbackIds.value = [];
    tableRef.value?.getTableRef()?.clearSelection();
  }

  function onbatchDel() {
    if (selectedFeedbackIds.value.length === 0) {
      message("请先选择需要删除的反馈", { type: "warning" });
      return;
    }
    ElMessageBox.confirm(
      `确认要<strong>批量删除</strong><strong style='color:var(--el-color-primary)'> ${selectedFeedbackIds.value.length} </strong>条反馈吗?`,
      "系统提示",
      {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
        dangerouslyUseHTMLString: true,
        draggable: true
      }
    )
      .then(() => {
        deleteFeedbacks(selectedFeedbackIds.value)
          .then(res => {
            if (res.code === 0) {
              message(`成功删除了 ${selectedFeedbackIds.value.length} 条反馈`, {
                type: "success"
              });
              onSearch();
              onSelectionCancel();
            } else {
              message("批量删除反馈失败: " + res.message, { type: "error" });
            }
          })
          .catch(err => {
            message("批量删除反馈失败", { type: "error" });
            console.error("批量删除反馈错误:", err);
          });
      })
      .catch(() => {});
  }

  async function onSearch() {
    loading.value = true;
    const params = toRaw(form);
    if (params.keyword === "" || params.keyword == null) {
      params.keyword = null;
    }

    try {
      const { data } = await getFeedbackList(params);
      dataList.value = data.items;
      pagination.total = data.total;
    } catch (e) {
      console.error("获取反馈列表错误:", e);
      message("获取反馈列表失败", { type: "error" });
      dataList.value = [];
      pagination.total = 0;
    } finally {
      setTimeout(() => {
        loading.value = false;
      }, 500);
    }
  }

  const resetForm = (formEl: InstanceType<typeof ElForm> | undefined) => {
    if (!formEl) return;
    formEl.resetFields();
    form.keyword = null;
    onSearch();
  };

  onMounted(() => {
    onSearch();
  });

  return {
    form,
    loading,
    columns,
    dataList,
    selectedNum,
    pagination,
    onSearch,
    resetForm,
    onbatchDel,
    handleDelete,
    handleSizeChange,
    handleCurrentChange,
    handleSelectionChange,
    onSelectionCancel,
    deviceDetection
  };
}
