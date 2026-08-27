package com.vibemusic.service.impl;

import com.vibemusic.constant.JwtClaimsConstant;
import com.vibemusic.constant.MessageConstant;
import com.vibemusic.enumeration.LikeStatusEnum;
import com.vibemusic.enumeration.RoleEnum;
import com.vibemusic.mapper.*;
import com.vibemusic.model.dto.SongAddDTO;
import com.vibemusic.model.dto.SongAndArtistDTO;
import com.vibemusic.model.dto.SongDTO;
import com.vibemusic.model.dto.SongUpdateDTO;
import com.vibemusic.model.entity.*;
import com.vibemusic.model.vo.SongAdminVO;
import com.vibemusic.model.vo.SongDetailVO;
import com.vibemusic.model.vo.SongVO;
import com.vibemusic.result.PageResult;
import com.vibemusic.result.Result;
import com.vibemusic.service.ISongService;
import com.vibemusic.service.MinioService;
import com.vibemusic.util.JwtUtil;
import com.vibemusic.util.TypeConversionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
@Service
@Slf4j
@CacheConfig(cacheNames = "songCache")
public class SongServiceImpl extends ServiceImpl<SongMapper, Song> implements ISongService {

    @Resource
    private SongMapper songMapper;
    @Resource
    private UserFavoriteMapper userFavoriteMapper;
    @Resource
    private StyleMapper styleMapper;
    @Resource
    private GenreMapper genreMapper;
    @Resource
    private MinioService minioService;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private PlaylistBindingMapper playlistBindingMapper;
    @Resource
    private CommentMapper commentMapper;
    /**
     * 获取所有歌曲
     *
     * @param songDTO songDTO
     * @return 歌曲列表
     */
    @Override
    @Cacheable(key = "#songDTO.pageNum + '-' + #songDTO.pageSize + '-' + #songDTO.songName + '-' + #songDTO.artistName + '-' + #songDTO.album")
    public Result<PageResult<SongVO>> getAllSongs(SongDTO songDTO, HttpServletRequest request) {
        // 获取请求头中的 token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7); // 去掉 "Bearer " 前缀
        }

        Map<String, Object> map = null;
        if (token != null && !token.isEmpty()) {
            map = JwtUtil.parseToken(token);
        }

        // 查询歌曲列表
        Page<SongVO> page = new Page<>(songDTO.getPageNum(), songDTO.getPageSize());
        IPage<SongVO> songPage = songMapper.getSongsWithArtist(page, songDTO.getSongName(), songDTO.getArtistName(), songDTO.getAlbum());
        if (songPage.getRecords().isEmpty()) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }

        // 设置默认状态
        List<SongVO> songVOList = songPage.getRecords().stream()
                .peek(songVO -> songVO.setLikeStatus(LikeStatusEnum.DEFAULT.getId()))
                .toList();

        // 如果 token 解析成功且用户为登录状态，进一步操作
        if (map != null) {
            String role = (String) map.get(JwtClaimsConstant.ROLE);
            if (role.equals(RoleEnum.USER.getRole())) {
                Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
                Long userId = TypeConversionUtil.toLong(userIdObj);

                // 获取用户收藏的歌曲
                List<UserFavorite> favoriteSongs = userFavoriteMapper.selectList(new QueryWrapper<UserFavorite>()
                        .eq("user_id", userId)
                        .eq("type", 0));

                // 获取用户收藏的歌曲 id
                Set<Long> favoriteSongIds = favoriteSongs.stream()
                        .map(UserFavorite::getSongId)
                        .collect(Collectors.toSet());

                // 检查并更新状态
                for (SongVO songVO : songVOList) {
                    if (favoriteSongIds.contains(songVO.getSongId())) {
                        songVO.setLikeStatus(LikeStatusEnum.LIKE.getId());
                    }
                }
            }
        }

        return Result.success(new PageResult<>(songPage.getTotal(), songVOList));
    }

    /**
     * 获取歌手的所有歌曲
     *
     * @param songDTO songAndArtistDTO
     * @return 歌曲列表
     */
    @Override
    @Cacheable(key = "#songDTO.pageNum + '-' + #songDTO.pageSize + '-' + #songDTO.songName + '-' + #songDTO.album + '-' + #songDTO.artistId")
    public Result<PageResult<SongAdminVO>> getAllSongsByArtist(SongAndArtistDTO songDTO) {
        // 分页查询
        Page<SongAdminVO> page = new Page<>(songDTO.getPageNum(), songDTO.getPageSize());
        IPage<SongAdminVO> songPage = songMapper.getSongsWithArtistName(page, songDTO.getArtistId(), songDTO.getSongName(), songDTO.getAlbum());

        if (songPage.getRecords().isEmpty()) {
            return Result.success(MessageConstant.DATA_NOT_FOUND, new PageResult<>(0L, null));
        }

        return Result.success(new PageResult<>(songPage.getTotal(), songPage.getRecords()));
    }

    /**
     * 获取推荐歌曲
     * 推荐歌曲的数量为 20
     *
     * @param request HttpServletRequest，用于获取请求头中的 token
     * @return 推荐歌曲列表
     */
    @Override
    public Result<List<SongVO>> getRecommendedSongs(HttpServletRequest request) {
        // 获取请求头中的 token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);  // 去掉 "Bearer " 前缀
        }

        Map<String, Object> map = null;
        if (token != null && !token.isEmpty()) {
            map = JwtUtil.parseToken(token);
        }

        // 用户未登录，返回随机歌曲列表
        if (map == null) {
            return Result.success(songMapper.getRandomSongsWithArtist());
        }

        // 获取用户 ID
        Long userId = TypeConversionUtil.toLong(map.get(JwtClaimsConstant.USER_ID));

        // 查询用户收藏的歌曲 ID
        List<Long> favoriteSongIds = userFavoriteMapper.getFavoriteSongIdsByUserId(userId);
        if (favoriteSongIds.isEmpty()) {
            return Result.success(songMapper.getRandomSongsWithArtist());
        }

        // 查询用户收藏的歌曲风格并统计频率
        List<Long> favoriteStyleIds = songMapper.getFavoriteSongStyles(favoriteSongIds);
        Map<Long, Long> styleFrequency = favoriteStyleIds.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // 按风格出现次数降序排序
        List<Long> sortedStyleIds = styleFrequency.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 收藏的歌曲均无风格数据时无法按风格推荐，退化为随机歌曲
        if (sortedStyleIds.isEmpty()) {
            log.warn("用户 {} 收藏的歌曲均无风格数据，推荐退化为随机歌曲", userId);
            return Result.success(songMapper.getRandomSongsWithArtist());
        }

        // 从 Redis 获取缓存的推荐列表
        String redisKey = "recommended_songs:" + userId;
        List<SongVO> cachedSongs = redisTemplate.opsForList().range(redisKey, 0, -1);

        // 如果 Redis 没有缓存，则查询数据库并缓存
        if (cachedSongs == null || cachedSongs.isEmpty()) {
            // 根据排序后的风格推荐歌曲（排除已收藏歌曲）
            cachedSongs = songMapper.getRecommendedSongsByStyles(sortedStyleIds, favoriteSongIds, 80);
            // 空列表写入 Redis 会抛异常，仅在非空时缓存
            if (!cachedSongs.isEmpty()) {
                redisTemplate.opsForList().rightPushAll(redisKey, cachedSongs);
                redisTemplate.expire(redisKey, 30, TimeUnit.MINUTES); // 设置过期时间 30 分钟
                log.debug("已缓存用户 {} 的风格推荐歌曲 {} 首", userId, cachedSongs.size());
            } else {
                log.warn("用户 {} 按风格推荐查询结果为空，跳过缓存写入", userId);
            }
        }

        // 随机选取 20 首
        Collections.shuffle(cachedSongs);
        List<SongVO> recommendedSongs = cachedSongs.subList(0, Math.min(20, cachedSongs.size()));

        // 如果推荐的歌曲不足 20 首，则用随机歌曲填充
        if (recommendedSongs.size() < 20) {
            List<SongVO> randomSongs = songMapper.getRandomSongsWithArtist();
            Set<Long> addedSongIds = recommendedSongs.stream().map(SongVO::getSongId).collect(Collectors.toSet());
            for (SongVO song : randomSongs) {
                if (recommendedSongs.size() >= 20){
                    break;
                }
                if (!addedSongIds.contains(song.getSongId())) {
                    recommendedSongs.add(song);
                }
            }
        }

        return Result.success(recommendedSongs);
    }

    /**
     * 获取歌曲详情
     *
     * @param songId  歌曲id
     * @param request HttpServletRequest，用于获取请求头中的 token
     * @return 歌曲详情
     */
    @Override
    @Cacheable(key = "#songId")
    public Result<SongDetailVO> getSongDetail(Long songId, HttpServletRequest request) {
        SongDetailVO songDetailVO = songMapper.getSongDetailById(songId);

        // 获取请求头中的 token
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);  // 去掉 "Bearer " 前缀
        }

        Map<String, Object> map = null;
        if (token != null && !token.isEmpty()) {
            map = JwtUtil.parseToken(token);
        }

        // 如果 token 解析成功且用户为登录状态，进一步操作
        if (map != null) {
            String role = (String) map.get(JwtClaimsConstant.ROLE);
            if (role.equals(RoleEnum.USER.getRole())) {
                Object userIdObj = map.get(JwtClaimsConstant.USER_ID);
                Long userId = TypeConversionUtil.toLong(userIdObj);

                // 获取用户收藏的歌曲
                UserFavorite favoriteSong = userFavoriteMapper.selectOne(new QueryWrapper<UserFavorite>()
                        .eq("user_id", userId)
                        .eq("type", 0)
                        .eq("song_id", songId));
                if (favoriteSong != null) {
                    songDetailVO.setLikeStatus(LikeStatusEnum.LIKE.getId());
                }
            }
        }

        return Result.success(songDetailVO);
    }

    /**
     * 获取所有歌曲的数量
     *
     * @param style 歌曲风格
     * @return 歌曲数量
     */
    @Override
    public Result<Long> getAllSongsCount(String style) {
        QueryWrapper<Song> queryWrapper = new QueryWrapper<>();
        if (style != null) {
            queryWrapper.like("style", style);
        }

        return Result.success(songMapper.selectCount(queryWrapper));
    }

    /**
     * 添加歌曲信息
     *
     * @param songAddDTO 歌曲信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "songCache", allEntries = true)
    public Result addSong(SongAddDTO songAddDTO) {
        if (songAddDTO == null) {
            return Result.error(MessageConstant.ERROR);
        }

        //兜底：判断歌手是否存在，若不存在则返回空列表
        if (songAddDTO.getArtistId() == null || songAddDTO.getArtistId() <= 0L) {
            return Result.error(MessageConstant.SINGER_IS_NULL);
        }
        Song song = new Song();
        BeanUtils.copyProperties(songAddDTO, song);

        // 插入歌曲记录
        if (songMapper.insert(song) == 0) {
            return Result.error(MessageConstant.ADD + MessageConstant.FAILED);
        }

        // 获取刚插入的歌曲记录
        Song songInDB = songMapper.selectOne(new QueryWrapper<Song>()
                .eq("artist_id", songAddDTO.getArtistId())
                .eq("name", songAddDTO.getSongName())
                .eq("album", songAddDTO.getAlbum())
                .orderByDesc("id")
                .last("LIMIT 1"));

        if (songInDB == null) {
            return Result.error(MessageConstant.SONG + MessageConstant.NOT_FOUND);
        }

        Long songId = songInDB.getSongId();

        // 解析风格字段（多个风格以逗号分隔）
        String styleStr = songAddDTO.getStyle();
        if (styleStr != null && !styleStr.isEmpty()) {
            List<String> styles = Arrays.asList(styleStr.split(","));

            // 查询风格 ID
            List<Style> styleList = styleMapper.selectList(new QueryWrapper<Style>().in("name", styles));

            // 插入到 tb_genre
            for (Style style : styleList) {
                Genre genre = new Genre();
                genre.setSongId(songId);
                genre.setStyleId(style.getStyleId());
                genreMapper.insert(genre);
            }
        }

        return Result.success(MessageConstant.ADD + MessageConstant.SUCCESS);
    }

    /**
     * 更新歌曲信息
     *
     * @param songUpdateDTO 歌曲信息
     * @return 结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "songCache", allEntries = true)
    public Result updateSong(SongUpdateDTO songUpdateDTO) {
        // 查询数据库中是否存在该歌曲
        Song songInDB = songMapper.selectById(songUpdateDTO.getSongId());
        if (songInDB == null) {
            return Result.error(MessageConstant.SONG + MessageConstant.NOT_FOUND);
        }

        // 更新歌曲基本信息
        Song song = new Song();
        BeanUtils.copyProperties(songUpdateDTO, song);
        if (songMapper.updateById(song) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        Long songId = songUpdateDTO.getSongId();

        // 删除 tb_genre 中该歌曲的原有风格映射
        genreMapper.delete(new QueryWrapper<Genre>().eq("song_id", songId));

        // 解析新的风格字段（多个风格以逗号分隔）
        String styleStr = songUpdateDTO.getStyle();
        if (styleStr != null && !styleStr.isEmpty()) {
            List<String> styles = Arrays.asList(styleStr.split(","));

            // 查询风格 ID
            List<Style> styleList = styleMapper.selectList(new QueryWrapper<Style>().in("name", styles));

            // 插入新的风格映射到 tb_genre
            for (Style style : styleList) {
                Genre genre = new Genre();
                genre.setSongId(songId);
                genre.setStyleId(style.getStyleId());
                genreMapper.insert(genre);
            }
        }

        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 更新歌曲封面
     *
     * @param songId   歌曲id
     * @param coverUrl 封面url
     * @return 更新结果
     */
    @Override
    @CacheEvict(cacheNames = "songCache", allEntries = true)
    public Result updateSongCover(Long songId, String coverUrl) {
        Song song = songMapper.selectById(songId);
        String cover = song.getCoverUrl();
        if (cover != null && !cover.isEmpty()) {
            minioService.deleteFile(cover);
        }

        song.setCoverUrl(coverUrl);
        if (songMapper.updateById(song) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 更新歌曲音频
     *
     * @param songId   歌曲id
     * @param audioUrl 音频url
     * @return 更新结果
     */
    @Override
    @CacheEvict(cacheNames = "songCache", allEntries = true)
    public Result updateSongAudio(Long songId, String audioUrl, String duration) {

        Song song = songMapper.selectById(songId);
        String audio = song.getAudioUrl();
        if (audio != null && !audio.isEmpty()) {

            minioService.deleteFile(audio);
        }

        song.setAudioUrl(audioUrl).setDuration(duration);
        if (songMapper.updateById(song) == 0) {
            return Result.error(MessageConstant.UPDATE + MessageConstant.FAILED);
        }

        return Result.success(MessageConstant.UPDATE + MessageConstant.SUCCESS);
    }

    /**
     * 删除歌曲
     *
     * @param songId 歌曲id
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "songCache", allEntries = true)
    public Result deleteSong(Long songId) {
        log.info("删除歌曲: {}", songId);

        Song song = songMapper.selectById(songId);
        if (song == null) {
            return Result.error(MessageConstant.SONG + MessageConstant.NOT_FOUND);
        }

        Result DELETE = deleteSongById(songId);
        if (DELETE != null) return DELETE;

        // 删除 MinIO 里的歌曲封面和音频文件
        String cover = song.getCoverUrl();
        String audio = song.getAudioUrl();
        if (cover != null && !cover.isEmpty()) {
            minioService.deleteFile(cover);
        }
        if (audio != null && !audio.isEmpty()) {
            minioService.deleteFile(audio);
        }

        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

    @Nullable
    private Result deleteSongById(Long songId) {
        //  先删关联表
        genreMapper.delete(new QueryWrapper<Genre>().eq("song_id", songId));
        playlistBindingMapper.delete(new QueryWrapper<PlaylistBinding>().eq("song_id", songId));
        userFavoriteMapper.delete(new QueryWrapper<UserFavorite>().eq("song_id", songId));
        commentMapper.delete(new QueryWrapper<Comment>().eq("song_id", songId));
        // 删除数据库song主表中的歌曲信息
        if (songMapper.deleteById(songId) == 0) {
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }
        return null;
    }

    /**
     * 批量删除歌曲
     *
     * @param songIds 歌曲id列表
     * @return 删除结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(cacheNames = "songCache", allEntries = true)
    public Result deleteSongs(List<Long> songIds) {
        // 查询歌曲信息，获取歌曲封面 URL 列表
        List<Song> songs = songMapper.selectByIds(songIds);

        // 删除数据库中的歌曲信息
        genreMapper.deleteByIds(songIds);
        playlistBindingMapper.deleteByIds(songIds);
        userFavoriteMapper.deleteByIds(songIds);
        commentMapper.deleteByIds(songIds);
        if(songMapper.deleteByIds(songIds) == 0){
            return Result.error(MessageConstant.DELETE + MessageConstant.FAILED);
        }

        List<String> coverUrlList = songs.stream()
                .map(Song::getCoverUrl)
                .filter(coverUrl -> coverUrl != null && !coverUrl.isEmpty())
                .toList();
        List<String> audioUrlList = songs.stream()
                .map(Song::getAudioUrl)
                .filter(audioUrl -> audioUrl != null && !audioUrl.isEmpty())
                .toList();

        // 先删除 MinIO 里的歌曲封面和音频文件
        for (String coverUrl : coverUrlList) {
            minioService.deleteFile(coverUrl);
        }
        for (String audioUrl : audioUrlList) {
            minioService.deleteFile(audioUrl);
        }

        return Result.success(MessageConstant.DELETE + MessageConstant.SUCCESS);
    }

}
