package com.noblesse.auth_service.controller;

import com.noblesse.auth_service.dto.request.AddCommunityRequest;
import com.noblesse.auth_service.dto.request.CustomFeedRequest;
import com.noblesse.auth_service.dto.request.SearchRequest;
import com.noblesse.auth_service.dto.response.ApiResponse;
import com.noblesse.auth_service.dto.response.CustomFeedResponse;
import com.noblesse.auth_service.dto.response.PostResponse;
import com.noblesse.auth_service.entity.Community;
import com.noblesse.auth_service.entity.CustomFeed;
import com.noblesse.auth_service.exception.AppException;
import com.noblesse.auth_service.exception.ErrorCode;
import com.noblesse.auth_service.repository.CustomFeedRepository;
import com.noblesse.auth_service.service.CustomFeedService;
import jakarta.persistence.EntityManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/custom-feed")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomFeedController {

    CustomFeedService customFeedService;
    CustomFeedRepository customFeedRepository;
    EntityManager entityManager;

    @PostMapping("/create/{userId}")
    public ApiResponse<CustomFeedResponse> createCustomFeed(@RequestBody CustomFeedRequest request, @PathVariable Long userId){
        return ApiResponse.<CustomFeedResponse>builder()
                .result(customFeedService.createCustomFeed(request,userId))
                .build();
    }

    // "/custom-feed/create/{userId}"

    @PostMapping("/add/{customfeedId}")
    public ApiResponse<CustomFeedResponse> addCommunity(@RequestBody AddCommunityRequest request, @PathVariable Long customfeedId){
        return ApiResponse.<CustomFeedResponse>builder()
                .result(customFeedService.addCommunity(request,customfeedId))
                .build();
    }

    // "/custom-feed/add/{customfeedId}"

    @PostMapping("/remove/{customfeedId}")
    public ApiResponse<CustomFeedResponse> removeCommunity(@RequestBody AddCommunityRequest request,@PathVariable Long customfeedId){
        return ApiResponse.<CustomFeedResponse>builder()
                .result(customFeedService.removeCommunity(customfeedId,request))
                .build();
    }

    @GetMapping("/recent-posts/{feedId}")
    public ApiResponse<List<PostResponse>> getAllPostsForCustomFeeds(@PathVariable Long feedId){
        return ApiResponse.<List<PostResponse>>builder()
                .result(customFeedService.getPostsForCustomFeed(feedId))
                .build();
    }

    @GetMapping("/{feedId}")
    public ApiResponse<CustomFeedResponse> getFeedById(@PathVariable Long feedId){
        return ApiResponse.<CustomFeedResponse>builder()
                .result(customFeedService.getFeedById(feedId))
                .build();
    }

    @GetMapping("/all")
    public ApiResponse<List<CustomFeedResponse>> getAllCustomFeeds(){
        return ApiResponse.<List<CustomFeedResponse>>builder()
                .result(customFeedService.getAllCustomFeed())
                .build();
    }

    @PostMapping("/search-posts/{feedId}")
    public ApiResponse<List<PostResponse>> searchPostFromFeed(@PathVariable Long feedId, @RequestBody SearchRequest request){
        return ApiResponse.<List<PostResponse>>builder()
                .result(customFeedService.searchPostsInCustomFeed(feedId, request))
                .build();
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<CustomFeedResponse>> getUserCustomFeeds(@PathVariable Long userId){
        return ApiResponse.<List<CustomFeedResponse>>builder()
                .result(customFeedService.getUserCustomFeeds(userId))
                .build();
    }

    @DeleteMapping("/delete/{feedId}")
    @Transactional
    public String deleteCustomFeed(@PathVariable Long feedId){

        //String currentUserEmail = authentication.getName();

        //log.info("CurrentUserEmail: " + currentUserEmail);

        CustomFeed customFeed = customFeedRepository.findById(feedId).orElseThrow(() -> new AppException(ErrorCode.CUSTOMFEED_NOT_FOUND));

        entityManager.createNativeQuery(
                "DELETE FROM custom_feed_communities WHERE custom_feed_id = :feedId"
        ).setParameter("feedId", feedId).executeUpdate();

//        if(!customFeed.getOwner().getUserEmail().equals(currentUserEmail)){
//            throw new AppException(ErrorCode.YOU_ARE_NOT_THE_OWNER);
//        }

        customFeedService.deleteFeed(feedId);
        return "Delete Community Success!";
    }

    // "/custom-feed/all"

}
