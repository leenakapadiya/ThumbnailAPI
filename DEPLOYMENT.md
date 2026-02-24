# Deployment Guide

Complete step-by-step instructions for deploying the Thumbnail API to DigitalOcean App Platform with GitHub Actions CI/CD.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [GitHub Repository Setup](#github-repository-setup)
3. [GitHub Actions Configuration](#github-actions-configuration)
4. [DigitalOcean Setup](#digitalocean-setup)
5. [Deployment Process](#deployment-process)
6. [Verification](#verification)
7. [Troubleshooting](#troubleshooting)

## Prerequisites

### Local Requirements
- Git configured with your GitHub account
- GitHub account with repository creation permissions
- DigitalOcean account with API access

### Account Setup
1. Create/verify GitHub account: https://github.com
2. Create/verify DigitalOcean account: https://digitalocean.com
3. Ensure Docker Hub account (or use GitHub Container Registry)

## GitHub Repository Setup

### Step 1: Create GitHub Repository

1. Go to GitHub: https://github.com/new
2. Fill in repository details:
   - **Repository name**: `image-thumbnail-api`
   - **Description**: "Production-ready REST API for image thumbnail generation"
   - **Visibility**: Public (or Private if preferred)
   - **Initialize with**: Empty (we'll push existing code)
3. Click "Create repository"

### Step 2: Push Local Code to GitHub

```bash
cd /workspaces/thumbnail-api

# Initialize git (if not already done)
git init

# Add all files
git add .

# Create initial commit
git commit -m "Initial commit: Production-ready thumbnail API"

# Add GitHub as remote
git remote add origin https://github.com/YOUR_USERNAME/image-thumbnail-api.git

# Rename branch to main (if needed)
git branch -M main

# Push to GitHub
git push -u origin main
```

**Replace `YOUR_USERNAME` with your actual GitHub username.**

### Step 3: Verify GitHub Repository

Visit `https://github.com/YOUR_USERNAME/image-thumbnail-api` and confirm:
- All files are pushed ✓
- README.md displays correctly ✓
- `.github/workflows/ci-cd.yml` exists ✓

## GitHub Actions Configuration

### Step 4: Configure GitHub Secrets

GitHub Actions workflow needs credentials to:
1. Push Docker images to GitHub Container Registry
2. Deploy to DigitalOcean App Platform

**Automatic secrets** (GitHub provides):
- `GITHUB_TOKEN`: Auto-generated, used for GHCR authentication

**Manual secrets needed**:
- `DIGITALOCEAN_ACCESS_TOKEN`: Required for DO deployment

#### Add DigitalOcean API Token

1. Log into DigitalOcean: https://cloud.digitalocean.com
2. Navigate to: **API** → **Tokens/Keys**
3. Click **Generate New Token**:
   - Name: `GitHub-Actions`
   - Select scopes:
     - ☑ `read:app_spec`
     - ☑ `write:app_spec`
     - ☑ `read:resource`
   - Expiration: 90 days (or longer)
   - Click "Generate Token"
4. **Copy the token immediately** (won't show again)

5. Add to GitHub Secrets:
   - Go to GitHub repo: https://github.com/YOUR_USERNAME/image-thumbnail-api
   - Navigate to: **Settings** → **Secrets and variables** → **Actions**
   - Click **New repository secret**
   - Name: `DIGITALOCEAN_ACCESS_TOKEN`
   - Value: Paste the token from step 4
   - Click "Add secret"

**Verify secrets are set:**
```bash
# You can't read secrets, but they appear in the Actions tab
# when the workflow runs
```

## DigitalOcean Setup

### Step 5: Create DigitalOcean App Manually

For the first deployment, you need to create the App manually in DO:

1. Log into DigitalOcean: https://cloud.digitalocean.com
2. Navigate to **Apps** → **Create Apps**
3. Select deployment source:
   - Source: **GitHub**
   - Repository: `YOUR_USERNAME/image-thumbnail-api`
   - Branch: `main`
4. Configure the app:
   - App name: `thumbnail-api`
   - Region: Choose closest to users (default: NYC)
5. Configure service:
   - **Source Code** → Edit:
     - Build command: `mvn clean package -DskipTests`
     - Run command: (leave default or) `java -jar target/thumbnail-api-1.0.0.jar --spring.profiles.active=prod`
   - **Container port**: 8080
   - **HTTP routes**:
     - Path: `/`
     - Preserve path prefix: ✓
6. Resources:
   - Instance type: **Basic** (smallest)
   - Instance size: **$5/month** (512MB RAM, 0.25 CPU)
   - Instance count: 1
   - Disable auto-scaling (for testing) or set max to 2

7. Environment variables:
   - Click **Add environment variable**:
     - Key: `SPRING_PROFILES_ACTIVE`
     - Value: `prod`
   - Click **Add environment variable**:
     - Key: `JAVA_TOOL_OPTIONS`
     - Value: `-Xmx512m -Xms256m`

8. Health checks:
   - HTTP path: `/api/v1/thumbnails/health`
   - Interval: 30 seconds
   - Timeout: 3 seconds

9. Click **Create App**

**Initial deployment takes 3-5 minutes.** Do NOT interrupt.

### Step 6: Get DO App ID

After app is created and running:

1. Go to DigitalOcean Dashboard
2. Navigate to Apps
3. Click on your app
4. Note the **App ID** (format: `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx`)
5. Save this for future reference

Alternatively, get App ID from CLI:
```bash
# Install doctl (DigitalOcean CLI)
# https://docs.digitalocean.com/reference/doctl/

doctl apps list
# Copy the APP ID from output
```

### Step 7: Update app.yaml

Update the `app.yaml` file in the repository with your GitHub username:

```bash
cd /workspaces/thumbnail-api

# Edit app.yaml
# Find this section:
services:
- name: thumbnail-api
  github:
    branch: main
    deploy_on_push: true
    repo: YOUR_GITHUB_USERNAME/image-thumbnail-api  # ← Change this

# Replace YOUR_GITHUB_USERNAME with your actual username

git add app.yaml
git commit -m "Update app.yaml with GitHub username"
git push origin main
```

## Deployment Process

### Automatic Deployment via GitHub Actions

**Trigger**: Push to `main` branch

```bash
cd /workspaces/thumbnail-api

# Make a change (e.g., update README)
echo "## Deployment: $(date)" >> README.md

# Commit and push
git add README.md
git commit -m "Update README"
git push origin main
```

**GitHub Actions Workflow Runs**:
1. **Build & Test** (~5 min)
   - Maven clean build
   - Unit tests
   - Integration tests
   - Code quality checks
2. **Docker Build & Push** (~3 min)
   - Build multi-stage Docker image
   - Push to GitHub Container Registry
   - Tag with git SHA and `latest`
3. **Deploy to DigitalOcean** (~2 min)
   - Detect app spec changes
   - Deploy to DO App Platform

**Monitor deployment**:
- GitHub: https://github.com/YOUR_USERNAME/image-thumbnail-api/actions
- DigitalOcean: https://cloud.digitalocean.com/apps

### Manual Deployment (if needed)

If GitHub Actions deployment fails:

```bash
# 1. Build Docker image locally
cd /workspaces/thumbnail-api
docker build -t thumbnail-api:latest .

# 2. Test image locally
docker run -p 8080:8080 thumbnail-api:latest

# 3. Tag for GHCR
docker tag thumbnail-api:latest ghcr.io/YOUR_USERNAME/image-thumbnail-api:latest

# 4. Push to GHCR (requires login)
docker login ghcr.io
docker push ghcr.io/YOUR_USERNAME/image-thumbnail-api:latest

# 5. Update DO app (via dashboard or CLI)
# Option A: Via Dashboard
#   - Navigate to app settings
#   - Restart app
# Option B: Via CLI
#   doctl apps update <APP_ID> --spec app.yaml
```

## Verification

### Step 8: Test Deployed Application

Your app should be live at: `https://thumbnail-api-<random>.ondigitalocean.app`

Find exact URL:
1. Go to DigitalOcean Apps dashboard
2. Click on `thumbnail-api` app
3. Copy the "Live App" URL

#### Test Health Endpoint

```bash
curl https://thumbnail-api-<random>.ondigitalocean.app/api/v1/thumbnails/health
# Response: OK
```

#### Test Info Endpoint

```bash
curl https://thumbnail-api-<random>.ondigitalocean.app/api/v1/thumbnails/info
# Response: JSON with app info
```

#### Test Thumbnail Generation

Create a test image:
```bash
# Create a 500x500 test image (PNG)
# Using ImageMagick or any image tool
convert -size 500x500 xc:blue /tmp/test.png

# Or download a sample image
curl -o /tmp/test.jpg https://picsum.photos/500/500
```

Make a request:
```bash
curl -X POST \
  -F "file=@/tmp/test.png" \
  -F "sizes=small,medium,large,400x300" \
  https://thumbnail-api-<random>.ondigitalocean.app/api/v1/thumbnails \
  | jq .
```

Expected response (200 OK):
```json
{
  "original_filename": "test.png",
  "original_format": "PNG",
  "original_width": 500,
  "original_height": 500,
  "original_file_size_bytes": 5000,
  "thumbnails": [
    {
      "size": "small",
      "width": 150,
      "height": 150,
      "format": "PNG",
      "file_size_bytes": 1024,
      "timestamp": "2024-01-15T10:30:45",
      "processing_time_ms": 45
    },
    ...
  ]
}
```

### Step 9: Monitor Application

#### View Logs

In DigitalOcean Dashboard:
1. Open app → **Logs**
2. View real-time application logs

Via CLI:
```bash
doctl apps logs get <APP_ID> --follow
```

#### Check Application Metrics

In DigitalOcean Dashboard:
1. Open app → **Insights**
2. Monitor CPU, memory, request rate

#### View Deployment Status

In DigitalOcean Dashboard:
1. Open app → **Deployments**
2. View deployment history and logs

## Custom Domain (Optional)

### Add Custom Domain to DigitalOcean App

1. Go to DigitalOcean Dashboard → Apps → Your app
2. Click **Settings** → **Domains**
3. Click **Add Domain**
4. Enter your domain (e.g., `api.example.com`)
5. Update your domain provider's DNS:
   - Add CNAME record pointing to DO-provided endpoint
   - Instructions provided in DO dashboard

### SSL/TLS Certificate

DigitalOcean App Platform automatically provisions Let's Encrypt certificate for your domain.

## Scaling Configuration

### Auto-Scaling Setup

Edit `app.yaml`:
```yaml
services:
- name: thumbnail-api
  ...
  instance_count: 1
  # Add auto-scaling config:
  # Note: This requires upgrading from App Platform Starter
```

Or via DigitalOcean Dashboard:
1. App → **Settings** → **Resource Configuration**
2. Enable auto-scaling
3. Set min/max instances
4. Set CPU/memory thresholds

### Manual Scaling

Change instance count in DigitalOcean Dashboard:
1. App → **Settings** → **Resource Configuration**
2. Adjust "Instance count"
3. Changes take effect in ~2 minutes

## Troubleshooting

### Issue: Build Failures

**Symptom**: GitHub Actions build fails

**Solution**:
```bash
# 1. Check Maven build locally
mvn clean package

# 2. Review GitHub Actions logs
# Go to: Actions → Latest workflow run → Click failed job

# 3. Common issues:
# - Java version mismatch: Ensure Java 21
# - Dependencies: Run mvn dependency:resolve
# - Tests failing: Review test output

# 4. Push fix
git add .
git commit -m "Fix build issue"
git push origin main
```

### Issue: Docker Image Too Large

**Symptom**: DO app deployment slow

**Solution**:
```bash
# Multi-stage build reduces image size
# Current Dockerfile uses builder stage
# Image size should be ~400MB

# Check image size
docker images
# Should show ~400MB or less

# If larger, optimize:
# - Remove unused dependencies from pom.xml
# - Use alpine base image (already done)
# - Enable Maven cache in buildkit
```

### Issue: App Not Responding

**Symptom**: 503 Service Unavailable

**Solution**:
```bash
# 1. Check DO app status
# Dashboard → Apps → Your app → Check health

# 2. Check logs for errors
doctl apps logs get <APP_ID>

# 3. Restart app
# Dashboard → Apps → Your app → Restart

# 4. Check memory/CPU usage
# If at limit, scale up resources
```

### Issue: Slow Thumbnail Generation

**Symptom**: Requests taking >5 seconds

**Solution**:
- Increase memory: Instance size from basic-xs to basic-sm (1GB)
- Increase CPU: Same instance size change provides more CPU
- Optimize image processing: Check ARCHITECTURE.md for tips
- Monitor metrics: Dashboard → Insights

### Issue: GitHub Actions Can't Push to DO

**Symptom**: "401 Unauthorized" in deploy step

**Solution**:
1. Verify `DIGITALOCEAN_ACCESS_TOKEN` is set
2. Check DO token has correct scopes (read + write app_spec)
3. Verify token hasn't expired (90 days default)
4. Generate new token and update GitHub Secret

### Issue: GitHub Actions Workflow Not Triggering

**Symptom**: Workflow doesn't run on push

**Solution**:
```bash
# 1. Verify .github/workflows/ci-cd.yml exists
git ls-files | grep workflow

# 2. Verify branch is main
git branch -M main
git push -u origin main

# 3. Check GitHub Actions enabled
# Settings → Actions → General → Allow Actions

# 4. Verify commit to main branch
# Not to develop or other branch
git push origin main

# 5. Check workflow syntax
# Actions tab → Workflow validation errors
```

## Rollback Deployment

### Rollback to Previous Version

**Option 1: Via DigitalOcean Dashboard**
1. App → Deployments
2. Click on previous successful deployment
3. Click "Rollback"

**Option 2: Revert Git Commit**
```bash
# Find commit to revert to
git log --oneline

# Revert to specific commit
git revert <COMMIT_HASH>

# Push reverted version
git push origin main
# GitHub Actions automatically deploys
```

**Option 3: Manual Image Rollback**
```bash
# Get previous image tag
docker images | grep image-thumbnail-api

# Re-deploy previous version
docker push ghcr.io/YOUR_USERNAME/image-thumbnail-api:previous-tag

# Update DO app via dashboard
```

## Performance Tuning

### Monitor and Optimize

1. **Check Request Latency**:
   - Dashboard → Insights → Requests/Response times
   - Target: <500ms average

2. **Check Resource Usage**:
   - Dashboard → Insights → CPU & Memory
   - Target: <60% utilization under normal load

3. **Optimize if Needed**:
   - Increase instance size (more RAM/CPU)
   - Enable caching layer (Redis)
   - Use CDN for thumbnail distribution
   - Implement image pre-processing

## Cost Optimization

### DigitalOcean Pricing

- **Basic App Platform**: $5/month (512MB RAM, 0.25 CPU)
- **Storage**: $0.05 per GB/month (if enabled)
- **Bandwidth**: First 1TB/month free, then $0.02 per GB
- **Custom domain**: Free

### Reduce Costs

1. Keep instance at basic-xs ($5/month)
2. Don't enable persistent storage (in-memory processing)
3. Use auto-scaling if traffic varies
4. Monitor and alert on unexpected costs

## Next Steps

1. ✅ Test deployed application
2. ✅ Set up custom domain (optional)
3. ✅ Configure monitoring/alerts
4. ✅ Document API endpoints for clients
5. ✅ Plan backup/disaster recovery strategy
6. ✅ Consider caching layer for high traffic

---

**Last Updated**: January 2024
