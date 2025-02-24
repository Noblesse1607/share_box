package com.noblesse.auth_service.service;

import com.noblesse.auth_service.dto.request.CreatePostRequest;
import com.noblesse.auth_service.dto.response.PostResponse;
import com.noblesse.auth_service.entity.Post;
import com.noblesse.auth_service.entity.User;
import com.noblesse.auth_service.exception.AppException;
import com.noblesse.auth_service.exception.ErrorCode;
import com.noblesse.auth_service.repository.PostRepository;
import com.noblesse.auth_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
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

    private static final String supabaseUrl = "https://eluflzblngwpnjifvwqo.supabase.co/storage/v1/object/images/";
    private static final String supabaseApiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVsdWZsemJsbmd3cG5qaWZ2d3FvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mjc3OTY3NzMsImV4cCI6MjA0MzM3Mjc3M30.1Xj5Ndd1J6-57JQ4BtEjBTxUqmVNgOhon1BhG1PSz78";

    public PostResponse createPost(CreatePostRequest request, Long userId) throws IOException {
        Post post = new Post();
        post.setPostTopic(request.getPostTopics());
        post.setTitle(request.getTitle());
        post.setContent(request.getContent());

        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        post.setUser(user);

        Post savedPost = postRepository.save(post);

        Long nextPostId = postRepository.countByUser(user);

        List<String> mediaUrls = new ArrayList<>();
        for(MultipartFile file : request.getMedia()){
            String mediaUrl = uploadPostMedia(file, userId, nextPostId);
            mediaUrls.add(mediaUrl);
        }

        savedPost.setMedia(mediaUrls);
        postRepository.save(savedPost);

        return savedPost.toPostResponse();
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

}
