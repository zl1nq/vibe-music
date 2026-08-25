import "./reset.css";
import { message } from "@/utils/message";
import bannerCover from "@/assets/cover.png";
import { usePublicHooks } from "../hooks";
import { addDialog } from "@/components/ReDialog";
import type { PaginationProps } from "@pureadmin/table";
import ReCropper from "@/components/ReCropper";
import { getKeyList, deviceDetection, formatBytes } from "@pureadmin/utils";
import { ElMessageBox, ElSwitch, ElImage } from "element-plus";
import { h, ref, computed, reactive, onMounted, type Ref } from "vue";
import {
  getBannerList,
  addBanner,
  updateBanner,
  updateBannerStatus,
  deleteBanner,
  deleteBanners
} from "@/api/system";

export function useBanner(formData: any, tableRef: Ref) {
  const dataList = ref([]);
  const loading = ref(true);
  const switchLoadMap = ref({});
  const selectionList = ref([]);
  const latestBlob = ref<Blob | null>(null);
  const latestBase64 = ref<string | null>(null);
  const latestInfo = ref<any>(null);
  const { switchStyle } = usePublicHooks();
  const pagination = reactive<PaginationProps>({
    total: 0,
    pageSize: 5,
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
      label: "轮播图编号",
      prop: "bannerId",
      minWidth: 100
    },
    {
      label: "轮播图",
      prop: "bannerUrl",
      minWidth: 400,
      cellRenderer: ({ row }) => (
        <ElImage
          fit="cover"
          preview-teleported={true}
          src={row.bannerUrl}
          preview-src-list={[row.bannerUrl]}
          class="w-[500px] h-[195px] rounded-lg align-middle"
        />
      )
    },
    {
      label: "状态",
      prop: "bannerStatus",
      minWidth: 200,
      cellRenderer: scope => (
        <ElSwitch
          v-model={scope.row.bannerStatus}
          activeValue={0}
          inactiveValue={1}
          style={switchStyle.value}
          onChange={val => onChange(scope.row, scope.$index, val)}
        />
      )
    },
    {
      label: "操作",
      fixed: "right",
      width: 180,
      slot: "operation"
    }
  ];

  function onChange(row, index, newValue) {
    ElMessageBox.confirm(
      `确认要<strong>${
        newValue === 1 ? "禁用" : "启用"
      }</strong><strong style='color:var(--el-color-primary)'> 编号:${
        row.bannerId
      } </strong>的轮播图吗?`,
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
        // 设置 loading 状态
        switchLoadMap.value[index] = {
          ...switchLoadMap.value[index],
          loading: true
        };

        // 调用接口更新状态
        updateBannerStatus(row.bannerId, newValue)
          .then(res => {
            if (res.code === 0) {
              row.bannerStatus = newValue; // 更新前端状态
              message("已成功修改轮播图状态", { type: "success" });
              onSearch(); // 刷新表格数据
            } else {
              message("修改轮播图状态失败，" + res.message, { type: "error" });
              row.bannerStatus = row.bannerStatus === 1 ? 0 : 1; // 还原状态
            }
          })
          .catch(err => {
            console.error("修改轮播图状态失败", err);
            message("修改轮播图状态失败", { type: "error" });
            row.bannerStatus = row.bannerStatus === 1 ? 0 : 1; // 还原状态
          })
          .finally(() => {
            // 关闭 loading 状态
            switchLoadMap.value[index] = {
              ...switchLoadMap.value[index],
              loading: false
            };
          });
      })
      .catch(() => {
        row.bannerStatus = row.bannerStatus === 1 ? 0 : 1; // 还原状态
      });
  }

  function handleDelete(row) {
    deleteBanner(row.bannerId)
      .then(res => {
        if (res.code === 0) {
          message(`删除了 编号 为 ${row.bannerId} 的轮播图`, {
            type: "success"
          });
          onSearch();
        } else {
          message(`删除失败: ${res.message}`, { type: "error" });
        }
      })
      .catch(err => {
        message(`删除失败: ${err}`, { type: "error" });
      });
  }

  function handleSizeChange(val: number) {
    pagination.pageSize = val;
    onSearch();
  }

  function handleCurrentChange(val: number) {
    pagination.currentPage = val;
    onSearch();
  }

  function handleSelectionChange(val) {
    selectionList.value = val;
  }

  function onSelectionCancel() {
    selectionList.value = [];
    tableRef.value?.getTableRef()?.clearSelection();
  }

  function onBatchDelete() {
    if (selectionList.value.length === 0) {
      message("请先选择需要删除的数据", { type: "warning" });
      return;
    }

    ElMessageBox.confirm(
      `确认要删除选中的 <strong>${selectionList.value.length}</strong> 条轮播图吗?`,
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
        const ids = getKeyList(selectionList.value, "bannerId");
        loading.value = true;
        deleteBanners(ids)
          .then(res => {
            if (res.code === 0) {
              message(`成功删除了 ${ids.length} 条数据`, { type: "success" });
              onSearch();
              onSelectionCancel();
            } else {
              message(`批量删除失败: ${res.message}`, { type: "error" });
            }
          })
          .catch(err => {
            message(`批量删除失败: ${err}`, { type: "error" });
          })
          .finally(() => {
            loading.value = false;
          });
      })
      .catch(() => {
        // 用户取消操作
      });
  }

  async function onSearch() {
    loading.value = true;
    const params = { ...formData, ...pagination };
    params.pageNum = params.currentPage;
    delete params.currentPage;
    delete params.total;

    try {
      const result = await getBannerList(params);

      if (result.code === 0 && result.data && result.data.items) {
        // 如果返回状态码为 0（成功），且 data 和 items 存在
        pagination.total = result.data.total; // 设置总条目数
        dataList.value = result.data.items.map(item => ({
          ...item,
          bannerStatus: item.bannerStatus === "ENABLE" ? 0 : 1
        }));
      } else {
        // 如果返回状态码不为 0 或 data.items 不存在
        dataList.value = [];
        pagination.total = 0;
        message("未找到匹配的轮播图信息", { type: "warning" });
      }
    } catch (error) {
      console.error("请求失败：", error);
      message("会话过期，请重新登录", { type: "error" });
    }

    setTimeout(() => {
      loading.value = false;
    }, 500);
  }

  function handleUpload(row?: any) {
    const isEdit = !!row?.bannerId;
    const title = isEdit ? "编辑轮播图" : "新增轮播图";

    addDialog({
      title: title,
      width: "60%",
      draggable: true,
      fullscreen: deviceDetection(),
      closeOnClickModal: false,
      contentRenderer: () =>
        h(
          "div",
          {
            class: "flex justify-between items-start"
          },
          [
            h(
              "div",
              {
                class: "w-[60%] mr-4"
              },
              [
                h(ReCropper, {
                  src: isEdit ? row.bannerUrl : bannerCover,
                  options: {
                    aspectRatio: 1080 / 420,
                    viewMode: 1,
                    dragMode: "move"
                  },
                  onCropper: handleCropperData
                }),
                h(
                  "p",
                  {
                    class: "mt-2 text-center text-gray-500 text-sm"
                  },
                  "温馨提示：右键上方裁剪区可开启功能菜单"
                )
              ]
            ),
            h(
              "div",
              {
                class: "w-[40%] flex flex-col justify-center items-center"
              },
              [
                latestBase64.value
                  ? h(ElImage, {
                      src: latestBase64.value,
                      "preview-src-list": [latestBase64.value],
                      fit: "contain",
                      class: "w-full mb-2 max-h-[200px]"
                    })
                  : h("div", "图片加载中或无效..."),
                latestInfo.value
                  ? h(
                      "div",
                      {
                        class: "mt-2 text-sm text-gray-600"
                      },
                      [
                        h(
                          "p",
                          `图像大小：${parseInt(latestInfo.value.width)} × ${parseInt(latestInfo.value.height)}像素`
                        ),
                        h(
                          "p",
                          `文件大小：${formatBytes(latestInfo.value.size)} (${latestInfo.value.size} 字节)`
                        )
                      ]
                    )
                  : null
              ]
            )
          ]
        ),
      beforeSure: async done => {
        const blob = latestBlob.value;

        if (!blob) {
          message("请先裁剪图片或图片无效", { type: "warning" });
          return;
        }

        const formData = new FormData();
        formData.append("banner", blob, `banner_${Date.now()}.png`);
        loading.value = true;

        try {
          let res;
          if (isEdit) {
            res = await updateBanner(row.bannerId, formData);
          } else {
            res = await addBanner(formData);
          }

          if (res.code === 0) {
            message(isEdit ? "更新成功" : "新增成功", { type: "success" });
            onSearch();
            done();
          } else {
            message(
              isEdit ? `更新失败: ${res.message}` : `新增失败: ${res.message}`,
              { type: "error" }
            );
          }
        } catch (error) {
          message(isEdit ? `更新失败: ${error}` : `新增失败: ${error}`, {
            type: "error"
          });
        } finally {
          loading.value = false;
        }
      },
      closeCallBack: () => {
        latestBlob.value = null;
        latestBase64.value = null;
        latestInfo.value = null;
      }
    });
  }

  function handleCropperData({
    blob,
    base64,
    info
  }: {
    blob: Blob;
    base64: string;
    info: any;
  }) {
    latestBlob.value = blob;
    latestBase64.value = base64;
    latestInfo.value = info;
  }

  onMounted(() => {
    onSearch();
  });

  return {
    loading,
    columns,
    dataList,
    pagination,
    selectionList,
    selectedNum: computed(() => selectionList.value.length),
    onSearch,
    handleSizeChange,
    handleCurrentChange,
    handleSelectionChange,
    handleDelete,
    onBatchDelete,
    handleUpload
  };
}
