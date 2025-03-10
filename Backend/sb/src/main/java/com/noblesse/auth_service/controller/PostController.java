package com.noblesse.auth_service.controller;

import com.noblesse.auth_service.dto.request.CreatePostRequest;
import com.noblesse.auth_service.dto.response.ApiResponse;
import com.noblesse.auth_service.dto.response.PostResponse;
import com.noblesse.auth_service.service.PostService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

public class PostController {

    PostService postService;

    @PostMapping("/create-post/{userId}")
    public ApiResponse<PostResponse> createPost(@ModelAttribute CreatePostRequest request, @PathVariable Long userId) throws IOException {
        return ApiResponse.<PostResponse>builder()
                .result(postService.createPost(request, userId))
                .build();
    }

    @GetMapping("/get-post/{userId}")
    public ApiResponse<List<PostResponse>> getAllPostByUserId(@PathVariable Long userId){
        return ApiResponse.<List<PostResponse>>builder()
                .result(postService.getAllPostByUserId(userId))
                .build();
    }

    @GetMapping("/{topicId}")
    public ApiResponse<List<PostResponse>> getPostByTopic(@PathVariable Long topicId){
        return ApiResponse.<List<PostResponse>>builder()
                .result(postService.getPostByTopic(topicId))
                .build();
    }

    @GetMapping("/posts")
    public ApiResponse<List<PostResponse>> getPostByTopics(@RequestBody List<Long> topicsId){
        return ApiResponse.<List<PostResponse>>builder()
                .result(postService.getPostByTopics(topicsId))
                .build();
    }

    @GetMapping("/get/{postId}")
    public ApiResponse<PostResponse> getPostById(@PathVariable Long postId){
        return ApiResponse.<PostResponse>builder()
                .result(postService.getPostById(postId))
                .build();
    }

    @GetMapping("/recommend-posts/{userId}")
    public ApiResponse<List<PostResponse>> getRecommendPosts(@PathVariable Long userId){
        return ApiResponse.<List<PostResponse>>builder()
                .result(postService.getPostByUserTopics(userId))
                .build();
    }

    @GetMapping("/{postId}/upvote")
    public ApiResponse<Integer> getUpVotesCount(@PathVariable Long postId){
        return ApiResponse.<Integer>builder()
                .result(postService.getUpvotesForPost(postId))
                .build();
    }

    @GetMapping("/{postId}/downvote")
    public ApiResponse<Integer> getDownVotesCount(@PathVariable Long postId){
        return ApiResponse.<Integer>builder()
                .result(postService.getDownvotesForPost(postId))
                .build();
    }

}
