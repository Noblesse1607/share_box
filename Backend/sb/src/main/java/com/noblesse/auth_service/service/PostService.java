package com.noblesse.auth_service.service;

import com.noblesse.auth_service.dto.request.CreatePostRequest;
import com.noblesse.auth_service.dto.request.SearchRequest;
import com.noblesse.auth_service.dto.response.PostResponse;
import com.noblesse.auth_service.entity.Community;
import com.noblesse.auth_service.entity.Post;
import com.noblesse.auth_service.entity.User;
import com.noblesse.auth_service.exception.AppException;
import com.noblesse.auth_service.exception.ErrorCode;
import com.noblesse.auth_service.repository.CommunityRepository;
import com.noblesse.auth_service.repository.PostRepository;
import com.noblesse.auth_service.repository.UserRepository;
import com.noblesse.auth_service.repository.VoteRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostService {

    PostRepository postRepository;

    UserRepository userRepository;

    VoteRepository voteRepository;

    CommunityRepository communityRepository;

    private static final String supabaseUrl = "https://eluflzblngwpnjifvwqo.supabase.co/storage/v1/object/images/";
    private static final String supabaseApiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVsdWZsemJsbmd3cG5qaWZ2d3FvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mjc3OTY3NzMsImV4cCI6MjA0MzM3Mjc3M30.1Xj5Ndd1J6-57JQ4BtEjBTxUqmVNgOhon1BhG1PSz78";

    public PostResponse createPost(CreatePostRequest request, Long userId, Long communityId) throws IOException {

        Community community = communityRepository.findById(communityId).orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!community.getMembers().contains(user)) {
            throw new AppException(ErrorCode.USER_NOT_MEMBER_OF_COMMUNITY);
        }

        Post post = new Post();
        post.setCommunity(community);
        post.setPostTopic(request.getPostTopics());
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        post.setUser(user);

        Post savedPost = postRepository.save(post);

        Long nextPostId = postRepository.countByUser(user);

        List<String> mediaUrls = new ArrayList<>();
        if(request.getMedia() != null && !request.getMedia().isEmpty()) {
            for (MultipartFile file : request.getMedia()) {
                String mediaUrl = uploadPostMedia(file, userId, nextPostId);
                mediaUrls.add(mediaUrl);
            }
        }

        savedPost.setMedia(mediaUrls);
        postRepository.save(savedPost);

        return savedPost.toPostResponse();
    }

    public List<PostResponse> getPostsByCommunity(Long communityId){
        List<Post> posts = postRepository.getPostByCommunity(communityId);
        return posts.stream().map(Post::toPostResponse).collect(Collectors.toList());
    }

    public List<PostResponse> searchPosts(SearchRequest request) {
        List<Post> posts = postRepository.findByNameContainingIgnoreCase(request);
        return posts.stream().map(Post::toPostResponse).collect(Collectors.toList());
    }

    public List<PostResponse> getAllPosts(){
        List<Post> posts = postRepository.findAll();

        return posts.stream().map(Post::toPostResponse).collect(Collectors.toList());
    }

    public String uploadPostMedia(MultipartFile file, Long userId, Long postId) throws  IOException {

        String url = supabaseUrl + "post-media/" + userId + "/" + postId + "/" + file.getOriginalFilename();
        System.out.println(url);
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        headers.set("Authorization", "Bearer " + supabaseApiKey);
        //headers.set("x-upsert", "true");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new InputStreamResource(file.getInputStream()));

        // Tạo HttpEntity với headers và body
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            System.out.println("Response: " + response.getBody());
            return url; // Trả về URL của avatar đã tải lên

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload avatar: " + e.getMessage());
        }
    }

    public List<PostResponse> getAllPostByUserId(Long userId){
        List<Post> posts = postRepository.getAllPost(userId);
        return posts.stream().map(Post::toPostResponse).collect(Collectors.toList());
    }

    public List<PostResponse> getPostByTopic(Long topicId){
        List<Post> postList = postRepository.getPostByDistinctTopic(topicId);
        return postList.stream().map(Post::toPostResponse).collect(Collectors.toList());
    }

    public List<PostResponse> getPostByTopics(List<Long> topicsId){
        List<Post> posts = postRepository.getPostByTopics(topicsId);
        return posts.stream().map(Post::toPostResponse).collect(Collectors.toList());
    }

    public List<PostResponse> getPostByUserTopics(Long userId){

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));


        List<Long> userTopics = userRepository.findUserTopicsId(userId);

        List<Post> posts = postRepository.getPostByTopics(userTopics);

        return posts.stream().map(Post::toPostResponse).collect(Collectors.toList());
    }



    public int getUpvotesForPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        return voteRepository.countUpvotesByPost(post);
    }

    public int getDownvotesForPost(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        return voteRepository.countDownvotesByPost(post);
    }

    public PostResponse getPostById(Long postId){
        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        return post.toPostResponse();
    }

}
