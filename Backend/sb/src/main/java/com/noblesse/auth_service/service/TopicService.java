package com.noblesse.auth_service.service;

import com.noblesse.auth_service.dto.request.TopicRequest;
import com.noblesse.auth_service.entity.Topic;
import com.noblesse.auth_service.exception.AppException;
import com.noblesse.auth_service.exception.ErrorCode;
import com.noblesse.auth_service.repository.TopicRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TopicService {

    TopicRepository topicRepository;
    public Topic addTopic(TopicRequest topic){
        Topic topic1 = new Topic();
        topic1.setContentTopic(topic.getTopic());
        topicRepository.save(topic1);
        return topic1;
    }
    public List<Topic> getAllTopics() {
        return topicRepository.findAll();
    }
    public Topic updateTopic(Long topicId, TopicRequest request) {
        Topic topic = topicRepository.findById(topicId).orElseThrow(() -> new AppException(ErrorCode.TOPIC_NOT_FOUND));
        topic.setContentTopic(request.getTopic());
        topicRepository.save(topic);
        return topic;
    }

    public void deleteTopic(Long topicId){
        topicRepository.deleteById(topicId);
    }

}
