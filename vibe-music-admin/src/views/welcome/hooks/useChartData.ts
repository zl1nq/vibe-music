import {
  getAllUsersCount,
  getAllArtistsCount,
  getAllSongsCount,
  getAllPlaylistsCount
} from "@/api/data";
import { onMounted, ref } from "vue";
import { message } from "@/utils/message";

export default () => {
  /** 总数 */
  const userCount = ref<number>(0);
  const artistCount = ref<number>(0);
  const songCount = ref<number>(0);
  const playlistCount = ref<number>(0);

  /** 歌曲类型 */
  const westernPopCount = ref<number>(0);
  const chinesePopCount = ref<number>(0);
  const cantonesePopCount = ref<number>(0);
  const koreanPopCount = ref<number>(0);
  const classicCount = ref<number>(0);
  const hiphopCount = ref<number>(0);
  const rockCount = ref<number>(0);
  const electronicCount = ref<number>(0);
  const jazzCount = ref<number>(0);
  const lightCount = ref<number>(0);

  /** 歌手地区分布 */
  const countAmerica = ref<number>(0);
  const countChina = ref<number>(0);
  const countKorea = ref<number>(0);
  const countJapan = ref<number>(0);
  const countGermany = ref<number>(0);
  const countBritain = ref<number>(0);

  /** 歌手性别 */
  const maleCount = ref<number>(0);
  const femaleCount = ref<number>(0);

  const songTypes = [
    "欧美流行",
    "华语流行",
    "粤语流行",
    "韩国流行",
    "古典",
    "嘻哈说唱",
    "摇滚",
    "电子",
    "节奏布鲁斯",
    "轻音乐"
  ];

  const artistAreas = ["美国", "中国", "韩国", "日本", "德国", "英国"];

  const artistGenders = [0, 1];

  const fetchData = async () => {
    try {
      // 使用 map 函数生成歌曲类型查询的 Promise 数组
      const songTypePromises = songTypes.map(type => getAllSongsCount(type));
      const artistAreasPromises = artistAreas.map(area =>
        getAllArtistsCount(undefined, area)
      );
      const artistGendersPromises = artistGenders.map(gender =>
        getAllArtistsCount(gender)
      );

      // 使用 Promise.all 并行执行所有 API 请求
      const allResponses = await Promise.all([
        getAllUsersCount(),
        getAllArtistsCount(),
        getAllSongsCount(),
        getAllPlaylistsCount(),
        ...songTypePromises,
        ...artistAreasPromises,
        ...artistGendersPromises
      ]);

      // 手动拆分数据
      const userRes = allResponses[0];
      const artistRes = allResponses[1];
      const songRes = allResponses[2];
      const playlistRes = allResponses[3];

      // 根据 songTypes 长度，切割歌曲类型的统计数据
      const songTypeRes = allResponses.slice(4, 4 + songTypes.length);

      // 根据 artistAreas 长度，切割歌手地区统计数据
      const artistAreaRes = allResponses.slice(
        4 + songTypes.length,
        4 + songTypes.length + artistAreas.length
      );

      // 获取歌手性别统计数据
      const artistGenderRes = allResponses.slice(
        4 + songTypes.length + artistAreas.length
      );

      // 将所有 API 响应放入一个数组
      const responses = [
        userRes,
        artistRes,
        songRes,
        playlistRes,
        ...songTypeRes,
        ...artistAreaRes,
        ...artistGenderRes
      ];

      // 将所有计数器 ref 对象放入一个数组
      const counts = [
        userCount,
        artistCount,
        songCount,
        playlistCount,
        westernPopCount,
        chinesePopCount,
        cantonesePopCount,
        koreanPopCount,
        classicCount,
        hiphopCount,
        rockCount,
        electronicCount,
        jazzCount,
        lightCount,
        countAmerica,
        countChina,
        countKorea,
        countJapan,
        countGermany,
        countBritain,
        maleCount,
        femaleCount
      ];

      let allSuccess = true;

      // 遍历所有 API 响应，并更新计数器
      responses.forEach((response, index) => {
        // 检查响应是否有效
        if (
          !response ||
          response.code !== 0 ||
          isNaN(Number(response.data)) // 确保 response.data 可以转换成数字
        ) {
          allSuccess = false;
          console.error(
            `获取第 ${index + 1} 个数据失败，返回数据错误:`,
            response
          );
          return;
        } else {
          // 更新计数器
          counts[index].value = Number(response.data);
        }
      });

      // 如果所有请求都成功，则记录计数器值，否则重置计数器
      if (!allSuccess) {
        resetCounts();
      }
    } catch (error) {
      console.error("获取数据失败:", error);
      message("会话过期，请重新登录", { type: "error" });
      resetCounts();
    }
  };

  const resetCounts = () => {
    userCount.value = 0;
    artistCount.value = 0;
    songCount.value = 0;
    playlistCount.value = 0;
    westernPopCount.value = 0;
    chinesePopCount.value = 0;
    cantonesePopCount.value = 0;
    koreanPopCount.value = 0;
    classicCount.value = 0;
    hiphopCount.value = 0;
    rockCount.value = 0;
    electronicCount.value = 0;
    jazzCount.value = 0;
    lightCount.value = 0;
    countAmerica.value = 0;
    countChina.value = 0;
    countKorea.value = 0;
    countJapan.value = 0;
    countGermany.value = 0;
    countBritain.value = 0;
    maleCount.value = 0;
    femaleCount.value = 0;
  };

  // const logCounts = () => {
  //   console.log(`用户数量: ${userCount.value}`);
  //   console.log(`歌手数量: ${artistCount.value}`);
  //   console.log(`歌曲数量: ${songCount.value}`);
  //   console.log(`歌单数量: ${playlistCount.value}`);
  // };

  onMounted(fetchData);

  return {
    userCount,
    artistCount,
    songCount,
    playlistCount,
    westernPopCount,
    chinesePopCount,
    cantonesePopCount,
    koreanPopCount,
    classicCount,
    hiphopCount,
    rockCount,
    electronicCount,
    jazzCount,
    lightCount,
    countAmerica,
    countChina,
    countKorea,
    countJapan,
    countGermany,
    countBritain,
    maleCount,
    femaleCount
  };
};
