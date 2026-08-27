package com.vibemusic.controller;

import jakarta.validation.Valid;
import com.vibemusic.model.dto.FeedbackDTO;
import com.vibemusic.model.entity.Feedback;
import com.vibemusic.result.PageResult;
import com.vibemusic.result.Result;
import com.vibemusic.service.IFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
@RestController
public class FeedbackController {

    @Autowired
    private IFeedbackService feedbackService;

    /**
     * 获取反馈列表
     *
     * @return 反馈列表
     */
    @PostMapping("/admin/getAllFeedbacks")
    public Result<PageResult<Feedback>> getAllFeedbacks(@Valid @RequestBody FeedbackDTO feedbackDTO) {
        return feedbackService.getAllFeedbacks(feedbackDTO);
    }

    /**
     * 删除反馈
     *
     * @param feedbackId 反馈id
     * @return 结果
     */
    @DeleteMapping("/admin/deleteFeedback/{id}")
    public Result deleteFeedback(@PathVariable("id") Long feedbackId) {
        return feedbackService.deleteFeedback(feedbackId);
    }

    /**
     * 批量删除反馈
     *
     * @param feedbackIds 反馈id列表
     * @return 结果
     */
    @DeleteMapping("/admin/deleteFeedbacks")
    public Result deleteFeedbacks(@RequestBody List<Long> feedbackIds) {
        return feedbackService.deleteFeedbacks(feedbackIds);
    }

    /**
     * 添加反馈
     *
     * @param content 反馈内容
     * @return 结果
     */
    @PostMapping("/feedback/addFeedback")
    public Result addFeedback(@RequestParam(value = "content") String content) {
        return feedbackService.addFeedback(content);
    }

}
