package com.thumbnailapi.api.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.thumbnailapi.model.ThumbnailResponse;
import com.thumbnailapi.service.ImageProcessor;
import com.thumbnailapi.util.Constants;
import com.thumbnailapi.util.LogUtil;

/**
 * REST controller for thumbnail generation API.
 * 
 * Provides endpoints for uploading images and generating thumbnails
 * at preset or custom dimensions.
 */
@RestController
@RequestMapping(Constants.BASE_API_PATH + "/thumbnails")
public class ThumbnailController {

    private static final Logger logger = LogManager.getLogger(ThumbnailController.class);
    private final ImageProcessor imageProcessor;

    public ThumbnailController(ImageProcessor imageProcessor) {
        this.imageProcessor = imageProcessor;
    }

    /**
     * POST endpoint for generating thumbnails from an uploaded image.
     * 
     * @param file the image file to process (multipart form data)
     * @param sizes optional comma-separated list of sizes (e.g., "small,medium,large,500x500")
     *              If not provided, defaults to small, medium, large
     * @return ThumbnailResponse containing generated thumbnail metadata
     * 
     * Example usage:
     * curl -X POST \
     *   -F "file=@image.jpg" \
     *   -F "sizes=small,medium,large,800x600" \
     *   http://localhost:8080/api/v1/thumbnails
     *
     * Response:
     * {
     *   "original_filename": "image.jpg",
     *   "original_format": "JPEG",
     *   "original_width": 2000,
     *   "original_height": 1500,
     *   "original_file_size_bytes": 524288,
     *   "thumbnails": [
     *     {
     *       "size": "small",
     *       "width": 150,
     *       "height": 150,
     *       "format": "JPEG",
     *       "file_size_bytes": 5120,
     *       "timestamp": "2024-01-15T10:30:45.123456",
     *       "processing_time_ms": 45
     *     },
     *     ...
     *   ]
     * }
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ThumbnailResponse> generateThumbnails(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "sizes", required = false) String sizes) {

        logger.info("Received thumbnail generation request for file: {} with sizes: {}",
            LogUtil.sanitizeForLog(file.getOriginalFilename()), sizes != null ? sizes : "default");

        // Process image and generate thumbnails
        ThumbnailResponse response = imageProcessor.processImage(file, sizes);

        logger.debug("Returning thumbnail response with {} thumbnails",
            response.thumbnails().size());

        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint for deployment monitoring.
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("OK");
    }

    /**
     * API information endpoint.
     */
    @GetMapping("/info")
    public ResponseEntity<ApiInfo> info() {
        ApiInfo info = new ApiInfo(
            "Thumbnail API Service",
            "1.0.0",
            "Generate image thumbnails at preset or custom dimensions",
            "POST /api/v1/thumbnails"
        );
        return ResponseEntity.ok(info);
    }

    /**
     * API information response model.
     */
    public record ApiInfo(
        String name,
        String version,
        String description,
        String mainEndpoint
    ) {}
}
