package com.Memory.memoir_box.image;

import com.Memory.memoir_box.model.ImageData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ImageRepository extends MongoRepository<ImageData, String> {
    // Add this method to find images by user email
    List<ImageData> findByUserEmail(String userEmail);
}