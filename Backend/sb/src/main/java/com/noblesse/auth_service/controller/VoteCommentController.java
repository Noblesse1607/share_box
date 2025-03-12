package com.noblesse.auth_service.controller;

import com.noblesse.auth_service.dto.response.ApiResponse;
import com.noblesse.auth_service.dto.response.VoteResponse;
import com.noblesse.auth_service.enums.VoteType;
import com.noblesse.auth_service.service.VoteCommentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vote-comment")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VoteCommentController {
    VoteCommentService voteCommentService;

    @PostMapping("/{userId}/{postId}/{commentId}")
    public void vote(@PathVariable Long userId, @PathVariable Long postId, @RequestParam VoteType voteType, @PathVariable Long commentId){
        voteCommentService.vote(postId, voteType, userId, commentId);
    }

    @GetMapping("/type/{userId}/{postId}/{commentId}")
    public ApiResponse<VoteResponse> findUserVoteType(@PathVariable Long userId, @PathVariable Long postId, @PathVariable Long commentId){
        return ApiResponse.<VoteResponse>builder()
                .result(voteCommentService.findUserVote(userId, postId, commentId))
                .build();
    }

    // "/vote-comment/{userId}/{postId}/{commentId}"
    // "/vote-comment/type/{userId}/{postId}/{commentId}"
}
