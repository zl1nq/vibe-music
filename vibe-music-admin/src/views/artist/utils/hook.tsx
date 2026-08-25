import "./reset.css";
import dayjs from "dayjs";
import editForm from "../form/index.vue";
import { message } from "@/utils/message";
import userAvatar from "@/assets/user.jpg";
import { addDialog } from "@/components/ReDialog";
import type { PaginationProps } from "@pureadmin/table";
import ReCropperPreview from "@/components/ReCropperPreview";
import type { FormItemProps } from "../utils/types";
import { getKeyList, deviceDetection } from "@pureadmin/utils";
import {
  getArtistList,
  addArtist,
  updateArtist,
  updateArtistAvatar,
  deleteArtist,
  deleteArtists
} from "@/api/system";
import { type Ref, h, ref, toRaw, computed, reactive, onMounted } from "vue";

export function useArtist(tableRef: Ref) {
  const form = reactive({
    pageNum: 1,
    pageSize: 10,
    artistName: null,
    area: null,
    gender: null
  });
  const formRef = ref();
  const dataList = ref([]);
  const loading = ref(true);
  // 上传头像信息
  const avatarInfo = ref();
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
      label: "歌手编号",
      prop: "artistId",
      width: 90
    },
    {
      label: "歌手头像",
      prop: "avatar",
      cellRenderer: ({ row }) => (
        <el-image
          fit="cover"
          preview-teleported={true}
          src={row.avatar || userAvatar}
          preview-src-list={Array.of(row.avatar || userAvatar)}
          class="w-[64px] h-[64px] rounded-full align-middle"
        />
      ),
      width: 90
    },
    {
      label: "歌手",
      prop: "artistName",
      minWidth: 130,
      width: 200
    },
    {
      label: "类型",
      prop: "gender",
      width: 50,
      cellRenderer: ({ row, props }) => (
        <el-tag
          size={props.size}
          type={
            row.gender === 0
              ? "primary"
              : row.gender === 1
                ? "danger"
                : row.gender === 2
                  ? "warning"
                  : "info"
          }
          effect="plain"
        >
          {row.gender === 0
            ? "男歌手"
            : row.gender === 1
              ? "女歌手"
              : row.gender === 2
                ? "组合/乐队"
                : "未知"}
        </el-tag>
      )
    },
    {
      label: "生日",
      minWidth: 130,
      width: 130,
      prop: "birth",
      formatter: ({ birth }) => (birth ? dayjs(birth).format("YYYY-MM-DD") : "")
    },
    {
      label: "国籍",
      prop: "area",
      minWidth: 100
    },
    {
      label: "简介",
      prop: "introduction",
      width: 460,
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
    deleteArtist(row.artistId).then(res => {
      if (res.code === 0) {
        message(`您删除了歌手编号为 ${row.artistId} 的这条数据`, {
          type: "success"
        });
        onSearch();
      } else {
        message("删除歌手失败", { type: "error" });
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
    // 用于多选表格，清空歌手的选择
    tableRef.value.getTableRef().clearSelection();
  }

  /** 批量删除 */
  function onbatchDel() {
    // 返回当前选中的行
    const curSelected = tableRef.value.getTableRef().getSelectionRows();
    const ids = getKeyList(curSelected, "artistId");

    deleteArtists(ids)
      .then(res => {
        if (res.code === 0) {
          message(`已删除歌手编号为 ${ids} 的数据`, {
            type: "success"
          });
          tableRef.value.getTableRef().clearSelection();
          onSearch();
        } else {
          message("删除歌手失败", { type: "error" });
        }
      })
      .catch(err => {
        console.error("删除歌手失败", err);
        message("删除歌手失败", { type: "error" });
      });
  }

  async function onSearch() {
    try {
      const result = await getArtistList(toRaw(form)); // 获取完整的返回数据

      if (result.code === 0 && result.data && result.data.items) {
        // 如果返回状态码为 0（成功），且 data 和 items 存在
        pagination.total = result.data.total; // 设置总条目数
        dataList.value = result.data.items.map(item => ({
          artistId: item.artistId,
          artistName: item.artistName,
          gender: item.gender,
          avatar: item.avatar,
          birth: item.birth,
          area: item.area,
          introduction: item.introduction
        }));
      } else {
        // 如果返回状态码不为 0 或 data.items 不存在
        dataList.value = [];
        pagination.total = 0;
        message("未找到匹配的歌手信息", { type: "warning" });
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

  function openDialog(title = "新增", row?: FormItemProps) {
    addDialog({
      title: `${title}歌手`,
      props: {
        formInline: {
          title,
          artistId: row?.artistId ?? "",
          artistName: row?.artistName ?? "",
          gender: row?.gender ?? "",
          birth: row?.birth ?? "",
          area: row?.area ?? "",
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
          message(`您${title}了歌手为 ${curData.artistName} 的这条数据`, {
            type: "success"
          });
          done(); // 关闭弹框
          onSearch(); // 刷新表格数据
        }
        FormRef.validate(valid => {
          if (valid) {
            // 表单规则校验通过
            if (title === "新增") {
              addArtist(curData).then(res => {
                if (res.code === 0) {
                  chores();
                } else {
                  message("新增歌手失败，" + res.message, { type: "error" });
                }
              });
            } else {
              updateArtist(curData).then(res => {
                if (res.code === 0) {
                  chores();
                } else {
                  message("修改歌手失败，" + res.message, { type: "error" });
                }
              });
            }
          }
        });
      }
    });
  }

  const cropRef = ref();
  /** 上传头像 */
  async function handleUpload(row) {
    addDialog({
      title: "裁剪、上传头像",
      width: "40%",
      closeOnClickModal: false,
      fullscreen: deviceDetection(),
      contentRenderer: () =>
        h(ReCropperPreview, {
          ref: cropRef,
          imgSrc: row.avatar || userAvatar,
          onCropper: info => (avatarInfo.value = info)
        }),
      beforeSure: async done => {
        if (!avatarInfo.value?.blob) {
          message("图片裁剪失败，请重试", { type: "error" });
          return;
        }

        // 创建 FormData
        const formData = new FormData();
        formData.append("avatar", avatarInfo.value.blob); // 使用 blob 数据
        formData.append("artistId", row.artistId); // 传递歌手 ID

        try {
          // 调用上传接口
          const res = await updateArtistAvatar(row.artistId, formData);

          if (res.code === 0) {
            message("上传头像成功", { type: "success" });
            done(); // 关闭弹框
            onSearch(); // 刷新表格数据
          } else {
            message("上传头像失败", { type: "error" });
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
