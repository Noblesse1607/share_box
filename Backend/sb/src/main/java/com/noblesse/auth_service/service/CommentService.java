package com.noblesse.auth_service.service;

import com.noblesse.auth_service.dto.request.CommentRequest;
import com.noblesse.auth_service.dto.response.CommentResponse;
import com.noblesse.auth_service.entity.Comment;
import com.noblesse.auth_service.entity.Notification;
import com.noblesse.auth_service.entity.Post;
import com.noblesse.auth_service.entity.User;
import com.noblesse.auth_service.exception.AppException;
import com.noblesse.auth_service.exception.ErrorCode;
import com.noblesse.auth_service.repository.CommentRepository;
import com.noblesse.auth_service.repository.NotificationRepository;
import com.noblesse.auth_service.repository.PostRepository;
import com.noblesse.auth_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentService {
    CommentRepository commentRepository;

    PostRepository postRepository;

    UserRepository userRepository;

    NotificationService notificationService;

    NotificationRepository notificationRepository;

    @Transactional
    public CommentResponse createComment(CommentRequest request, Long userId, Long postId){

        Post post = postRepository.findById(postId).orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content(request.getContent())
                .build();

        if (request.getParentCommentId() != null) {
            Comment parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new IllegalArgumentException("Parent Comment không tồn tại"));
            comment.setParentComment(parentComment);
        }

        Comment savedComment = commentRepository.save(comment);
        if (post.getUser().getUserId() != user.getUserId()) {
            Notification notification = Notification.builder()
                    .message(user.getUsername() + " just commented on your post!" + request.getContent())
                    .image(user.getAvatar())
                    .receiverId(post.getUser().getUserId())
                    .commentId(savedComment.getId())
                    .postId(postId)
                    .build();
            notificationRepository.save(notification);
            notificationService.notifyUser(post.getUser().getUserId(), user.getUsername() + " just commented on your post!" + request.getContent(), user.getAvatar());
        }


        return savedComment.toCommentResponse();
    }

    @Transactional
    public List<CommentResponse> getCommentsByPost(Long postId){
        List<Comment> comments = commentRepository.findByPostId(postId);
        return comments.stream().map(Comment::toCommentResponse).collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long commentId){
        commentRepository.deleteById(commentId);
    }

    public Long getTotalComments(Long postId){
        return commentRepository.getTotalComments(postId);
    }

    public List<CommentResponse> getAllParentComment(Long postId){
        List<Comment> comments = commentRepository.findAllParentComments(postId);
        return comments.stream().map(Comment::toCommentResponse).collect(Collectors.toList());
    }

    public List<CommentResponse> getChildComments(Long postId, Long parentCommentId) {
        List<Comment> childComments = commentRepository.findChildCommentsByParentId(postId, parentCommentId);
        return childComments.stream()
                .map(Comment::toCommentResponse).collect(Collectors.toList());
    }
}
