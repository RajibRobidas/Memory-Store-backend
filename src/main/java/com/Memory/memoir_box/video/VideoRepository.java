package com.Memory.memoir_box.video;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface VideoRepository extends MongoRepository<VideoData, String> {
    List<VideoData> findByUserEmail(String userEmail);
}