package com.noblesse.auth_service.controller;

import com.noblesse.auth_service.dto.request.TopicRequest;
import com.noblesse.auth_service.dto.response.ApiResponse;
import com.noblesse.auth_service.entity.Topic;
import com.noblesse.auth_service.service.TopicService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/topic")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TopicController {

    TopicService topicService;
    @PostMapping("/add")
    public ResponseEntity<Topic> addTopic(@RequestBody TopicRequest request){
        return ResponseEntity.ok(topicService.addTopic(request));
    }
    @GetMapping("/topics")
    public ResponseEntity<List<Topic>> getAllTopics(){
        return ResponseEntity.ok(topicService.getAllTopics());
    }
    @PutMapping("/update/{topicId}")
    public ResponseEntity<Topic> updateTopic(@PathVariable Long topicId, @RequestBody TopicRequest request){
        return ResponseEntity.ok(topicService.updateTopic(topicId,request));
    }

    @DeleteMapping("/delete/{topicId}")
    public ApiResponse<String> deleteTopic(@PathVariable Long topicId){
        topicService.deleteTopic(topicId);
        return ApiResponse.<String>builder()
                .result("Delete topic success!")
                .build();
    }

}
