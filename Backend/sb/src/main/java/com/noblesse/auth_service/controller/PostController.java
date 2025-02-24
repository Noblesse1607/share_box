package com.noblesse.auth_service.controller;

import com.noblesse.auth_service.dto.request.CreatePostRequest;
import com.noblesse.auth_service.dto.response.ApiResponse;
import com.noblesse.auth_service.dto.response.PostResponse;
import com.noblesse.auth_service.service.PostService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

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

}
