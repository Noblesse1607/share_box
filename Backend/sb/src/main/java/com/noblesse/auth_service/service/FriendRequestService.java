package com.noblesse.auth_service.service;

import com.noblesse.auth_service.dto.response.FriendPendingResponse;
import com.noblesse.auth_service.dto.response.UserResponse;
import com.noblesse.auth_service.entity.ChatRoom;
import com.noblesse.auth_service.entity.FriendRequest;
import com.noblesse.auth_service.entity.Message;
import com.noblesse.auth_service.entity.User;
import com.noblesse.auth_service.enums.ChatRoomStatus;
import com.noblesse.auth_service.enums.MessageType;
import com.noblesse.auth_service.enums.Status;
import com.noblesse.auth_service.repository.ChatroomRepository;
import com.noblesse.auth_service.repository.FriendRequestRepository;
import com.noblesse.auth_service.repository.MessageRepository;
import com.noblesse.auth_service.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FriendRequestService {

    FriendRequestRepository friendRequestRepository;

    UserRepository userRepository;

    NotificationService notificationService;

    ChatroomRepository chatroomRepository;

    MessageRepository messageRepository;

    private static final String supabaseUrl = "https://eluflzblngwpnjifvwqo.supabase.co/storage/v1/object/images/";
    private static final String supabaseApiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVsdWZsemJsbmd3cG5qaWZ2d3FvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mjc3OTY3NzMsImV4cCI6MjA0MzM3Mjc3M30.1Xj5Ndd1J6-57JQ4BtEjBTxUqmVNgOhon1BhG1PSz78";

    public FriendRequest sendFriendRequest(Long requesterId, Long receiverId) {
        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        if (friendRequestRepository.findByRequesterAndStatus(requester, Status.PENDING)
                .stream()
                .anyMatch(request -> request.getReceiver().equals(receiver))) {
            throw new RuntimeException("Friend request already sent");
        }

        FriendRequest friendRequest = FriendRequest.builder()
                .requester(requester)
                .receiver(receiver)
                .status(Status.PENDING)
                .build();

        notificationService.notifyUser(receiver.getUserId(),
                "You have a new friend request from " + requester.getUsername(), requester.getAvatar());

        return friendRequestRepository.save(friendRequest);
    }

    public void respondToRequest(Long requesterId, Long receiverId, Status status) {
        FriendRequest friendRequest = friendRequestRepository.findByRequesterUserIdAndReceiverUserId(requesterId, receiverId)
                .orElseThrow(() -> new RuntimeException("Friend Request not found"));

        if (!friendRequest.getReceiver().getUserId().equals(receiverId)) {
            throw new RuntimeException("Only the receiver can respond to the friend request");
        }

        //friendRequest.setStatus(status);

        String message = status == Status.ACCEPTED
                ? "Your friend request to " + friendRequest.getReceiver().getUsername() + " was accepted!"
                : "Your friend request to " + friendRequest.getReceiver().getUsername() + " was rejected!";

        notificationService.notifyUser(friendRequest.getRequester().getUserId(), message, friendRequest.getReceiver().getAvatar());
        notificationService.notifyFriendReq(friendRequest.getRequester().getUserId(), status.toString());

        if (status == Status.REJECTED) {
            friendRequestRepository.delete(friendRequest);
        } else {
            ChatRoom chatroom = ChatRoom.builder()
                    .user1(friendRequest.getReceiver())
                    .user2(friendRequest.getRequester())
                    .user1Status(ChatRoomStatus.LEAVE)
                    .user2Status(ChatRoomStatus.LEAVE)
                    .build();
            chatroomRepository.save(chatroom);
            friendRequest.setStatus(status);
            friendRequestRepository.save(friendRequest);
        }

    }

    public List<UserResponse> getPendingRequests(Long receiverId) {
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        List<User> users = friendRequestRepository.findPendingReqsByUserId(receiverId);

        return users.stream().map(User::toUserResponse).collect(Collectors.toList());

    }

    public void cancelFriendRequest(Long requesterId, Long receiverId) {
        FriendRequest friendRequest = friendRequestRepository.findByRequesterUserIdAndReceiverUserId(requesterId, receiverId)
                .orElseThrow(() -> new RuntimeException("Friend Request not found"));

        // Kiểm tra xem request có đang ở trạng thái PENDING không
        if (friendRequest.getStatus() != Status.PENDING) {
            throw new RuntimeException("Only pending friend requests can be cancelled");
        }

        // Kiểm tra xem người hủy có phải là người gửi request không
        if (!friendRequest.getRequester().getUserId().equals(requesterId)) {
            throw new RuntimeException("Only the requester can cancel the friend request");
        }

        // Xóa request khỏi database
        friendRequestRepository.delete(friendRequest);

        // Thông báo cho người nhận
        notificationService.notifyUser(
                friendRequest.getReceiver().getUserId(),
                friendRequest.getRequester().getUsername() + " has cancelled their friend request.",
                friendRequest.getRequester().getAvatar()
        );
    }

    public List<UserResponse> getFriends(Long userId) {
        List<User> users = friendRequestRepository.findFriendsByUserId(userId);
        return users.stream().map(User::toUserResponse).collect(Collectors.toList());
    }

    public List<UserResponse> getOnlineFriends(Long userId) {
        List<User> friends = friendRequestRepository.findFriendsByUserId(userId);
        return friends.stream()
                .filter(User::getOnline)
                .map(User::toUserResponse)
                .collect(Collectors.toList());
    }

    @Async
    public void notifyFriendsAboutOnlineStatus(User user) {
        if (user == null) {
            log.error("Cannot notify friends: User is null");
            return;
        }

        try {
            List<User> onlineFriends = Optional
                    .ofNullable(friendRequestRepository.findFriendsByUserId(user.getUserId()))
                    .orElseGet(Collections::emptyList) // Default to empty list if null
                    .stream()
                    .filter(Objects::nonNull) // Filter out any null friends
                    .filter(User::getOnline)
                    .collect(Collectors.toList());

            onlineFriends.forEach(friend -> {
                try {
                    if (friend != null) {
                        notificationService.notifyOnlineUser(
                                friend.getUserId(),
                                user.getUsername() + " is now online");
                    }
                } catch (Exception e) {
                    log.error("Failed to notify friend {} about user {} online status",
                            friend != null ? friend.getUserId() : "unknown",
                            user.getUserId(),
                            e);
                }
            });
        } catch (Exception e) {
            log.error("Error in notifyFriendsAboutOnlineStatus for user {}", user.getUserId(), e);
        }
    }

    private void deleteMediaFromSupabase(String mediaUrl) {
        RestTemplate restTemplate = new RestTemplate();

        // Trích xuất đường dẫn file từ URL
        // URL format: https://eluflzblngwpnjifvwqo.supabase.co/storage/v1/object/images/messages/chatroomId/filename
        String filePath = mediaUrl.replace(supabaseUrl, "");

        // Tạo URL cho yêu cầu DELETE
        String deleteUrl = supabaseUrl + filePath;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseApiKey);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    deleteUrl,
                    HttpMethod.DELETE,
                    requestEntity,
                    String.class
            );

            log.info("Đã xóa media từ Supabase: {} với trạng thái: {}",
                    mediaUrl, response.getStatusCode());
        } catch (Exception e) {
            log.error("Không thể xóa media từ Supabase: {} - Lỗi: {}",
                    mediaUrl, e.getMessage());
            // Chỉ ghi log lỗi và tiếp tục xử lý
        }
    }

    public void unfriend(Long userId, Long friendId) {
        // Kiểm tra xem người dùng có tồn tại không
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Friend not found"));

        // Tìm kiếm quan hệ bạn bè (có thể là 2 chiều - user có thể là requester hoặc receiver)
        Optional<FriendRequest> friendshipOpt = friendRequestRepository.findByRequesterUserIdAndReceiverUserIdAndStatus(
                userId, friendId, Status.ACCEPTED);

        if (!friendshipOpt.isPresent()) {
            friendshipOpt = friendRequestRepository.findByRequesterUserIdAndReceiverUserIdAndStatus(
                    friendId, userId, Status.ACCEPTED);
        }

        FriendRequest friendship = friendshipOpt.orElseThrow(
                () -> new RuntimeException("You are not friends with this user"));

        // Xóa quan hệ bạn bè
        friendRequestRepository.delete(friendship);

        // Tìm và xóa chatroom nếu có
        Optional<ChatRoom> chatRoomOpt = chatroomRepository.findByUser1AndUser2OrUser2AndUser1(user, friend);
        List<Message> messages = messageRepository.findByChatroomId(chatRoomOpt.get().getChatroomId());
        for(Message message : messages){
            if(message.getType() == MessageType.IMAGE || message.getType() == MessageType.VIDEO){
                String mediaUrl = message.getContent();
                if(mediaUrl != null && !mediaUrl.isEmpty()){
                    deleteMediaFromSupabase(mediaUrl);
                }
            }
        }

        messageRepository.deleteAll(messages);
        if (!chatRoomOpt.isPresent()) {
            chatRoomOpt = chatroomRepository.findByUser1AndUser2OrUser2AndUser1(friend, user);
        }

        chatRoomOpt.ifPresent(chatroomRepository::delete);

        // Thông báo cho người bị hủy kết bạn
        notificationService.notifyUser(
                friendId,
                user.getUsername() + " has removed you from their friends list.",
                user.getAvatar()
        );

        log.info("User {} unfriended user {}", userId, friendId);
    }


}
