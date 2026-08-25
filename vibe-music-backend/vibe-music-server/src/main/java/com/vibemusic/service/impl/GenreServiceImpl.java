package com.vibemusic.service.impl;

import com.vibemusic.model.entity.Genre;
import com.vibemusic.mapper.GenreMapper;
import com.vibemusic.service.IGenreService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
@Service
public class GenreServiceImpl extends ServiceImpl<GenreMapper, Genre> implements IGenreService {

}
