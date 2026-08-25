import "./reset.css";
import editForm from "../form/index.vue";
import { message } from "@/utils/message";
import playlistCover from "@/assets/song.jpg";
import { addDialog } from "@/components/ReDialog";
import type { PaginationProps } from "@pureadmin/table";
import ReCropperPreview from "@/components/ReCropperPreview";
import type { FormItemProps } from "../utils/types";
import { getKeyList, deviceDetection } from "@pureadmin/utils";
import {
  getPlaylistList,
  addPlaylist,
  updatePlaylist,
  updatePlaylistCover,
  deletePlaylist,
  deletePlaylists
} from "@/api/system";
import { type Ref, h, ref, toRaw, computed, reactive, onMounted } from "vue";

export function usePlaylist(tableRef: Ref) {
  const form = reactive({
    pageNum: 1,
    pageSize: 10,
    title: null,
    style: null
  });
  const formRef = ref();
  const dataList = ref([]);
  const loading = ref(true);
  // 上传歌单封面信息
  const coverInfo = ref();
  const selectedNum = ref(0);
  const pagination = reactive<PaginationProps>({
    total: 0,
    pageSize: 10,
    currentPage: 1,
    background: true
  });
  const columns: TableColumnList = [
    {
      label: "勾选列", // 如果需要表格多选，此处label必须设置
      type: "selection",
      fixed: "left",
      reserveSelection: true // 数据刷新后保留选项
    },
    {
      label: "歌单编号",
      prop: "playlistId",
      width: 90
    },
    {
      label: "封面",
      prop: "cover",
      cellRenderer: ({ row }) => (
        <el-image
          fit="cover"
          preview-teleported={true}
          src={row.cover || playlistCover}
          preview-src-list={Array.of(row.cover || playlistCover)}
          class="w-[72px] h-[72px] rounded-lg align-middle"
        />
      ),
      width: 90
    },
    {
      label: "歌单",
      prop: "title",
      minWidth: 300
    },
    {
      label: "风格",
      prop: "style",
      minWidth: 180,
      cellRenderer: ({ row }) => (row.style ? <el-tag>{row.style}</el-tag> : "")
    },
    {
      label: "简介",
      prop: "introduction",
      minWidth: 300,
      width: 420,
      formatter: ({ introduction }) => {
        if (!introduction) {
          return "";
        }
        if (typeof introduction !== "string") {
          return "无效数据";
        }
        const maxChars = 60; // 两行最多60个字符
        if (introduction.length > maxChars) {
          return introduction.slice(0, maxChars) + "...";
        }
        return introduction;
      }
    },
    {
      label: "操作",
      fixed: "right",
      width: 180,
      slot: "operation"
    }
  ];
  const buttonClass = computed(() => {
    return [
      "!h-[20px]",
      "reset-margin",
      "!text-gray-500",
      "dark:!text-white",
      "dark:hover:!text-primary"
    ];
  });

  function handleUpdate(row) {
    console.log(row);
  }

  function handleDelete(row) {
    deletePlaylist(row.playlistId).then(res => {
      if (res.code === 0) {
        message(`您删除了歌单编号为 ${row.playlistId} 的这条数据`, {
          type: "success"
        });
        onSearch();
      } else {
        message("删除歌单失败", { type: "error" });
      }
    });
  }

  function handleSizeChange(val: number) {
    // console.log(`${val} items per page`);
    pagination.pageSize = val; // 更新每页条目数
    form.pageSize = val; // 更新页码参数
    onSearch(); // 重新获取数据
  }

  function handleCurrentChange(val: number) {
    // console.log(`current page: ${val}`);
    pagination.currentPage = val; // 更新当前页码
    form.pageNum = val; // 更新页码参数
    onSearch(); // 重新获取数据
  }

  /** 当CheckBox选择项发生变化时会触发该事件 */
  function handleSelectionChange(val) {
    selectedNum.value = val.length;
    // 重置表格高度
    tableRef.value.setAdaptive();
  }

  /** 取消选择 */
  function onSelectionCancel() {
    selectedNum.value = 0;
    // 用于多选表格，清空歌单的选择
    tableRef.value.getTableRef().clearSelection();
  }

  /** 批量删除 */
  function onbatchDel() {
    // 返回当前选中的行
    const curSelected = tableRef.value.getTableRef().getSelectionRows();
    const ids = getKeyList(curSelected, "playlistId");

    deletePlaylists(ids)
      .then(res => {
        if (res.code === 0) {
          message(`已删除歌单编号为 ${ids} 的数据`, {
            type: "success"
          });
          tableRef.value.getTableRef().clearSelection();
          onSearch();
        } else {
          message("删除歌单失败", { type: "error" });
        }
      })
      .catch(err => {
        console.error("删除歌单失败", err);
        message("删除歌单失败", { type: "error" });
      });
  }

  async function onSearch() {
    try {
      const result = await getPlaylistList(toRaw(form)); // 获取完整的返回数据

      if (result.code === 0 && result.data && result.data.items) {
        // 如果返回状态码为 0（成功），且 data 和 items 存在
        pagination.total = result.data.total; // 设置总条目数
        dataList.value = result.data.items.map(item => ({
          playlistId: item.playlistId,
          title: item.title,
          cover: item.coverUrl,
          style: item.style,
          introduction: item.introduction
        }));
      } else {
        // 如果返回状态码不为 0 或 data.items 不存在
        dataList.value = [];
        pagination.total = 0;
        message("未找到匹配的歌单信息", { type: "warning" });
      }
    } catch (error) {
      console.error("请求失败：", error);
      message("会话过期，请重新登录", { type: "error" });
    }

    setTimeout(() => {
      loading.value = false;
    }, 500);
  }

  const resetForm = formEl => {
    if (!formEl) return;
    formEl.resetFields();
    onSearch();
  };

  function openDialog(formTitle = "新增", row?: FormItemProps) {
    addDialog({
      title: `${formTitle}歌单`,
      props: {
        formInline: {
          formTitle,
          playlistId: row?.playlistId ?? "",
          title: row?.title ?? "",
          style: row?.style ?? "",
          introduction: row?.introduction ?? ""
        }
      },
      width: "46%",
      draggable: true,
      fullscreen: deviceDetection(),
      fullscreenIcon: true,
      closeOnClickModal: false,
      contentRenderer: () => h(editForm, { ref: formRef, formInline: null }),
      beforeSure: (done, { options }) => {
        const FormRef = formRef.value.getRef();
        const curData = options.props.formInline as FormItemProps;
        function chores() {
          message(`您${formTitle}了歌单为 ${curData.title} 的这条数据`, {
            type: "success"
          });
          done(); // 关闭弹框
          onSearch(); // 刷新表格数据
        }
        FormRef.validate(valid => {
          if (valid) {
            // 表单规则校验通过
            if (formTitle === "新增") {
              addPlaylist(curData).then(res => {
                if (res.code === 0) {
                  chores();
                } else {
                  message("新增歌单失败，" + res.message, { type: "error" });
                }
              });
            } else {
              updatePlaylist(curData).then(res => {
                if (res.code === 0) {
                  chores();
                } else {
                  message("修改歌单失败，" + res.message, { type: "error" });
                }
              });
            }
          }
        });
      }
    });
  }

  const cropRef = ref();
  /** 上传歌单封面 */
  async function handleUpload(row) {
    addDialog({
      title: "裁剪、上传歌单封面",
      width: "40%",
      closeOnClickModal: false,
      fullscreen: deviceDetection(),
      contentRenderer: () =>
        h(ReCropperPreview, {
          ref: cropRef,
          imgSrc: row.cover || playlistCover,
          onCropper: info => (coverInfo.value = info)
        }),
      beforeSure: async done => {
        if (!coverInfo.value?.blob) {
          message("图片裁剪失败，请重试", { type: "error" });
          return;
        }

        // 创建 FormData
        const formData = new FormData();
        formData.append("cover", coverInfo.value.blob); // 使用 blob 数据
        formData.append("playlistId", row.playlistId); // 传递歌单 ID

        try {
          // 调用上传接口
          const res = await updatePlaylistCover(row.playlistId, formData);

          if (res.code === 0) {
            message("上传歌单封面成功", { type: "success" });
            done(); // 关闭弹框
            onSearch(); // 刷新表格数据
          } else {
            message("上传歌单封面失败", { type: "error" });
          }
        } catch (error) {
          console.error("上传失败:", error);
          message("上传失败，请重试", { type: "error" });
        }
      },
      closeCallBack: () => cropRef.value.hidePopover()
    });
  }

  onMounted(async () => {
    onSearch();
  });

  return {
    form,
    loading,
    columns,
    dataList,
    selectedNum,
    pagination,
    buttonClass,
    deviceDetection,
    onSearch,
    resetForm,
    onbatchDel,
    openDialog,
    handleUpdate,
    handleDelete,
    handleUpload,
    handleSizeChange,
    onSelectionCancel,
    handleCurrentChange,
    handleSelectionChange
  };
}
