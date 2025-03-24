package com.noblesse.auth_service.service;

import com.noblesse.auth_service.dto.request.AddCommunityRequest;
import com.noblesse.auth_service.dto.request.CustomFeedRequest;
import com.noblesse.auth_service.dto.request.SearchRequest;
import com.noblesse.auth_service.dto.response.CustomFeedResponse;
import com.noblesse.auth_service.dto.response.PostResponse;
import com.noblesse.auth_service.entity.Community;
import com.noblesse.auth_service.entity.CustomFeed;
import com.noblesse.auth_service.entity.Post;
import com.noblesse.auth_service.entity.User;
import com.noblesse.auth_service.exception.AppException;
import com.noblesse.auth_service.exception.ErrorCode;
import com.noblesse.auth_service.repository.CommunityRepository;
import com.noblesse.auth_service.repository.CustomFeedRepository;
import com.noblesse.auth_service.repository.PostRepository;
import com.noblesse.auth_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomFeedService {
    UserRepository userRepository;
    CustomFeedRepository customFeedRepository;
    CommunityRepository communityRepository;
    PostRepository postRepository;

    public CustomFeedResponse createCustomFeed(CustomFeedRequest request, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        CustomFeed customFeed = CustomFeed.builder()
                .owner(user)
                .name(request.getName())
                .description(request.getDescription())
                .build();

        customFeedRepository.save(customFeed);
        return customFeed.toCustomFeedResponse();
    }

    public CustomFeedResponse addCommunity(AddCommunityRequest request, Long customfeedId) {
        CustomFeed customFeed = customFeedRepository.findById(customfeedId).orElseThrow(() -> new AppException(ErrorCode.CUSTOMFEED_NOT_FOUND));

        List<Community> communities = communityRepository.findAllById(request.getCommunityIds());

        if (communities.size() != request.getCommunityIds().size()) {

            throw new AppException(ErrorCode.COMMUNITY_NOT_FOUND);
        }

        for (Community community : communities) {
            if (customFeed.getCommunities().contains(community)) {
                throw new AppException(ErrorCode.COMMUNITY_ALREADY_EXISTS_IN_FEED);
            }
        }

        customFeed.getCommunities().addAll(communities);
        customFeedRepository.save(customFeed);
        return customFeed.toCustomFeedResponse();
    }

    public CustomFeedResponse getFeedById(Long feedId) {
        CustomFeed customFeed = customFeedRepository.findById(feedId).orElseThrow(() -> new AppException(ErrorCode.CUSTOMFEED_NOT_FOUND));

        return customFeed.toCustomFeedResponse();
    }

    public CustomFeedResponse removeCommunity(Long feedId, AddCommunityRequest request) {
        CustomFeed customFeed = customFeedRepository.findById(feedId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMFEED_NOT_FOUND));
        List<Community> communities = communityRepository.findAllById(request.getCommunityIds());

        if (communities.size() != request.getCommunityIds().size()) {

            throw new AppException(ErrorCode.COMMUNITY_NOT_FOUND);
        }

        customFeed.getCommunities().removeAll(communities);
        customFeedRepository.save(customFeed);

        return customFeed.toCustomFeedResponse();
    }

    public List<PostResponse> getPostsForCustomFeed(Long feedId) {
        CustomFeed customFeed = customFeedRepository.findById(feedId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMFEED_NOT_FOUND));

        List<Post> posts = new ArrayList<>();
        for (Community community : customFeed.getCommunities()) {
            posts.addAll(postRepository.getPostByCommunity(community.getId()));
        }
        posts.sort(Comparator.comparing(Post::getCreateAt).reversed());
        return posts.stream().map(Post::toPostResponse).collect(Collectors.toList());
    }

    public List<CustomFeedResponse> getAllCustomFeed() {
        List<CustomFeed> customFeeds = customFeedRepository.findAll();
        return customFeeds.stream().map(CustomFeed::toCustomFeedResponse).collect(Collectors.toList());
    }

    public List<CustomFeedResponse> getUserCustomFeeds(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        List<CustomFeed> customFeeds = customFeedRepository.findByOwner(user);

        return customFeeds.stream().map(CustomFeed::toCustomFeedResponse).collect(Collectors.toList());
    }

    public List<PostResponse> searchPostsInCustomFeed(Long feedId, SearchRequest request) {
        CustomFeed customFeed = customFeedRepository.findById(feedId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMFEED_NOT_FOUND));

        List<Post> posts = new ArrayList<>();
        for (Community community : customFeed.getCommunities()) {
            posts.addAll(postRepository.findByCommunityAndKeyword(community.getId(), request));
        }
        return posts.stream().map(Post::toPostResponse).collect(Collectors.toList());
    }
}
