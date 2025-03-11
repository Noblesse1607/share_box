package com.noblesse.auth_service.controller;

import com.noblesse.auth_service.dto.response.ApiResponse;
import com.noblesse.auth_service.dto.response.VoteResponse;
import com.noblesse.auth_service.enums.VoteType;
import com.noblesse.auth_service.service.VoteService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/vote")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VoteController {

    VoteService voteService;

    @PostMapping("/{userId}/{postId}")
    public void vote(@PathVariable Long userId, @PathVariable Long postId, @RequestParam VoteType voteType){
        voteService.vote(postId, voteType, userId);
    }
    @GetMapping("/type/{userId}/{postId}")
    public ApiResponse<VoteResponse> findUserVoteType(@PathVariable Long userId, @PathVariable Long postId){
        return ApiResponse.<VoteResponse>builder()
                .result(voteService.findUserVote(userId, postId))
                .build();
    }

}
