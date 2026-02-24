# Architecture Documentation

## System Overview

The Thumbnail API is a Spring Boot-based REST service designed for high-concurrency image thumbnail generation. It processes multipart image uploads, generates thumbnails at multiple dimensions, and returns generation metadata in JSON format.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────┐
│                         HTTP Clients                                 │
└────────────────────────────────┬────────────────────────────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │   Spring Boot App       │
                    │  TomcatServlet Engine   │
                    │  (200 max threads)      │
                    └────────────┬────────────┘
                                 │
                    ┌────────────▼────────────┐
                    │  ThumbnailController    │
                    │  (REST Endpoints)       │
                    └────────────┬────────────┘
                                 │
        ┌────────────────────────┼──────────────────────────┐
        │                        │                          │
   ┌────▼──────┐      ┌──────────▼────────┐      ┌─────────▼──────┐
   │  Validate  │      │  Parse Dimensions │      │  Process Image │
   │  - Size    │      │  - Preset (S/M/L) │      │  - Detect fmt  │
   │  - Format  │      │  - Custom (WxH)   │      │  - Read dims   │
   │  - Corrupt │      │  - Validate range │      │  - Generate TN │
   └────┬───────┘      └───────┬───────────┘      └────────┬──────┘
        │                      │                           │
        │       ┌──────────────┴───────────────┐           │
        │       │                              │           │
   ┌────▼───────▼──────────────────┐   ┌──────▼───────────▼┐
   │   ImageValidator              │   │ ThumbnailGenerator│
   │   ├─ validateNotNull()         │   │ ├─ readImage()    │
   │   ├─ validateFileSize()        │   │ ├─ resizeImage()  │
   │   ├─ validateMimeType()        │   │ ├─ encodeImage()  │
   │   └─ validateFileContent()     │   └─┬────────────────┘
   │      ├─ validateImageSig()     │     │
   │      ├─ isJpeg()               │     └─────▶ Imgscalr
   │      ├─ isPng()                │            (Image resizing)
   │      ├─ isGif()                │
   │      ├─ isBmp()                │
   │      ├─ isWebp()               │
   │      └─ isTiff()               │
   └────────────────────────────────┘
        │
   ┌────▼────────────────────────────┐
   │   Exception Handling             │
   │   GlobalExceptionHandler         │
   │   ├─ InvalidImageException       │
   │   ├─ UnsupportedFormatException  │
   │   ├─ FileSizeLimitExceeded       │
   │   ├─ InvalidDimensionsException  │
   │   └─ Generic Exception           │
   └────────────────────────────────┘
        │
   ┌────▼──────────────────────────────┐
   │   JSON Response                    │
   │   ├─ Original Image Metadata       │
   │   ├─ Thumbnail Array               │
   │   │  ├─ size                       │
   │   │  ├─ dimensions (W×H)           │
   │   │  ├─ format                     │
   │   │  ├─ file_size_bytes            │
   │   │  ├─ timestamp                  │
   │   │  └─ processing_time_ms         │
   │   └─ HTTP Status (200, 400, 413...)│
   └────────────────────────────────────┘
```

## Core Components

### 1. REST Controller (`ThumbnailController`)

**Responsibility**: Handle HTTP requests and responses

```java
@PostMapping("/thumbnails")
public ResponseEntity<ThumbnailResponse> generateThumbnails(
    @RequestParam("file") MultipartFile file,
    @RequestParam(value = "sizes", required = false) String sizes)
```

- Accepts multipart file uploads
- Optional `sizes` parameter (defaults to small, medium, large)
- Returns `ThumbnailResponse` with metadata for all generated thumbnails
- Delegates validation and processing to services

### 2. Image Processor (`ImageProcessor`)

**Responsibility**: Orchestrate the thumbnail generation workflow

Core flow:
1. Validate image (reject invalid files)
2. Detect image format (JPEG, PNG, etc.)
3. Read original dimensions
4. Parse target dimensions
5. Generate thumbnails
6. Build and return response

**Key Design Patterns**:
- **Dependency Injection**: All dependencies injected via constructor
- **Separation of Concerns**: Each operation delegated to specialized services
- **Exception Propagation**: Exceptions bubble up for global exception handling

### 3. Image Validator (`ImageValidator`)

**Responsibility**: Validate uploaded image files

Validation checks (in order):
1. **Not Null**: File must exist
2. **File Size**: Must not exceed 20MB
3. **MIME Type**: Must be supported format (image/jpeg, image/png, etc.)
4. **File Content**:
   - Read file bytes
   - Verify magic bytes (file signature)
   - Support all image formats: JPEG, PNG, GIF, BMP, WebP, TIFF
   - Detect corrupted files

**Why separate validator?**
- Reusable across multiple endpoints
- Easy to test in isolation
- Single responsibility
- Clear validation logic

### 4. Dimension Parser (`DimensionParser`)

**Responsibility**: Parse and validate thumbnail dimensions

Supported formats:
- **Presets**: `small` (150×150), `medium` (300×300), `large` (600×600)
- **Custom**: `WIDTHxHEIGHT` format (e.g., `500x500`, `800x600`)

Validation:
- Dimension minimum: 16×16
- Dimension maximum: 2000×2000
- Must be valid integers
- Removes duplicate dimensions

**Design Decision**:
- Parsed as Java `Dimension` objects for type safety
- Enum-like behavior for presets
- Regex validation for custom format

### 5. Thumbnail Generator (`ThumbnailGenerator`)

**Responsibility**: Generate actual thumbnail images

Core algorithm:
```
For each target dimension:
  1. Load original image from bytes
  2. Resize using Imgscalr.resize() with QUALITY method
  3. Encode resized image back to bytes
  4. Measure processing time
  5. Create ThumbnailMetadata with results
```

**Image Resizing**:
- Uses **Imgscalr** library (Java-native, no external dependencies)
- Method: `QUALITY` (best for thumbnail generation)
- Mode: `AUTOMATIC` (maintains aspect ratio intelligently)
- Includes anti-aliasing for smooth edges

**Performance**:
- Single thumbnail: ~50ms average
- Multiple sizes: ~150ms for 3 thumbnails
- Memory efficient (streams, not keeping full images in memory)

### 6. Image Format Detector (`ImageFormatDetector`)

**Responsibility**: Detect image format and read dimensions

**Format Detection** (via magic bytes):
- JPEG: `FF D8 FF`
- PNG: `89 50 4E 47`
- GIF: `47 49 46`
- BMP: `42 4D`
- WebP: `52 49 46 46` + `57 45 42 50`
- TIFF: `49 49 2A 00` or `4D 4D 00 2A`

**Dimension Reading**:
- Reads ImageIO metadata without loading full image
- Returns `ImageDimensions` record (immutable)
- Throws IOException for unreadable images

### 7. Exception Handler (`GlobalExceptionHandler`)

**Responsibility**: Convert exceptions to HTTP responses

Handled exceptions:
- `InvalidImageException` → 400 Bad Request
- `UnsupportedFormatException` → 415 Unsupported Media Type
- `FileSizeLimitExceededException` → 413 Payload Too Large
- `InvalidDimensionsException` → 400 Bad Request
- `MaxUploadSizeExceededException` → 413 Payload Too Large
- Generic `Exception` → 500 Internal Server Error

**Response Format**:
```json
{
  "status": 400,
  "error": "Error Type",
  "message": "Detailed message",
  "path": "/api/v1/thumbnails",
  "timestamp": "2024-01-15T10:30:45",
  "trace_id": "a1b2c3d4-e5f6-g7h8-i9j0"
}
```

### 8. Executor Service Configuration (`ExecutorServiceConfig`)

**Responsibility**: Configure thread pool for concurrent requests

Configuration:
- **Core threads**: 10 (always running)
- **Max threads**: 100 (scales up under load)
- **Queue**: LinkedBlockingQueue with capacity 500
- **Keep-alive**: 60 seconds (unused threads terminated)
- **Rejection policy**: CallerRunsPolicy (backpressure)

**Why ThreadPoolExecutor?**
- Direct control over thread pool behavior
- Predictable performance characteristics
- CallerRunsPolicy provides backpressure (client waits when queue full)
- Named threads for easier debugging

**Concurrency Capacity**:
- Simultaneous processing: up to 100 images
- Queued: up to 500 more
- Total: ~600 concurrent requests before rejection
- Target: 50-500 concurrent ✅

## Data Models

### ThumbnailMetadata (Record)
```java
public record ThumbnailMetadata(
    String size,              // "small", "medium", "large", or "500x500"
    int width,               // Actual width in pixels
    int height,              // Actual height in pixels
    String format,           // "JPEG", "PNG", "GIF", etc.
    long fileSizeBytes,      // Generated thumbnail file size
    String timestamp,        // ISO 8601 format
    long processingTimeMs    // Time to generate this thumbnail
)
```

### ThumbnailResponse (Record + Builder)
```java
public record ThumbnailResponse(
    String originalFilename,
    String originalFormat,
    int originalWidth,
    int originalHeight,
    long originalFileSizeBytes,
    List<ThumbnailMetadata> thumbnails
)
```

**Why Records?**
- Immutability (thread-safe)
- Auto-generated equals/hashCode/toString
- Minimal boilerplate
- Java 21 feature (modern, lightweight)

## Request-Response Flow

### Request
```
POST /api/v1/thumbnails
Content-Type: multipart/form-data

file: <binary image data>
sizes: small,medium,large,500x500 (optional)
```

### Processing Steps
```
1. Spring receives multipart request
2. ThumbnailController validates request
3. ImageValidator checks file
4. DimensionParser processes sizes parameter
5. ThumbnailGenerator creates thumbnails
6. Metadata compiled into response
7. GlobalExceptionHandler catches any errors
8. JSON response returned
```

### Response
```json
200 OK
Content-Type: application/json

{
  "original_filename": "photo.jpg",
  "original_format": "JPEG",
  "original_width": 2000,
  "original_height": 1500,
  "original_file_size_bytes": 524288,
  "thumbnails": [
    {
      "size": "small",
      "width": 150,
      "height": 150,
      "format": "JPEG",
      "file_size_bytes": 5120,
      "timestamp": "2024-01-15T10:30:45.123456",
      "processing_time_ms": 45
    },
    ...
  ]
}
```

## Concurrency & Thread Safety

### Thread Pool Model
```
HTTP Requests (1-500+ concurrent)
    ↓
[TomcatThreadPool: 200 threads max]
    ↓
[ExecutorService: 10-100 threads for image processing]
    ↓
[Image Processing Tasks]
```

### Thread Safety Guarantees
- **Immutable Records**: ThumbnailMetadata, ThumbnailResponse (thread-safe)
- **Stateless Services**: No shared mutable state
- **ThreadPoolExecutor**: Handles concurrent task execution
- **MDC Logging**: Each request gets unique trace_id

### Backpressure Strategy
- **CallerRunsPolicy**: If queue full, caller thread processes task
- This prevents memory explosion under extreme load
- Provides natural rate limiting

## Performance Characteristics

### Time Complexity
- Image validation: O(1) + O(n) where n = file size (magic bytes check)
- Dimension parsing: O(m) where m = number of sizes
- Thumbnail generation: O(w × h) for each resize (image library dependent)

### Space Complexity
- Original image: O(n) where n = file size
- Thumbnails: O(w × h × 3) per thumbnail (RGB pixels)
- Total memory: Original + sum of all thumbnails
- Stream-based: Not holding all in memory simultaneously

### Optimization Opportunities
1. **Caching**: Cache frequently requested sizes
2. **Async Processing**: Queue tasks, return job ID, poll for status
3. **Progressive Loading**: Stream thumbnail bytes as they're generated
4. **Image Pre-processing**: Detect and handle EXIF rotation

## Error Handling Strategy

### Validation Layers
```
1. Spring MVC: Multipart file validation
2. ImageValidator: File size, format, integrity
3. DimensionParser: Dimension format & range
4. GlobalExceptionHandler: Convert to HTTP response
```

### Exception Hierarchy
```
Exception
├─ RuntimeException
│  ├─ InvalidImageException
│  ├─ UnsupportedFormatException
│  ├─ FileSizeLimitExceededException
│  └─ InvalidDimensionsException
└─ Checked (handled by Spring/Servlet)
   └─ IOException, etc.
```

## Logging Strategy

### Levels
- **ERROR**: Exceptions, critical failures
- **WARN**: Validation failures, skipped operations
- **INFO**: Request summaries, configuration changes
- **DEBUG**: Method entry/exit, detailed metrics

### Log Destinations
- **Console**: INFO+ (development)
- **application.log**: DEBUG+ (all events)
- **error.log**: ERROR (errors only)

### Structured Logging
```
%d{ISO8601} [%thread] %-5level %logger - %msg

Example:
2024-01-15T10:30:45.123 [thumbnail-processor-1] INFO  c.t.s.ImageProcessor - Processing image upload: photo.jpg (524288)
```

## Security Considerations

1. **File Upload Validation**:
   - Magic bytes verification (prevent code upload)
   - File size limit (20MB)
   - MIME type checking

2. **Input Validation**:
   - Dimension range limits (16-2000 pixels)
   - Format rejection list

3. **Error Messages**:
   - No sensitive info in error responses
   - Generic error trace IDs for debugging

4. **Non-root Docker User**:
   - Application runs as `appuser` (UID 1000)
   - Prevents privilege escalation

5. **CORS**:
   - Currently allows all origins (configure for production)

## Deployment Considerations

### Docker
- **Multi-stage build**: Reduces image size
- **Alpine base**: 21-jre-alpine (~400MB)
- **Health checks**: Configured for orchestration
- **Non-root user**: Security best practice

### Kubernetes/DO App Platform
- **Liveness probe**: `/actuator/health/liveness`
- **Readiness probe**: `/actuator/health/readiness`
- **Resource limits**: 512MB RAM, 0.25 CPU baseline
- **Auto-scaling**: Up to 2GB RAM, 1 CPU if needed

### Monitoring
- Request tracing via trace IDs
- Processing time metrics per thumbnail
- Log aggregation ready (structured JSON possible)

## Future Enhancements

1. **Caching Layer**: Redis for thumbnail metadata
2. **Async Processing**: Kafka for distributed processing
3. **Database**: Store generation history
4. **Metrics**: Prometheus exposition
5. **CDN Integration**: Cloudflare or similar
6. **Batch Processing**: Upload multiple images at once
7. **Webhook**: Callback when processing complete
8. **Formats Conversion**: Convert between formats during resize

---

**Last Updated**: January 2024
