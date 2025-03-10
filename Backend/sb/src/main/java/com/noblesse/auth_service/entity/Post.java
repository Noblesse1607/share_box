package com.noblesse.auth_service.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.noblesse.auth_service.dto.response.PostResponse;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String title;

    @ManyToMany
    @JoinTable(
            name = "Post_Topic",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "topic_id")
    )
    @JsonManagedReference
    List<Topic> postTopic;

    List<String> media;

    int voteCount = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Vote> votes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = false)
    User user;

    @CreationTimestamp
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    LocalDateTime createAt;

    String content;

    public PostResponse toPostResponse(){
        PostResponse createPostResponse = new PostResponse();
        createPostResponse.setPostId(id);
        createPostResponse.setUserId(user.getUserId());
        createPostResponse.setUserAvatar(user.getAvatar());
        createPostResponse.setUsername(user.getUsername());
        createPostResponse.setPostTopics(postTopic);
        createPostResponse.setContent(content);
        createPostResponse.setMedia(media);
        createPostResponse.setTitle(title);
        createPostResponse.setVoteCount(voteCount);
        createPostResponse.setCreateAt(createAt);

        return createPostResponse;
    }
}
