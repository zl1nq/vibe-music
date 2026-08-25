package com.vibemusic.service;

import com.vibemusic.model.dto.FeedbackDTO;
import com.vibemusic.model.entity.Feedback;
import com.vibemusic.result.PageResult;
import com.vibemusic.result.Result;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
public interface IFeedbackService extends IService<Feedback> {

    // 获取反馈列表
    Result<PageResult<Feedback>> getAllFeedbacks(FeedbackDTO feedbackDTO);

    // 删除反馈
    Result deleteFeedback(Long feedbackId);

    // 批量删除反馈
    Result deleteFeedbacks(List<Long> feedbackIds);

    // 添加反馈
    Result addFeedback(String content);

}
