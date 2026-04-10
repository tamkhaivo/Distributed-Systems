# HW4: Kubernetes and Docker Scaling on AWS (Java & EKS)

This guide provides step-by-step instructions for completing the HW4 scaling and microservices assignment using **Java** and **Amazon EKS** (Elastic Kubernetes Service).

> [!WARNING]
> **AWS Cost Disclaimer**: Amazon EKS does not have a free tier for its control plane (it costs ~$0.10/hour). While this is the easiest and most "production-like" way to complete the assignment, remember to **delete your cluster immediately** after taking your screenshots to keep costs under $1.

## Step 1: Create a Simple Java HTTP Server

First, create the application that will be containerized. We will use Java's built-in HTTP server to avoid hefty dependencies, and we'll add an artificial delay/compute task to make it easier to trigger the CPU limits for the Horizontal Pod Autoscaler.

1. **Create `App.java`**:
```java
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class App {
    public static void main(String[] args) throws Exception {
        // Listen on port 8080
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/status", new MyHandler());
        server.setExecutor(null); 
        System.out.println("Java HTTP Server listening on port 8080...");
        server.start();
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            // Intentionally consume CPU to easily trigger Kubernetes Autoscaling
            double x = 0.0001;
            for (int i = 0; i <= 100000; i++) { 
                x += Math.sqrt(x); 
            }
            
            String response = "Server responding! Simulated CPU load generated.";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}
```

## Step 2: Dockerize the Java App

Create a `Dockerfile` in the exact same directory as your `App.java`.

1. **Create `Dockerfile`**:
```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY App.java .
RUN javac App.java
EXPOSE 8080
CMD ["java", "App"]
```

2. **Build your image locally**:
```bash
docker build -t java-http-server .
```

## Step 3: Push Image to AWS ECR (Elastic Container Registry)

AWS ECR has a secure, private registry that is free up to 500MB/month.

1. **Create an ECR Repository**:
```bash
aws ecr create-repository --repository-name java-http-server --region us-east-1
```

2. **Authenticate Docker to Amazon ECR**:
```bash
aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <YOUR_AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com
```

3. **Tag and Push your image**:
```bash
docker tag java-http-server:latest <YOUR_AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/java-http-server:latest
docker push <YOUR_AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/java-http-server:latest
```

## Step 4: Setup Managed Kubernetes (Amazon EKS)

The easiest way to provision a Kubernetes cluster on AWS is using the official CLI tool, `eksctl`. We will deploy a Fargate cluster so we don't have to manage EC2 worker nodes.

1. Install `eksctl` and `kubectl` on your local machine if you haven't already.
2. **Create the EKS Cluster**:
This will take around 15 minutes to provision the VPC, control plane, and Fargate profiles.
```bash
eksctl create cluster \
  --name hw4-cluster \
  --region us-east-1 \
  --fargate
```

> [!TIP]
> After creation, `eksctl` automatically configures your local `kubectl` to point to the new cluster.

3. **Install the Metrics Server**:
HPA requires the metrics server to monitor CPU utilization. Deploy it using:
```bash
kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml
```

## Step 5: Deploy the Image & Enable Autoscaling (HPA)

1. **Create a Deployment/Service configuration (`deployment.yaml`)**:
*(Make sure to replace `<YOUR_AWS_ACCOUNT_ID>`)*
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: java-app-deployment
spec:
  replicas: 1
  selector:
    matchLabels:
      app: java-http-server
  template:
    metadata:
      labels:
        app: java-http-server
    spec:
      containers:
      - name: java-http-server
        image: <YOUR_AWS_ACCOUNT_ID>.dkr.ecr.us-east-1.amazonaws.com/java-http-server:latest
        ports:
        - containerPort: 8080
        resources:
          requests:
            cpu: "100m" # CRITICAL: Requests must be defined for HPA to calculate percentages
---
apiVersion: v1
kind: Service
metadata:
  name: java-app-service
spec:
  type: LoadBalancer
  ports:
  - port: 80
    targetPort: 8080
  selector:
    app: java-http-server
```
Apply the configuration:
```bash
kubectl apply -f deployment.yaml
```

2. **Configure HPA (Horizontal Pod Autoscaler)**:
Tell Kubernetes to scale out up to 5 pods if the CPU usage exceeds 50% of the requested `100m`.
```bash
kubectl autoscale deployment java-app-deployment --cpu-percent=50 --min=1 --max=5
```

## Step 6: Trigger Demand and Monitor Autoscaler

1. **Find your service endpoint (Load Balancer URL)**:
```bash
kubectl get svc java-app-service
```
Copy the `EXTERNAL-IP` (it will look like a long AWS URL). It might take a few minutes for the Load Balancer to provision.

2. **Generate Load**:
In a separate terminal, throw continuous requests at your Java HTTP server to spike the CPU.
```bash
# Simple shell loop
while true; do curl -s http://<EXTERNAL_IP_URL>:80/status > /dev/null; done
```
*(Alternatively, you can use Java standard tools like Apache JMeter, or CLI tools like `hey` or `ab`).*

3. **Monitor Scaling Behavior**:
Watch Kubernetes spin up new pods dynamically as it notices the CPU spike. Take screenshots of these commands for your deliverable:
```bash
kubectl get hpa -w
kubectl get pods -w
```
You will notice the `REPLICAS` go from 1 to 2, 3, etc. depending on your synthetic load.

## Step 7: Record and Analyze Results

Draft your writeup:
- Take screenshots showing the source code execution.
- Take screenshots of your ECR repository.
- Provide snapshots of `kubectl get hpa` seeing the load jump above 50% and triggering new pods.

> [!CAUTION]
> **AWS Cleanup - VERY IMPORTANT!**
> EKS charges by the hour. Do not leave the cluster running.
> 
> *   **Delete EKS Cluster:** Run `eksctl delete cluster --name hw4-cluster --region us-east-1`
> *   **Clean Registry:** Delete your ECR container image via the AWS Console or AWS CLI (`aws ecr delete-repository --repository-name java-http-server --force --region us-east-1`).
