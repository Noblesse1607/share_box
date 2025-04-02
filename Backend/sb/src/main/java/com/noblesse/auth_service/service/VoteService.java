package com.noblesse.auth_service.service;

import com.noblesse.auth_service.dto.response.VoteResponse;
import com.noblesse.auth_service.entity.Notification;
import com.noblesse.auth_service.entity.Post;
import com.noblesse.auth_service.entity.User;
import com.noblesse.auth_service.entity.Vote;
import com.noblesse.auth_service.enums.VoteType;
import com.noblesse.auth_service.exception.AppException;
import com.noblesse.auth_service.exception.ErrorCode;
import com.noblesse.auth_service.repository.NotificationRepository;
import com.noblesse.auth_service.repository.PostRepository;
import com.noblesse.auth_service.repository.UserRepository;
import com.noblesse.auth_service.repository.VoteRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VoteService {
    VoteRepository voteRepository;
    PostRepository postRepository;
    UserRepository userRepository;
    NotificationRepository notificationRepository;
    NotificationService notificationService;

    @Transactional
    public void vote(Long postId, VoteType newVoteType, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Vote existingVote = voteRepository.findByPostIdAndUserId(postId, userId);
        boolean isNewVote = existingVote == null;
        boolean isVoteChanged = existingVote != null && existingVote.getVoteType() != newVoteType;

        if (existingVote != null) {
            if (existingVote.getVoteType() == newVoteType) {
                post.setVoteCount(post.getVoteCount() - newVoteType.getDirection());
                voteRepository.delete(existingVote);
            } else {
                post.setVoteCount(post.getVoteCount()
                        + newVoteType.getDirection()
                        - existingVote.getVoteType().getDirection());

                existingVote.setVoteType(newVoteType);
                voteRepository.save(existingVote);
            }
        } else {
            Vote vote = Vote.builder()
                    .post(post)
                    .user(user)
                    .voteType(newVoteType)
                    .build();

            post.setVoteCount(post.getVoteCount() + newVoteType.getDirection());
            voteRepository.save(vote);
        }

        postRepository.save(post);

        if ((isNewVote || isVoteChanged) && !user.getUserId().equals(post.getUser().getUserId())) {
            String voteTypeText = newVoteType.getDirection() > 0 ? "upvoted" : "downvoted";

            Notification notification = Notification.builder()
                    .message(user.getUsername() + " just " + voteTypeText + " your post!")
                    .image(user.getAvatar())
                    .receiverId(post.getUser().getUserId())
                    .postId(postId)
                    .build();

            notificationRepository.save(notification);

            notificationService.notifyUser(
                    post.getUser().getUserId(),
                    user.getUsername() + " just " + voteTypeText + " your post!",
                    user.getAvatar()
            );
        }
    }

    public VoteResponse findUserVote(Long userId, Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Vote vote = voteRepository.findByPostIdAndUserId(postId, userId);

        if (vote == null) {
            return VoteResponse.builder().voteType(null).build();
        }

        return vote.toVoteResponse();

    }
}
