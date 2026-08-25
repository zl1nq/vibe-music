package com.vibemusic.controller;


import com.vibemusic.model.dto.CommentPlaylistDTO;
import com.vibemusic.model.dto.CommentSongDTO;
import com.vibemusic.result.Result;
import com.vibemusic.service.ICommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author sunpingli
 * @since 2025-01-09
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private ICommentService commentService;

    /**
     * 新增歌曲评论
     *
     * @param commentSongDTO 评论信息
     * @return 结果
     */
    @PostMapping("/addSongComment")
    public Result addSongComment(@RequestBody CommentSongDTO commentSongDTO) {
        return commentService.addSongComment(commentSongDTO);
    }

    /**
     * 新增歌单评论
     *
     * @param commentPlaylistDTO 评论信息
     * @return 结果
     */
    @PostMapping("/addPlaylistComment")
    public Result addPlaylistComment(@RequestBody CommentPlaylistDTO commentPlaylistDTO) {
        return commentService.addPlaylistComment(commentPlaylistDTO);
    }

    /**
     * 点赞评论
     *
     * @param commentId 评论id
     * @return 结果
     */
    @PatchMapping("/likeComment/{id}")
    public Result likeComment(@PathVariable("id") Long commentId) {
        return commentService.likeComment(commentId);
    }

    /**
     * 取消点赞评论
     *
     * @param commentId 评论id
     * @return 结果
     */
    @PatchMapping("/cancelLikeComment/{id}")
    public Result cancelLikeComment(@PathVariable("id") Long commentId) {
        return commentService.cancelLikeComment(commentId);
    }

    /**
     * 删除评论
     *
     * @param commentId 评论id
     * @return 结果
     */
    @DeleteMapping("/deleteComment/{id}")
    public Result deleteComment(@PathVariable("id") Long commentId) {
        return commentService.deleteComment(commentId);
    }

}
