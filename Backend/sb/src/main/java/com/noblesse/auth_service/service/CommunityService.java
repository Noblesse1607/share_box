package com.noblesse.auth_service.service;

import com.noblesse.auth_service.dto.request.CommunityCreateRequest;
import com.noblesse.auth_service.dto.request.SearchRequest;
import com.noblesse.auth_service.dto.response.CommunityResponse;
import com.noblesse.auth_service.entity.*;
import com.noblesse.auth_service.enums.Status;
import com.noblesse.auth_service.exception.AppException;
import com.noblesse.auth_service.exception.ErrorCode;
import com.noblesse.auth_service.repository.*;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommunityService {
    CustomFeedRepository customFeedRepository;
    PostRepository postRepository;
    CommunityRepository communityRepository;
    UserRepository userRepository;
    CommentRepository commentRepository;
    VoteRepository voteRepository;
    VoteCommentRepository voteCommentRepository;
    FavoriteRepository favoriteRepository;
    CommunityRequestRepository communityRequestRepository;
    NotificationService notificationService;
    PostService postService;

    private static final String supabaseUrl = "https://eluflzblngwpnjifvwqo.supabase.co/storage/v1/object/images/";
    private static final String supabaseApiKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVsdWZsemJsbmd3cG5qaWZ2d3FvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3Mjc3OTY3NzMsImV4cCI6MjA0MzM3Mjc3M30.1Xj5Ndd1J6-57JQ4BtEjBTxUqmVNgOhon1BhG1PSz78";


    public CommunityResponse createCommunity(CommunityCreateRequest request, Long userId){

        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Community community = Community.builder()
                .name(request.getName())
                .description(request.getDescription())
                .owner(user)
                .build();

        community.setMembers(new ArrayList<>());
        community.getMembers().add(user);

        if (user.getCommunities() == null) {
            user.setCommunities(new ArrayList<>());
        }
        user.getCommunities().add(community);

        communityRepository.save(community);

        return community.toCommunityResponse();
    }



    private void deleteMediaFromSupabase(String mediaUrl) {
        RestTemplate restTemplate = new RestTemplate();

        // Trích xuất đường dẫn file từ URL
        // URL có định dạng: https://eluflzblngwpnjifvwqo.supabase.co/storage/v1/object/images/xxx
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

    public void requestToJoinCommunity(Long userId, Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Kiểm tra xem người dùng đã là thành viên chưa
        if (community.getMembers().contains(user)) {
            throw new AppException(ErrorCode.USER_ALREADY_MEMBER_OF_COMMUNITY);
        }

        // Kiểm tra xem đã có yêu cầu tham gia đang chờ xử lý chưa
        Optional<CommunityRequest> existingRequest = communityRequestRepository
                .findByRequesterAndCommunityAndStatus(user, community, Status.PENDING);

        if (existingRequest.isPresent()) {
            throw new AppException(ErrorCode.REQUEST_ALREADY_SENT);
        }

        // Tạo yêu cầu tham gia mới
        CommunityRequest request = CommunityRequest.builder()
                .requester(user)
                .community(community)
                .status(Status.PENDING)
                .build();

        communityRequestRepository.save(request);

        // Gửi thông báo cho chủ sở hữu
        notificationService.notifyUser(
                community.getOwner().getUserId(),
                "User " + user.getUsername() + " has requested to join your community: " + community.getName(),
                user.getAvatar()
        );
    }

    public void respondToCommunityRequest(Long requestId, Status status, Long ownerId) {
        CommunityRequest request = communityRequestRepository.findById(requestId)
                .orElseThrow(() -> new AppException(ErrorCode.REQUEST_NOT_FOUND));

        // Xác thực người trả lời có phải là chủ sở hữu không
        if (!request.getCommunity().getOwner().getUserId().equals(ownerId)) {
            throw new AppException(ErrorCode.NOT_COMMUNITY_OWNER);
        }

        // Nếu yêu cầu bị từ chối, cập nhật trạng thái và gửi thông báo
        if (status == Status.REJECTED) {
            request.setStatus(Status.REJECTED);
            communityRequestRepository.save(request);

            notificationService.notifyUser(
                    request.getRequester().getUserId(),
                    "Your request to join " + request.getCommunity().getName() + " has been rejected.",
                    request.getCommunity().getAvatar()
            );
            return;
        }

        // Nếu chấp nhận
        if (status == Status.ACCEPTED) {
            // Thêm người dùng vào cộng đồng
            Community community = request.getCommunity();
            User user = request.getRequester();

            if (community.getMembers() == null) {
                community.setMembers(new ArrayList<>());
            }
            community.getMembers().add(user);
            communityRepository.save(community);

            if (user.getCommunities() == null) {
                user.setCommunities(new ArrayList<>());
            }
            user.getCommunities().add(community);
            userRepository.save(user);

            // Cập nhật trạng thái yêu cầu
            request.setStatus(Status.ACCEPTED);
            communityRequestRepository.save(request);

            // Gửi thông báo cho người yêu cầu
            notificationService.notifyUser(
                    user.getUserId(),
                    "Your request to join " + community.getName() + " has been accepted!",
                    community.getAvatar()
            );
        }
    }

    public List<CommunityRequest> getPendingRequestsByCommunityId(Long communityId) {
        return communityRequestRepository.findPendingRequestsByCommunityId(communityId);
    }

    public List<CommunityRequest> getPendingRequestsByOwnerId(Long ownerId) {
        return communityRequestRepository.findPendingRequestsByOwnerId(ownerId);
    }

    public void cancelJoinRequest(Long userId, Long communityId) {
        Optional<CommunityRequest> request = communityRequestRepository.findByRequester_UserIdAndCommunity_Id(userId, communityId);

        if (request.isPresent() && request.get().getStatus() == Status.PENDING) {
            communityRequestRepository.delete(request.get());

            // Gửi thông báo cho owner
            Community community = request.get().getCommunity();
            notificationService.notifyUser(
                    community.getOwner().getUserId(),
                    "User " + request.get().getRequester().getUsername() + " has cancelled their request to join " + community.getName(),
                    request.get().getRequester().getAvatar()
            );
        } else {
            throw new AppException(ErrorCode.REQUEST_NOT_FOUND);
        }
    }

    public boolean removePostFromCommunity(Long communityId, Long postId, Long userId) {
        // Tìm community theo ID
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_NOT_FOUND));

        // Kiểm tra xem người dùng có phải là chủ sở hữu của community không
        if (!community.getOwner().getUserId().equals(userId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Tìm post trong community
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));

        // Kiểm tra xem post có thuộc community này không
        if (!post.getCommunity().getId().equals(communityId)) {
            throw new AppException(ErrorCode.POST_NOT_FOUND);
        }

        // Gọi hàm deletePost đã có sẵn để xử lý logic xóa hoàn chỉnh
        postService.deletePost(postId);

        return true;
    }

    public String uploadAvatar(byte[] avatarData,Long communityId, String fileName){

        String newFileName = "avatar.jpg";

        String url = supabaseUrl + "community-media/avatar/" + communityId + "/" + newFileName;
        System.out.println(url);
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.IMAGE_JPEG);
        //headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        headers.set("Authorization", "Bearer " + supabaseApiKey);
        headers.set("x-upsert", "true");

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(avatarData, headers);
        try {

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            System.out.println("Response: " + response.getBody());
            return url;

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload avatar: " + e.getMessage());
        }
    }

    public CommunityResponse savedCommunity(Long communityId, String avatarUrl){
        Community community = communityRepository.findById(communityId).orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_NOT_FOUND));
        community.setAvatar(avatarUrl);
        communityRepository.save(community);
        return community.toCommunityResponse();
    }

    public String uploadBackgroundImg(byte[] backgroundData,Long communityId, String fileName){

        String newFileName = "background.jpg";

        String url = supabaseUrl + "community-media/background/" + communityId + "/" + newFileName;
        System.out.println(url);
        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.IMAGE_JPEG);
        //headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        headers.set("Authorization", "Bearer " + supabaseApiKey);
        headers.set("x-upsert", "true");

        HttpEntity<byte[]> requestEntity = new HttpEntity<>(backgroundData, headers);
        try {

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            System.out.println("Response: " + response.getBody());
            return url;

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload avatar: " + e.getMessage());
        }
    }

    public CommunityResponse getCommunityById(Long communityId){
        Community community = communityRepository.findById(communityId).orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_NOT_FOUND));
        return community.toCommunityResponse();
    }

    public List<CommunityResponse> userJoinCommunities(Long userId){
        List<Community> communities = communityRepository.findUserCommunities(userId);
        return communities.stream().map(Community::toCommunityResponse).collect(Collectors.toList());
    }

    public List<CommunityResponse> searchCommunities(SearchRequest request) {
        List<Community> communities = communityRepository.findByNameContainingIgnoreCase(request);
        return communities.stream().map(Community::toCommunityResponse).collect(Collectors.toList());
    }

    public CommunityResponse savedCommunityBackground(Long communityId, String backgroundUrl){
        Community community = communityRepository.findById(communityId).orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_NOT_FOUND));
        community.setBackgroundImg(backgroundUrl);
        communityRepository.save(community);
        return community.toCommunityResponse();
    }

    public List<CommunityResponse> getAllCommunity(){
        List<Community> communities = communityRepository.findAll();
        return communities.stream().map(Community::toCommunityResponse).collect(Collectors.toList());
    }

    public void addMemberToCommunity(Long userId, Long communityId) {
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (community.getMembers().contains(user)) {
            throw new AppException(ErrorCode.USER_ALREADY_MEMBER_OF_COMMUNITY);
        }

        community.getMembers().add(user);
        communityRepository.save(community);

        user.getCommunities().add(community);
        userRepository.save(user);
    }

    public void leaveCommunity(Long userId, Long communityId) {
        Community community = communityRepository.findById(communityId).orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_NOT_FOUND));
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if(community.getMembers().contains(user)) {
            community.getMembers().remove(user);
            communityRepository.save(community);

            user.getCommunities().remove(community);
            userRepository.save(user);
        }
    }

    public List<User> getAllMembers(Long communityId){
        Community community = communityRepository.findById(communityId).orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_NOT_FOUND));
        return community.getMembers();
    }

    public void deleteCommunity(Long communityId){
        Community community = communityRepository.findById(communityId).orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_NOT_FOUND));

        if (community.getAvatar() != null && !community.getAvatar().isEmpty()) {
            deleteMediaFromSupabase(community.getAvatar());
        }

        if (community.getBackgroundImg() != null && !community.getBackgroundImg().isEmpty()) {
            deleteMediaFromSupabase(community.getBackgroundImg());
        }

        List<CustomFeed> feeds = customFeedRepository.findAllByCommunitiesId(communityId);

        for(CustomFeed feed : feeds){
            feed.getCommunities().remove(community);
        }
        customFeedRepository.saveAll(feeds);

        List<Post> posts = postRepository.getPostByCommunity(communityId);

        for (Post post : posts){
            Long postId = post.getId();

            if (post.getMedia() != null && !post.getMedia().isEmpty()) {
                for (String mediaUrl : post.getMedia()) {
                    deleteMediaFromSupabase(mediaUrl);
                }
            }

            voteRepository.deleteByPostId(postId);
            voteCommentRepository.deleteByPostId(postId);
            commentRepository.deleteChildCommentsByPostId(postId);
            commentRepository.deleteParentCommentsByPostId(postId);
            favoriteRepository.deleteByPostId(postId);
            postRepository.deleteById(postId);
        }

        communityRequestRepository.deleteAllByCommunityId(communityId);

        communityRepository.deleteById(communityId);
    }
}
