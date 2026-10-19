# Development and Deployment Workflow

This document describes the complete workflow for making code updates, building locally, pushing to GitHub, pushing to Docker Hub, and deploying to OpenShift Local for the reservation system.

## Architecture Overview

The reservation system is split into two separate repositories:

- **reservation-system-api**: Spring Boot backend (Java/Kotlin)
- **reservation-system-ui**: React frontend (JavaScript/JSX)

Both repositories deploy as separate pods on OpenShift Local with independent lifecycles.

```
┌─────────────────────────────────────────┐
│           OpenShift Local               │
├─────────────────────────────────────────┤
│                                         │
│  ┌──────────────┐     ┌──────────────┐ │
│  │   UI Pod     │────▶│  API Pod     │ │
│  │  (React)     │     │ (Spring)     │ │
│  │  Port 80     │     │  Port 8080   │ │
│  └──────────────┘     └──────┬───────┘ │
│         │                     │          │
│         │                     ▼          │
│         │              ┌──────────────┐ │
│         │              │   MySQL Pod  │ │
│         │              └──────────────┘ │
│         ▼                                  │
│    External Traffic (LoadBalancer)        │
└─────────────────────────────────────────┘
```

## Prerequisites

### Required Software

- **Java 21**: For building the Spring Boot API
- **Gradle 8.14+**: Build tool for the API
- **Node.js 20+**: For building the React UI
- **npm**: Node package manager
- **Podman** or **Docker**: For building container images
- **OpenShift Local (CRC)**: For local Kubernetes deployment
- **oc CLI**: OpenShift command-line tool
- **Git**: Version control

### Initial Setup

#### 1. Clone Repositories

```bash
# Clone API repository
git clone https://github.com/slohja/reservation-system-api.git
cd reservation-system-api

# Clone UI repository
git clone https://github.com/slohja/reservation-system-ui.git
cd reservation-system-ui
```

#### 2. Configure Podman (macOS)

```bash
# Initialize Podman machine
podman machine init

# Start Podman machine
podman machine start

# Verify it's running
podman machine list
```

#### 3. Login to Docker Hub

```bash
podman login docker.io
# Enter your Docker Hub username and password
```

#### 4. Login to OpenShift Local

```bash
oc login -u developer https://api.crc.testing:6443
```

#### 5. Create OpenShift Project

```bash
oc new-project reservation-system
```

---

## API Development Workflow

### Making Code Changes

1. **Navigate to API repository**
   ```bash
   cd /Users/u404858/IdeaProjects/reservation-system-api
   ```

2. **Create a new branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make your code changes**
   - Edit Java source files in `src/main/java/`
   - Update configuration in `src/main/resources/`
   - Add or update Flyway migrations in `src/main/resources/db/migration/`

4. **Build and test locally**
   ```bash
   ./gradlew build
   ./gradlew test
   ```

5. **Run the application locally**
   ```bash
   ./gradlew bootRun
   ```
   The API will be available at `http://localhost:8080`

### Building Docker Image

1. **Ensure Dockerfile exists** (copy from monorepo if needed)
   ```bash
   # Dockerfile should be in the root of the API repository
   # It uses multi-stage build with Gradle 8.14 and JRE 21
   ```

2. **Build the Docker image**
   ```bash
   cd /Users/u404858/IdeaProjects/reservation-system-api
   podman build -t reservation-system-api:latest .
   ```

3. **Tag the image for Docker Hub**
   ```bash
   podman tag reservation-system-api:latest docker.io/slohja/reservation-system-api:latest
   ```

4. **Push to Docker Hub**
   ```bash
   podman push docker.io/slohja/reservation-system-api:latest
   ```

### Pushing Code to GitHub

1. **Stage and commit changes**
   ```bash
   git add .
   git commit -m "Description of your changes"
   ```

2. **Push to GitHub**
   ```bash
   git push origin feature/your-feature-name
   ```

3. **Create a pull request** on GitHub
   - Review and merge the PR to main branch

### Deploying to OpenShift Local

1. **Ensure OpenShift is running**
   ```bash
   crc status
   oc login -u developer https://api.crc.testing:6443
   ```

2. **Navigate to OpenShift manifests directory**
   ```bash
   cd /Users/u404858/IdeaProjects/reservation-system-api/openshift
   ```

3. **Apply ConfigMap** (if configuration changed)
   ```bash
   oc apply -f configmap.yaml
   ```

4. **Update deployment with new image**
   ```bash
   oc set image deployment/reservation-system-api api=docker.io/slohja/reservation-system-api:latest
   ```

   Or re-apply the deployment manifest:
   ```bash
   oc apply -f deployment.yaml
   ```

5. **Verify deployment**
   ```bash
   oc get pods
   oc logs -f deployment/reservation-system-api
   ```

6. **Test the API**
   ```bash
   # Port forward to test locally
   oc port-forward svc/reservation-system-api 8080:8080

   # Test endpoints
   curl http://localhost:8080/api/v1/management/lanes
   ```

---

## UI Development Workflow

### Making Code Changes

1. **Navigate to UI repository**
   ```bash
   cd /Users/u404858/IdeaProjects/reservation-system-ui
   ```

2. **Install dependencies** (first time only)
   ```bash
   npm install
   ```

3. **Create a new branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

4. **Make your code changes**
   - Edit React components in `src/components/`
   - Edit pages in `src/pages/`
   - Update API service calls in `src/services/`
   - Update environment variables in `.env.local`

5. **Run development server**
   ```bash
   npm run dev
   ```
   The UI will be available at `http://localhost:3000`

6. **Build for production**
   ```bash
   npm run build
   ```

### Building Docker Image

1. **Ensure Dockerfile and nginx.conf exist** (copy from monorepo if needed)
   ```bash
   # Dockerfile should be in the root of the UI repository
   # nginx.conf should be in the root of the UI repository
   ```

2. **Build the production React app**
   ```bash
   cd /Users/u404858/IdeaProjects/reservation-system-ui
   npm run build
   ```

3. **Build the Docker image**
   ```bash
   podman build -t reservation-system-ui:latest .
   ```

4. **Tag the image for Docker Hub**
   ```bash
   podman tag reservation-system-ui:latest docker.io/slohja/reservation-system-ui:latest
   ```

5. **Push to Docker Hub**
   ```bash
   podman push docker.io/slohja/reservation-system-ui:latest
   ```

### Pushing Code to GitHub

1. **Stage and commit changes**
   ```bash
   git add .
   git commit -m "Description of your changes"
   ```

2. **Push to GitHub**
   ```bash
   git push origin feature/your-feature-name
   ```

3. **Create a pull request** on GitHub
   - Review and merge the PR to main branch

### Deploying to OpenShift Local

1. **Ensure OpenShift is running**
   ```bash
   crc status
   oc login -u developer https://api.crc.testing:6443
   ```

2. **Navigate to OpenShift manifests directory**
   ```bash
   cd /Users/u404858/IdeaProjects/reservation-system-ui/openshift
   ```

3. **Apply ConfigMap** (if configuration changed)
   ```bash
   oc apply -f configmap.yaml
   ```

4. **Update deployment with new image**
   ```bash
   oc set image deployment/reservation-system-ui ui=docker.io/slohja/reservation-system-ui:latest
   ```

   Or re-apply the deployment manifest:
   ```bash
   oc apply -f deployment.yaml
   ```

5. **Verify deployment**
   ```bash
   oc get pods
   oc logs -f deployment/reservation-system-ui
   ```

6. **Access the UI**
   ```bash
   # Get the LoadBalancer IP or route
   oc get svc reservation-system-ui
   # Or create a route
   oc expose svc reservation-system-ui
   oc get route
   ```

---

## Initial Deployment (First Time Setup)

### Deploy API

1. **Copy OpenShift manifests to API repository**
   ```bash
   mkdir -p /Users/u404858/IdeaProjects/reservation-system-api/openshift
   cp /Users/u404858/IdeaProjects/reservation-system/openshift-api-deployment.yaml \
      /Users/u404858/IdeaProjects/reservation-system-api/openshift/deployment.yaml
   cp /Users/u404858/IdeaProjects/reservation-system/openshift-api-service.yaml \
      /Users/u404858/IdeaProjects/reservation-system-api/openshift/service.yaml
   cp /Users/u404858/IdeaProjects/reservation-system/openshift-api-configmap.yaml \
      /Users/u404858/IdeaProjects/reservation-system-api/openshift/configmap.yaml
   ```

2. **Update image name in deployment.yaml**
   ```yaml
   # Change image: reservation-system-api:latest
   # To: image: docker.io/slohja/reservation-system-api:latest
   ```

3. **Deploy to OpenShift**
   ```bash
   cd /Users/u404858/IdeaProjects/reservation-system-api/openshift
   oc apply -f configmap.yaml
   oc create secret generic db-credentials \
       --from-literal=username=your-db-username \
       --from-literal=password=your-db-password
   oc apply -f deployment.yaml
   oc apply -f service.yaml
   ```

### Deploy UI

1. **Copy OpenShift manifests to UI repository**
   ```bash
   mkdir -p /Users/u404858/IdeaProjects/reservation-system-ui/openshift
   cp /Users/u404858/IdeaProjects/reservation-system/openshift-ui-deployment.yaml \
      /Users/u404858/IdeaProjects/reservation-system-ui/openshift/deployment.yaml
   cp /Users/u404858/IdeaProjects/reservation-system-ui/openshift-ui-service.yaml \
      /Users/u404858/IdeaProjects/reservation-system-ui/openshift/service.yaml
   cp /Users/u404858/IdeaProjects/reservation-system/openshift-ui-configmap.yaml \
      /Users/u404858/IdeaProjects/reservation-system-ui/openshift/configmap.yaml
   ```

2. **Update image name in deployment.yaml**
   ```yaml
   # Change image: reservation-system-ui:latest
   # To: image: docker.io/slohja/reservation-system-ui:latest
   ```

3. **Deploy to OpenShift**
   ```bash
   cd /Users/u404858/IdeaProjects/reservation-system-ui/openshift
   oc apply -f configmap.yaml
   oc apply -f deployment.yaml
   oc apply -f service.yaml
   ```

---

## Environment Configuration

### API Configuration

The API uses environment variables configured via OpenShift ConfigMap and Secret:

**ConfigMap (`configmap.yaml`):**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: api-config
data:
  database-url: "jdbc:mysql://mysql:3306/reservation_system"
```

**Secret (created manually):**
```bash
oc create secret generic db-credentials \
    --from-literal=username=appuser \
    --from-literal-password=apppass
```

### UI Configuration

The UI uses environment variables configured via OpenShift ConfigMap:

**ConfigMap (`configmap.yaml`):**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: ui-config
data:
  api-url: "http://reservation-system-api:8080"
```

**Local development (`.env.local`):**
```bash
VITE_API_URL=http://localhost:8080
```

**Production (`.env.production`):**
```bash
VITE_API_URL=https://api.yourdomain.com
```

---

## Troubleshooting

### Podman Issues

**Podman machine not running:**
```bash
podman machine start
```

**Podman connection refused:**
```bash
podman machine stop
podman machine start
```

### Docker Build Issues

**Gradle version mismatch:**
- Ensure Dockerfile uses Gradle 8.14 or later
- Spring Boot 4.1.0 requires Gradle 8.14+

**Build fails with dependency errors:**
```bash
# Clear Gradle cache and rebuild
./gradlew clean build --refresh-dependencies
```

### OpenShift Issues

**Cannot connect to OpenShift:**
```bash
crc start
oc login -u developer https://api.crc.testing:6443
```

**Pod not starting:**
```bash
oc get pods
oc describe pod <pod-name>
oc logs <pod-name>
```

**Image pull errors:**
```bash
# Verify image exists on Docker Hub
podman pull docker.io/slohja/reservation-system-api:latest

# Verify OpenShift can access Docker Hub
oc get imagestream
```

### API Issues

**Database connection errors:**
- Verify MySQL pod is running
- Check database credentials in Secret
- Verify ConfigMap database URL is correct

**404 errors on API endpoints:**
- Verify API versioning: use `/api/v1/` prefix
- Check controller mappings

### UI Issues

**UI cannot connect to API:**
- Verify API pod is running
- Check nginx.conf proxy configuration
- Verify ConfigMap api-url is correct
- Check browser console for CORS errors

---

## API Endpoints

All API endpoints use versioned paths (`/api/v1/`):

### Management
- `POST /api/v1/management/lanes` - Create lanes
- `GET /api/v1/management/lanes` - Get all lanes
- `GET /api/v1/management/lane/{laneNumber}` - Get lane by number
- `POST /api/v1/management/lanes/{id}/block` - Block a lane
- `POST /api/v1/management/lanes/{id}/unblock` - Unblock a lane
- `PUT /api/v1/management/lanes/{id}` - Update a lane
- `POST /api/v1/management/reservations/{id}/approve` - Approve extended hours
- `POST /api/v1/management/reservations/{id}/reject` - Reject extended hours

### Reservations
- `POST /api/v1/reservations` - Create a reservation
- `GET /api/v1/reservations/{id}` - Get reservation by ID
- `PATCH /api/v1/reservations/{id}/cancel` - Cancel a reservation
- `GET /api/v1/reservations` - Query reservations with filters

### Customers
- `POST /api/v1/customers` - Create a customer
- `GET /api/v1/customers/{id}` - Get customer by ID
- `PUT /api/v1/customers/{id}` - Update a customer
- `POST /api/v1/customers/history` - Get customer reservation history

### Availability
- `POST /api/v1/availability` - Check lane availability
- `GET /api/v1/lanes/status` - Get current lane status

---

## Quick Reference Commands

### API
```bash
# Build
cd /Users/u404858/IdeaProjects/reservation-system-api
./gradlew build

# Run locally
./gradlew bootRun

# Build Docker image
podman build -t reservation-system-api:latest .

# Tag and push
podman tag reservation-system-api:latest docker.io/slohja/reservation-system-api:latest
podman push docker.io/slohja/reservation-system-api:latest

# Deploy to OpenShift
oc set image deployment/reservation-system-api api=docker.io/slohja/reservation-system-api:latest
```

### UI
```bash
# Install dependencies
cd /Users/u404858/IdeaProjects/reservation-system-ui
npm install

# Run locally
npm run dev

# Build for production
npm run build

# Build Docker image
podman build -t reservation-system-ui:latest .

# Tag and push
podman tag reservation-system-ui:latest docker.io/slohja/reservation-system-ui:latest
podman push docker.io/slohja/reservation-system-ui:latest

# Deploy to OpenShift
oc set image deployment/reservation-system-ui ui=docker.io/slohja/reservation-system-ui:latest
```

### OpenShift
```bash
# Login
oc login -u developer https://api.crc.testing:6443

# Check status
oc get pods
oc get svc
oc get configmap
oc get secret

# View logs
oc logs -f deployment/reservation-system-api
oc logs -f deployment/reservation-system-ui

# Port forward
oc port-forward svc/reservation-system-api 8080:8080
oc port-forward svc/reservation-system-ui 3000:80
```

---

## Version History

- **v1.0.0** - Initial multi-repo structure with API versioning
- API endpoints use `/api/v1/` prefix
- Separate repositories for API and UI
- OpenShift Local deployment configuration
