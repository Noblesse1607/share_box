package com.noblesse.auth_service.controller;

import com.noblesse.auth_service.dto.request.AvatarRequest;
import com.noblesse.auth_service.dto.request.BackgroundImgRequest;
import com.noblesse.auth_service.dto.request.CommunityCreateRequest;
import com.noblesse.auth_service.dto.request.SearchRequest;
import com.noblesse.auth_service.dto.response.ApiResponse;
import com.noblesse.auth_service.dto.response.CommunityResponse;
import com.noblesse.auth_service.dto.response.UserResponse;
import com.noblesse.auth_service.entity.Community;
import com.noblesse.auth_service.entity.CommunityRequest;
import com.noblesse.auth_service.entity.User;
import com.noblesse.auth_service.enums.Status;
import com.noblesse.auth_service.exception.AppException;
import com.noblesse.auth_service.exception.ErrorCode;
import com.noblesse.auth_service.repository.CommunityRepository;
import com.noblesse.auth_service.service.CommunityService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommunityController {
    CommunityService communityService;
    CommunityRepository communityRepository;

    @PostMapping("/create/{userId}")
    public ApiResponse<CommunityResponse> createCommunity(@RequestBody CommunityCreateRequest request, @PathVariable Long userId){
        return ApiResponse.<CommunityResponse>builder()
                .result(communityService.createCommunity(request, userId))
                .build();
    }

    @PostMapping("/{communityId}/upload-avatar")
    public ResponseEntity<String> uploadAvatar(@PathVariable Long communityId, @ModelAttribute AvatarRequest request){
        try {
            byte[] avatarData = request.getAvatar().getBytes();
            String avatarUrl = communityService.uploadAvatar(avatarData,communityId,request.getAvatar().getOriginalFilename());

            communityService.savedCommunity(communityId,avatarUrl);

            return ResponseEntity.ok("Community registered successfully with avatar URL: " + avatarUrl);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/{communityId}")
    public ApiResponse<CommunityResponse> getCommunityById(@PathVariable Long communityId){
        return ApiResponse.<CommunityResponse>builder()
                .result(communityService.getCommunityById(communityId))
                .build();
    }

    @GetMapping("/user/{userId}")
    public ApiResponse<List<CommunityResponse>> getUserCommunities(@PathVariable Long userId){
        return ApiResponse.<List<CommunityResponse>>builder()
                .result(communityService.userJoinCommunities(userId))
                .build();
    }

    @PostMapping("/{communityId}/upload-background")
    public ResponseEntity<String> uploadBackground(@PathVariable Long communityId, @ModelAttribute BackgroundImgRequest request){
        try {
            byte[] backgroundData = request.getBackgroundImg().getBytes();
            String backgroundUrl = communityService.uploadBackgroundImg(backgroundData,communityId,request.getBackgroundImg().getOriginalFilename());

            communityService.savedCommunityBackground(communityId,backgroundUrl);

            return ResponseEntity.ok("Community registered successfully with background URL: " + backgroundUrl);
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/search")
    public ApiResponse<List<CommunityResponse>> searchCommunities(@RequestBody SearchRequest request){
        return ApiResponse.<List<CommunityResponse>>builder()
                .result(communityService.searchCommunities(request))
                .build();
    }

    @GetMapping("/all")
    public ApiResponse<List<CommunityResponse>> getAllCommunity(){
        return ApiResponse.<List<CommunityResponse>>builder()
                .result(communityService.getAllCommunity())
                .build();
    }

    @PostMapping("/add/{userId}/{communityId}")
    public void joinCommunity(@PathVariable Long userId,@PathVariable Long communityId){
        communityService.addMemberToCommunity(userId,communityId);
    }

    @PostMapping("/leave/{userId}/{communityId}")
    public void leaveCommunity(@PathVariable Long userId,@PathVariable Long communityId){
        communityService.leaveCommunity(userId,communityId);
    }

    @GetMapping("/members/{communityId}")
    public ApiResponse<List<User>> getAllMembers(@PathVariable Long communityId){
        return ApiResponse.<List<User>>builder()
                .result(communityService.getAllMembers(communityId))
                .build();

    }

    @DeleteMapping("/delete/{communityId}")
    public String deleteCommunity(@PathVariable Long communityId){

        //String currentUserEmail = authentication.getName();

        //log.info("CurrentUserEmail: " + currentUserEmail);

        Community community = communityRepository.findById(communityId).orElseThrow(() -> new AppException(ErrorCode.COMMUNITY_NOT_FOUND));

        community.getMembers().forEach(user -> user.getCommunities().remove(community));
        community.getMembers().clear();

//        if(!community.getOwner().getUserEmail().equals(currentUserEmail)){
//            throw new AppException(ErrorCode.YOU_ARE_NOT_THE_OWNER);
//        }

        communityService.deleteCommunity(communityId);
        return "Delete Community Success!";
    }

    @DeleteMapping("/{communityId}/{postId}")
    public ResponseEntity<?> removePostFromCommunity(@PathVariable Long communityId, @PathVariable Long postId, @RequestParam("userId") Long userId){
        boolean isRemoved = communityService.removePostFromCommunity(communityId,postId,userId);
        if (isRemoved) {
            return ResponseEntity.ok().body("Remove post from community successfully!");
        } else {
            return ResponseEntity.badRequest().body("Failed to remove post from community");
        }
    }

    @PostMapping("/{communityId}/request-join")
    public ResponseEntity<String> requestToJoinCommunity(
            @PathVariable Long communityId,
            @RequestParam Long userId) {
        communityService.requestToJoinCommunity(userId, communityId);
        return ResponseEntity.ok("Join request sent to community owner");
    }

    // API để lấy danh sách yêu cầu tham gia theo community
    @GetMapping("/{communityId}/pending-requests")
    public ResponseEntity<List<UserResponse>> getPendingRequestsByCommunity(
            @PathVariable Long communityId) {
        List<CommunityRequest> requests = communityService.getPendingRequestsByCommunityId(communityId);
        List<UserResponse> userResponses = requests.stream()
                .map(req -> req.getRequester().toUserResponse())
                .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }

    // API để lấy danh sách yêu cầu tham gia theo owner
    @GetMapping("/owner/{ownerId}/pending-requests")
    public ResponseEntity<List<UserResponse>> getPendingRequestsByOwner(
            @PathVariable Long ownerId) {
        List<CommunityRequest> requests = communityService.getPendingRequestsByOwnerId(ownerId);
        List<UserResponse> userResponses = requests.stream()
                .map(req -> req.getRequester().toUserResponse())
                .collect(Collectors.toList());
        return ResponseEntity.ok(userResponses);
    }

    // API để trả lời yêu cầu tham gia
    @PostMapping("/request/{requestId}/respond")
    public ResponseEntity<String> respondToJoinRequest(
            @PathVariable Long requestId,
            @RequestParam Status status,
            @RequestParam Long ownerId) {
        communityService.respondToCommunityRequest(requestId, status, ownerId);
        return ResponseEntity.ok("Request processed successfully");
    }

    // API để hủy yêu cầu tham gia
    @DeleteMapping("/{communityId}/cancel-request")
    public ResponseEntity<String> cancelJoinRequest(
            @PathVariable Long communityId,
            @RequestParam Long userId) {
        communityService.cancelJoinRequest(userId, communityId);
        return ResponseEntity.ok("Join request cancelled");
    }
}
