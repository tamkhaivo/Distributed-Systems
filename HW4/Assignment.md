To complete this assignment using **AWS** (as your chosen cloud provider), you should focus on a **Serverless Container** approach. This is the most cost-effective and easiest way to satisfy the requirements while staying within the AWS Free Tier.

The recommended stack is: **Docker** + **Amazon ECR** (Registry) + **Amazon ECS with AWS Fargate** (Orchestration).

---

## 1. Project Deliverables Structure

### **A. Code & Documentation**
Organize your repository clearly so the grader can see your methodology:
* `/src`: Your application code (Python/Flask, Node.js, etc.).
* `Dockerfile`: The instructions to build your image.
* `docker-compose.yml`: (Optional) To show how you run it locally.
* `README.md`: High-level instructions on how to build and run.

### **B. Writeup Sections**
1.  **Summary of Methodology:** Explain that you chose **AWS ECS Fargate** because it is a "serverless" compute engine for containers, removing the need to manage EC2 instances.
2.  **Experiments & Results:** Describe the build process (local), the push process (to ECR), and the deployment (ECS).
3.  **Limitations & Challenges:**
    * *Challenge:* Configuring Security Groups to allow public traffic on Port 80/443.
    * *Challenge:* IAM permissions for the ECS task execution role.
    * *Limitation:* Fargate startup time can be slower than a pre-provisioned server.
4.  **Future Work:** Mention adding a CI/CD pipeline using **AWS CodePipeline** or **GitHub Actions**.

---

## 2. Technical Architecture (AWS)

The workflow for your cloud setup should follow these steps:
1.  **Build:** Develop your app locally and test with `docker run`.
2.  **Ship:** Create an **Amazon ECR** repository and push your image using the AWS CLI.
3.  **Deploy:** * Create an **ECS Cluster**.
    * Define a **Task Definition** (specify CPU, memory, and your ECR image URI).
    * Create an **ECS Service** to run the task.
    * *Tip:* Use a **Public Subnet** and enable "Auto-assign Public IP" so you can access the app directly via an IP address without needing a costly Load Balancer.

---

## 3. Implementation Guide

### **The Dockerfile**
Ensure your Dockerfile is optimized. Using a "slim" base image helps keep your ECR storage costs low.

```dockerfile
# Example for a Python App
FROM python:3.9-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install -r requirements.txt
COPY . .
CMD ["python", "app.py"]
```

### **AWS Setup Screenshots to Include**
To get full marks for the "cloud setup" requirement, capture these specific screens:
1.  **Amazon ECR:** The repository screen showing your pushed image with the "Latest" tag.
2.  **ECS Task Definition:** Showing the container port mappings (e.g., Port 80 -> 8080).
3.  **ECS Service Events:** The log showing "Service has reached a steady state."
4.  **The Running App:** A browser tab showing your app's UI with the **AWS Public IP** in the address bar.

---

## 4. Cost Management (Crucial)
AWS gives you **500MB** of free ECR storage and a limited amount of Fargate usage in the Free Tier. To avoid charges:
1.  **Delete the ECS Service** immediately after you take your screenshots/video.
2.  **Delete the ECR Repository** to stop storage fees.
3.  **Check "Cost Explorer"** in the AWS Billing Dashboard 24 hours after your project to ensure nothing was left running.

> **Note:** If you are building on a previous assignment, ensure you highlight the transition from "Running a script" to "Containerizing a service."

Would you like a specific code template for a simple "Hello World" app in a particular language (e.g., Python or Node.js) to get your container started?



## Grading Rubric 

Criteria 	Ratings 	Pts
This criterion is linked to a Learning Outcome Summary of the experiment
	
	
5 pts
This criterion is linked to a Learning Outcome Methodology used
	
	
5 pts
This criterion is linked to a Learning Outcome Challenges, limitations, future work
	
	
5 pts
This criterion is linked to a Learning Outcome Developed code and expirement
Organized setup, all files included, comments added throughout the code. Experiment setup, deployment.
	
	
20 pts
This criterion is linked to a Learning Outcome Overall writeup organization
organized work, clear sections, not too short and not too long with enough visuals.
	
	
5 pts
This criterion is linked to a Learning Outcome Results and analysis
Explaining results, analysis of results and providing enough information in the report.
	
	
10 pts
Total Points: 50