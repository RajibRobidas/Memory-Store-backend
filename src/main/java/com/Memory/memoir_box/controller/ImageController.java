package com.Memory.memoir_box.controller;

import com.Memory.memoir_box.image.ImageRepository;
import com.Memory.memoir_box.model.ImageData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.Memory.memoir_box.image.ImageService;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/images")
public class ImageController {

    private final ImageService imageService;
    private final ImageRepository imageRepository;

    public ImageController(ImageService imageService, ImageRepository imageRepository) {
        this.imageService = imageService;
        this.imageRepository = imageRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(
            @RequestParam("photo") MultipartFile file,
            @RequestHeader("User-Email") String userEmail) {

        try {
            if (userEmail == null || userEmail.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "You must be logged in to upload images"));
            }

            // Upload to Cloudinary
            String imageUrl = imageService.uploadImage(file);

            // Save metadata to MongoDB
            ImageData imageData = new ImageData();
            imageData.setUrl(imageUrl);
            imageData.setFileName(file.getOriginalFilename());
            imageData.setFileType(file.getContentType());
            imageData.setUploadedAt(new Date());
            imageData.setUserEmail(userEmail);

            imageRepository.save(imageData);

            return ResponseEntity.ok().body(
                    Map.of(
                            "message", "Image uploaded successfully",
                            "url", imageUrl,
                            "id", imageData.getId()
                    )
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Upload failed: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllImages(@RequestHeader("User-Email") String userEmail) {
        try {
            if (userEmail == null || userEmail.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "You must be logged in to view images"));
            }

            List<ImageData> images = imageRepository.findByUserEmail(userEmail);
            return ResponseEntity.ok(images);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error fetching images: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteImage(
            @PathVariable String id,
            @RequestHeader("User-Email") String userEmail) {

        try {
            if (userEmail == null || userEmail.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "You must be logged in to delete images"));
            }

            ImageData image = imageRepository.findById(id).orElse(null);
            if (image == null) {
                return ResponseEntity.notFound().build();
            }

            if (!image.getUserEmail().equals(userEmail)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "You can only delete your own images"));
            }

            imageService.deleteImage(image.getUrl());
            imageRepository.delete(image);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Deletion failed: " + e.getMessage()));
        }
    }
}