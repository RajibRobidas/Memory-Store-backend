package com.Memory.memoir_box.image;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service
public class ImageService {

    private final Cloudinary cloudinary;

    public ImageService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public String uploadImage(MultipartFile file) throws IOException {
        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(),
                ObjectUtils.asMap(
                        "folder", "memoryStore_img",
                        "resource_type", "auto"
                ));
        return (String) uploadResult.get("secure_url");
    }

    public void deleteImage(String url) throws IOException {
        String publicId = extractPublicId(url);
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
    }

    private String extractPublicId(String url) {
        return url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('.'));
    }
}