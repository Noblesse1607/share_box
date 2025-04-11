package com.noblesse.auth_service.repository;

import com.noblesse.auth_service.entity.Comment;
import com.noblesse.auth_service.entity.Post;
import com.noblesse.auth_service.entity.VoteComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface VoteCommentRepository extends JpaRepository<VoteComment, Long> {
    @Query("SELECT vc FROM VoteComment vc WHERE vc.post.id = :postId AND vc.user.id = :userId AND vc.comment.id = :commentId")
    VoteComment findByPostIdAndUserIdAndCommentId(@Param("postId") Long postId, @Param("userId") Long userId, @Param("commentId") Long commentId);

    @Query("SELECT COUNT(vc) FROM VoteComment vc WHERE vc.post = :post AND vc.comment = :comment AND vc.voteType = 'UPVOTE'")
    int countUpvotesByComment(Post post, Comment comment);

    @Query("SELECT COUNT(vc) FROM VoteComment vc WHERE vc.post = :post AND vc.comment = :comment AND vc.voteType = 'DOWNVOTE'")
    int countDownvotesByComment(Post post, Comment comment);

    @Modifying
    @Transactional
    @Query("DELETE FROM VoteComment vc WHERE vc.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);
}
