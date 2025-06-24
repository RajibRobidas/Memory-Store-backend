package com.Memory.memoir_box.controller;

import com.Memory.memoir_box.video.VideoData;
import com.Memory.memoir_box.video.VideoRepository;
import com.Memory.memoir_box.video.VideoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/videos")
@CrossOrigin(origins = "*")
public class VideoController {

    private final VideoService videoService;
    private final VideoRepository videoRepository;

    public VideoController(VideoService videoService, VideoRepository videoRepository) {
        this.videoService = videoService;
        this.videoRepository = videoRepository;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadVideo(
            @RequestParam("video") MultipartFile file,
            @RequestParam String userEmail) {

        Map<String, Object> response = new HashMap<>();

        try {
            // Validate file size (100MB max)
            if (file.getSize() > 100 * 1024 * 1024) {
                response.put("error", "File size exceeds 100MB limit");
                return ResponseEntity.badRequest().body(response);
            }

            // Validate file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("video/")) {
                response.put("error", "Invalid video file type");
                return ResponseEntity.badRequest().body(response);
            }

            // Upload video to Cloudinary
            String videoUrl = videoService.uploadVideo(file);

            // Save metadata to MongoDB
            VideoData videoData = new VideoData();
            videoData.setUrl(videoUrl);
            videoData.setFileName(file.getOriginalFilename());
            videoData.setFileType(contentType);
            videoData.setFileSize(file.getSize());
            videoData.setUploadedAt(new Date());
            videoData.setThumbnailUrl(generateThumbnailUrl(videoUrl));
            videoData.setUserEmail(userEmail);

            videoRepository.save(videoData);

            // Prepare response
            response.put("message", "Video uploaded successfully");
            response.put("video", videoData);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("error", "Upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @GetMapping("/user/{email}")
    public ResponseEntity<List<VideoData>> getVideosByUser(@PathVariable String email) {
        List<VideoData> videos = videoRepository.findByUserEmail(email);
        return ResponseEntity.ok(videos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VideoData> getVideoById(@PathVariable String id) {
        return videoRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable String id) {
        videoRepository.findById(id).ifPresent(video -> {
            try {
                videoService.deleteVideo(video.getUrl());
                videoRepository.delete(video);
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete video: " + e.getMessage());
            }
        });
        return ResponseEntity.noContent().build();
    }

    private String generateThumbnailUrl(String videoUrl) {
        if (videoUrl == null) return null;
        int lastDot = videoUrl.lastIndexOf('.');
        if (lastDot == -1) return null;
        return videoUrl.substring(0, lastDot) + ".jpg";
    }
}