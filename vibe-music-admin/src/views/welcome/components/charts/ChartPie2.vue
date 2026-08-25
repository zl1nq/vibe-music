<script setup lang="ts">
import { ref, computed, nextTick, watch } from "vue";
import { useDark, useECharts } from "@pureadmin/utils";

const props = defineProps({
  chartData: {
    type: Array as PropType<Array<Record<string, any>>>,
    default: () => []
  }
});

const { isDark } = useDark();

const theme = computed(() => (isDark.value ? "dark" : "light"));

const chartRef = ref();
const { setOptions } = useECharts(chartRef, {
  theme
});

watch(
  () => props.chartData,
  async newVal => {
    await nextTick(); // 确保DOM更新完成后再执行
    setOptions({
      title: {
        // text: "Referer of a Website",
        // subtext: "Fake Data",
        left: "center"
      },
      tooltip: {
        trigger: "item"
      },
      legend: {
        orient: "vertical",
        left: "left"
      },
      series: [
        {
          name: "歌手性别数量",
          type: "pie",
          radius: "75%",
          data: newVal,
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: "rgba(0, 0, 0, 0.5)"
            }
          }
        }
      ]
    });
  },
  {
    deep: true,
    immediate: true
  }
);
</script>

<template>
  <div ref="chartRef" style="width: 100%; height: 240px" />
</template>
