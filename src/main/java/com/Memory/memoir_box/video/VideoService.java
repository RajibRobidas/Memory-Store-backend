package com.Memory.memoir_box.video;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Service
public class VideoService {

    private final Cloudinary cloudinary;

    public VideoService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadVideo(MultipartFile file) throws IOException {
        try {
            // Basic file validation
            if (file == null || file.isEmpty()) {
                throw new IOException("File is empty");
            }

            // First upload the video
            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                    "folder", "memoryStore_videos",
                    "resource_type", "video",
                    "chunk_size", 100000000 // 100MB
            );

            Map<?, ?> uploadResult = cloudinary.uploader()
                    .upload(file.getBytes(), uploadOptions);

            String publicId = (String) uploadResult.get("public_id");
            String videoUrl = (String) uploadResult.get("secure_url");

            // Then generate thumbnail
            String thumbnailUrl = cloudinary.url()
                    .resourceType("video")
                    .transformation(new Transformation()
                            .width(300)
                            .height(300)
                            .crop("thumb")
                            .fetchFormat("jpg"))
                    .generate(publicId + ".jpg");

            return videoUrl;

        } catch (Exception e) {
            throw new IOException("Failed to upload video: " + e.getMessage(), e);
        }
    }

    public void deleteVideo(String url) throws IOException {
        try {
            String publicId = extractPublicId(url);
            if (publicId == null || publicId.isEmpty()) {
                throw new IOException("Invalid video URL");
            }

            cloudinary.uploader().destroy(publicId,
                    ObjectUtils.asMap(
                            "resource_type", "video",
                            "invalidate", true
                    ));
        } catch (Exception e) {
            throw new IOException("Failed to delete video: " + e.getMessage(), e);
        }
    }

    private String extractPublicId(String url) {
        try {
            // Handle both http and https URLs
            String cleanUrl = url.replace("https://", "").replace("http://", "");
            String path = cleanUrl.substring(cleanUrl.indexOf('/') + 1); // Skip cloud name

            // Remove file extension
            int lastDot = path.lastIndexOf('.');
            if (lastDot > 0) {
                path = path.substring(0, lastDot);
            }

            return path;
        } catch (Exception e) {
            return null;
        }
    }
}