package com.noblesse.auth_service.controller;

import com.noblesse.auth_service.dto.response.ApiResponse;
import com.noblesse.auth_service.dto.response.PostResponse;
import com.noblesse.auth_service.service.FavoriteService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FavoriteController {

    FavoriteService favoriteService;

    @PostMapping("/save/{userId}")
    public void savePost(@PathVariable Long userId, @RequestParam Long postId){
        favoriteService.savePost(userId, postId);
    }

    @PostMapping("/unsave/{userId}")
    public void unsavePost(@PathVariable Long userId, @RequestParam Long postId){
        favoriteService.unsavePost(userId, postId);
    }

    @GetMapping("{userId}")
    public ApiResponse<List<PostResponse>> getUserSavePost(@PathVariable Long userId){
        return ApiResponse.<List<PostResponse>>builder()
                .result(favoriteService.getUserSavedPosts(userId))
                .build();
    }

}
