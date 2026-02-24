# Thumbnail API Service

A production-ready REST API for generating image thumbnails with support for preset and custom dimensions.

## Features

- ✅ **Multi-format Support**: JPEG, PNG, WebP, GIF, BMP, TIFF
- ✅ **Preset Sizes**: Small (150x150), Medium (300x300), Large (600x600)
- ✅ **Custom Dimensions**: Accept any dimension from 16x16 to 2000x2000
- ✅ **Concurrent Handling**: Supports 50-500+ concurrent requests
- ✅ **Comprehensive Validation**: File size, format, integrity checks
- ✅ **Production-Ready**: Spring Boot, Maven, log4j2, Docker, CI/CD
- ✅ **Well-Tested**: Unit and integration tests with 80%+ coverage
- ✅ **Fully Documented**: API specifications and architecture guides

## Quick Start

### Prerequisites

- Java 21 LTS or later
- Maven 3.9+
- Docker (optional, for containerized deployment)

### Local Development

```bash
# Clone repository
git clone git@github.com:YOUR_USERNAME/image-thumbnail-api.git
cd image-thumbnail-api

# Build project
mvn clean package

# Run application
java -jar target/thumbnail-api-1.0.0.jar

# Application starts on http://localhost:8080
```

### With Docker

```bash
# Build Docker image
docker build -t thumbnail-api:latest .

# Run container
docker run -p 8080:8080 thumbnail-api:latest

# Access at http://localhost:8080
```

## API Usage

### Generate Thumbnails

**Endpoint**: `POST /api/v1/thumbnails`

**Request** (multipart/form-data):
```bash
curl -X POST \
  -F "file=@image.jpg" \
  -F "sizes=small,medium,large,500x500" \
  http://localhost:8080/api/v1/thumbnails
```

**Response** (200 OK):
```json
{
  "original_filename": "image.jpg",
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
    {
      "size": "medium",
      "width": 300,
      "height": 300,
      "format": "JPEG",
      "file_size_bytes": 12288,
      "timestamp": "2024-01-15T10:30:45.167890",
      "processing_time_ms": 52
    },
    {
      "size": "large",
      "width": 600,
      "height": 600,
      "format": "JPEG",
      "file_size_bytes": 28672,
      "timestamp": "2024-01-15T10:30:45.210234",
      "processing_time_ms": 67
    },
    {
      "size": "500x500",
      "width": 500,
      "height": 500,
      "format": "JPEG",
      "file_size_bytes": 19456,
      "timestamp": "2024-01-15T10:30:45.251567",
      "processing_time_ms": 58
    }
  ]
}
```

### Size Options

| Parameter | Dimensions | Example |
|-----------|-----------|---------|
| `small` | 150×150 | `small` |
| `medium` | 300×300 | `medium` |
| `large` | 600×600 | `large` |
| `custom` | WIDTHxHEIGHT | `500x500`, `800x600` |

### Error Responses

**400 Bad Request** - Invalid image or dimensions:
```json
{
  "status": 400,
  "error": "Invalid Dimensions",
  "message": "Width 5 is outside valid range [16, 2000]",
  "path": "/api/v1/thumbnails",
  "timestamp": "2024-01-15T10:30:45",
  "trace_id": "a1b2c3d4-e5f6-g7h8-i9j0"
}
```

**413 Payload Too Large** - File exceeds 20MB:
```json
{
  "status": 413,
  "error": "File Size Limit Exceeded",
  "message": "File size 25165824 bytes exceeds maximum allowed size of 20971520 bytes",
  "path": "/api/v1/thumbnails",
  "timestamp": "2024-01-15T10:30:45",
  "trace_id": "a1b2c3d4-e5f6-g7h8-i9j0"
}
```

**415 Unsupported Media Type** - Unsupported image format:
```json
{
  "status": 415,
  "error": "Unsupported Format",
  "message": "Unsupported image format: text/plain",
  "path": "/api/v1/thumbnails",
  "timestamp": "2024-01-15T10:30:45",
  "trace_id": "a1b2c3d4-e5f6-g7h8-i9j0"
}
```

## Testing

### Run All Tests
```bash
mvn test verify
```

### Run Specific Test Suite
```bash
mvn test -Dtest=ImageValidatorTest
mvn test -Dtest=DimensionParserTest
mvn test -Dtest=ThumbnailGeneratorTest
```

### View Coverage
```bash
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

## Project Structure

```
thumbnail-api/
├── src/main/java/com/thumbnailapi/
│   ├── ThumbnailApiApplication.java
│   ├── api/controller/
│   │   └── ThumbnailController.java
│   ├── service/
│   │   ├── ImageProcessor.java
│   │   └── ThumbnailGenerator.java
│   ├── model/
│   │   ├── ThumbnailMetadata.java
│   │   └── ThumbnailResponse.java
│   ├── util/
│   │   ├── ImageValidator.java
│   │   ├── DimensionParser.java
│   │   ├── ImageFormatDetector.java
│   │   └── Constants.java
│   ├── exception/
│   │   ├── InvalidImageException.java
│   │   ├── UnsupportedFormatException.java
│   │   ├── FileSizeLimitExceededException.java
│   │   ├── InvalidDimensionsException.java
│   │   └── GlobalExceptionHandler.java
│   └── config/
│       └── ExecutorServiceConfig.java
├── src/main/resources/
│   ├── application.properties
│   ├── application-prod.properties
│   └── log4j2.xml
├── src/test/java/com/thumbnailapi/
│   ├── controller/ThumbnailControllerTest.java
│   ├── service/ThumbnailGeneratorTest.java
│   └── util/
│       ├── ImageValidatorTest.java
│       ├── DimensionParserTest.java
│       └── ImageFormatDetectorTest.java
├── pom.xml
├── Dockerfile
├── .github/workflows/ci-cd.yml
├── app.yaml
├── README.md (this file)
├── ARCHITECTURE.md
└── DEPLOYMENT.md
```

## Configuration

### Application Properties

**Development** (`application.properties`):
- Log level: DEBUG
- File upload max: 20MB
- Actuator endpoints: enabled

**Production** (`application-prod.properties`):
- Log level: WARN (INFO for app)
- Compression: enabled
- Thread pool optimized for production loads
- Actuator endpoints: health only

### Environment Variables

```bash
SPRING_PROFILES_ACTIVE=prod              # Use production profile
JAVA_TOOL_OPTIONS=-Xmx512m -Xms256m     # Java memory settings
```

### Thread Pool Configuration

- **Core threads**: 10
- **Max threads**: 100
- **Queue capacity**: 500
- **Keep-alive time**: 60 seconds
- **Rejection policy**: CallerRunsPolicy (backpressure)

## Logging

Logs are written to:
- **Console**: INFO level and above (development)
- **`logs/application.log`**: DEBUG level and above
- **`logs/error.log`**: ERROR level

Log rotation:
- Daily or at 100MB, whichever comes first
- Keep up to 10 archived files

## CI/CD Pipeline

GitHub Actions workflow runs on every push to `main`:

1. **Build & Test**
   - Maven clean build
   - Unit tests (surefire)
   - Integration tests (failsafe)
   - Code quality (SpotBugs)

2. **Docker Build & Push**
   - Build multi-stage Docker image
   - Push to GitHub Container Registry (GHCR)
   - Tag with git SHA and `latest`

3. **Deploy to DigitalOcean**
   - Deploy to DO App Platform
   - Auto-scaling enabled
   - Health checks configured

## Deployment

See [DEPLOYMENT.md](DEPLOYMENT.md) for detailed setup instructions for:
- GitHub repository configuration
- GitHub Secrets setup
- DigitalOcean API token configuration
- Manual and automated deployment steps

## Performance

### Benchmarks (on typical hardware)

- **Single thumbnail generation**: ~50ms
- **Three thumbnails**: ~150ms
- **Max concurrent requests**: 500+
- **Average response time**: <500ms

### Optimization Tips

- Image pre-processing and caching for frequently requested sizes
- Asynchronous batch processing for multiple images
- CDN for thumbnail distribution
- Consider Redis caching for thumbnail metadata

## Architecture

See [ARCHITECTURE.md](ARCHITECTURE.md) for:
- Design decisions and rationale
- Concurrency model
- Image processing pipeline
- Thread pool tuning
- Exception handling strategy

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Commit changes: `git commit -am 'Add feature'`
4. Push to branch: `git push origin feature/your-feature`
5. Submit pull request

## License

This project is licensed under the MIT License - see LICENSE file for details.

## Support

For issues, questions, or contributions, please open an issue on GitHub.

---

**Version**: 1.0.0  
**Last Updated**: January 2024  
**Status**: Production Ready ✅
