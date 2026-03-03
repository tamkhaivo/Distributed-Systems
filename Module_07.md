# Module 7: Containerization & Docker

> *"It works on my machine." "Great, then we'll back up your email, put your machine in a box, and ship it to the data center." Containerization solved the dependency hell of the 2000s, but it gave us the orchestration hell of the 2020s.*

## 7.1 The "Works on My Machine" Problem

> *Before Docker, deploying a distributed system involved a 40-page Word document, five sysadmins, three days of downtime, and a lot of cursing at mismatched glibc versions.*

*   **The Matrix from Hell**: If you have 5 different applications (using Node, Python, Java, Go, Ruby) and 4 different target environments (Dev, QA, Staging, Production), you have an $M \times N$ deployment matrix. If Staging has an older version of OpenSSL than Dev, your app breaks the moment it's promoted.
*   **Mutable Infrastructure**: Historically, servers were treated like *pets*. You named them, nurtured them, and SSH'd into them to manually `apt-get install` dependencies. Over time, configuration drift occurred. Server A and Server B, supposedly identical, behaved differently.
*   **The Container Solution**: Instead of shipping just the application code, we ship the application *and* its entire user-space environment (libraries, binaries, configuration) in a single, standardized, immutable package.

## 7.2 Virtual Machines vs. Containers

> *Don't confuse a container with a tiny virtual machine. A virtual machine virtualizes the hardware. A container virtualizes the operating system.*

### Virtual Machines (VMs)
*   **How it Works**: A Hypervisor (like VMware ESXi or KVM) sits on the physical server. It carves up the physical CPU, RAM, and Disk to create distinct virtual hardware platforms. You then install a full Guest OS (like Ubuntu or Windows) onto that virtual hardware.
*   **The Weight**: Every VM carries the overhead of an entire kernel. If you run 10 VMs, you are running 10 separate Linux kernels, 10 separate network stacks, and 10 separate file systems. This uses gigabytes of RAM before your app even boots.
*   **Isolation**: Excellent. Hardware-level isolation is incredibly secure.

### Containers (Docker)
*   **How it Works**: The Docker Engine sits on the Host OS. The containers share the single underlying Host OS kernel directly.
*   **The Weight**: Containers are just processes. They are extremely lightweight, starting in milliseconds instead of minutes. They consume almost zero memory beyond what the application itself uses.
*   **Isolation**: Good, but not hardware-level. Containers are isolated via OS-level constructs (namespaces and cgroups), meaning a kernel exploit can theoretically compromise all containers on the host.
*   *Principal's Take*: "If you want strict, impenetrable multi-tenant security where you don't trust your neighbors, use VMs (like Firecracker). If you want to pack 50 microservices onto a single server efficiently to cut your AWS bill in half, use containers."

## 7.3 Core Docker Technologies (Linux Primitives)

> *Docker didn't invent anything fundamentally new about operating systems. It just provided an incredibly elegant developer experience wrapped around obscure Linux kernel features from the 2000s.*

Docker is essentially a magical combination of three Linux kernel features:

### 7.3.1 Namespaces (Isolation)
Namespaces limit what a process can *see*. When you put a process in a namespace, it thinks it is alone on the machine.
*   **PID (Process ID)**: The container has its own process tree. The app inside is usually PID 1. It cannot see the host's background processes.
*   **NET (Network)**: The container gets its own virtual network stack, its own IP address, its own routing table, and its own firewall rules.
*   **MNT (Mount)**: The container gets its own isolated filesystem map. It cannot look at the host's disk.
*   **IPC (Interprocess Communication)**: Prevents containers from accessing shared memory segments of other containers.
*   **UTS (Unix Timesharing System)**: Allows the container to have its own hostname.

### 7.3.2 Control Groups (cgroups) (Resource Limiting)
While namespaces limit what a process can *see*, cgroups limit what a process can *use*.
*   **The Problem**: Without limits, a Java app with a memory leak in one container will consume all the host's RAM, crashing the other 49 containers via Out-Of-Memory (OOM) kills.
*   **The Fix**: cgroups allow the host to say: "Container A is only allowed 512MB of RAM and 0.5 logical CPU cores." If Container A exceeds that limit, the kernel strictly throttles or kills it, protecting the rest of the node.

### 7.3.3 Union File Systems (e.g., OverlayFS)
Images must be lightweight and fast to pull. If every container contained a full 500MB copy of Ubuntu, your disks would fill immediately.
*   **Layers**: A Docker image is built in layers. 
    1. Layer 1: Base Alpine Linux (5MB).
    2. Layer 2: Install Python (20MB).
    3. Layer 3: Copy App code (1MB).
*   **Reusability**: If you launch 100 Python apps using that base image, Docker only stores Layer 1 and 2 *once* on disk. All 100 containers share those read-only layers.
*   **Copy-on-Write (CoW)**: When a container boots, Docker adds a paper-thin, temporary writable layer on top. If the container modifies a system file, it uses CoW to copy the file from the read-only layer into the writable layer and modifies it there.

## 7.4 The Docker Image vs. Container

> *Understanding the difference between an Image and a Container is the difference between writing classes and creating objects.*

*   **The Image**: The immutable blueprint. It is the read-only file containing your code, libraries, and instructions. Once built, an image never changes. (To change it, you must build a new image with a new version tag).
*   **The Container**: The running instantiation of the image. It is the active process executing in memory with a read-write top layer. You can create a hundred containers from one image.
*   *Principal's Take*: "If you are SSH'ing into a running container to edit a configuration file, you are violating the fundamental axiom of immutable infrastructure. Stop it. When that container restarts, your changes vanish. Fix the Dockerfile, rebuild the image, and redeploy."

## 7.5 Networking in Docker

> *Containers need to talk to each other, to the host, and to the internet. Doing this securely requires understanding virtual networking.*

*   **Bridge Network (The Default)**: Docker creates a local virtual switch (a bridge, usually `docker0`) inside the host. Containers get local IPs (like `172.17.0.x`) and can talk to each other. They use Network Address Translation (NAT) to access the internet.
*   **Host Network**: The container completely bypasses the NET namespace and uses the host's actual network interface. It is fast, but causes port conflicts (two containers cannot both listen on port 80).
*   **Overlay Network**: Used for multi-node deployments (Docker Swarm or Kubernetes). It encapsulates container traffic in UDP packets to securely route communication between containers living on completely different physical servers, making it look like they are on the same local network.

## 7.7 Storage and Persistence

> *Containers are ephemeral by design. When a container dies, its local filesystem dies with it. If that container held your production database, you are now updating your resume.*

*   **The Writable Layer**: Fast, temporary, tightly coupled to the container's lifecycle. Never store customer data here.
*   **Volumes**: The preferred method. Docker provisions and manages an opaque folder on the host machine. You mount this Volume into the container (e.g., at `/var/lib/mysql`). When the container dies, the Volume remains intact. A new database container can be spun up and attached to the old Volume, resuming instantly.
*   **Bind Mounts**: You map a specific, known path on the host (e.g., `/Users/tvo/code/project`) directly into the container. Excellent for local development (changes in your IDE instantly reflect inside the running container). Bad for production (tightly couples the container to the host's directory structure).

## 7.8 Architectural Antipatterns (The Principal's Cut)

1.  **Treating Containers like VMs (The "Fat Container")**
    *   *The Trap*: Installing `systemd`, SSH daemon, cron, syslog, and multiple active applications natively inside a single container.
    *   *The Fix*: A container should wrap a *single logical process*. If you need a web server and a background worker, run two separate containers and compose them.
2.  **Running as Root**
    *   *The Trap*: By default, processes in Docker run as `root`. If a hacker compromises your app and breaks out of the container (escaping the namespace), they become `root` on your physical host machine.
    *   *The Fix*: Always add a `USER` directive in your Dockerfile to run the application as a non-privileged user. Grant the absolute minimum permissions required.
3.  **The 2-Gigabyte Node.js Image**
    *   *The Trap*: Leaving build tools, thousands of source `.cpp` files, and `node_modules` caches in the final production image. It takes forever to pull and drastically increases the surface area for security vulnerabilities.
    *   *The Fix*: Use **Multi-Stage Builds**. Compile your Go code or Webpack bundle in a fat "builder" stage, and only copy the compiled binary/artifacts to a tiny, scratch or Alpine production stage.
4.  **Using 'latest' Tag in Production**
    *   *The Trap*: Your deployment script pulls `myapp:latest`. One day, "latest" introduces a breaking API change. Your autoscaler spins up a new node, pulls the new "latest", and half your cluster crashes while the other half (running the old version) stays alive.
    *   *The Fix*: Pin images to specific, immutable SHAs or strict semantic version tags (`myapp:1.4.2`). "Latest" is a moving target.

## 7.9 Essential Docker Commands (The Operational View)

> *Theory is great, but eventually, you have to actually run the container. And more importantly, you have to know how to kill it when it inevitably hangs.*

### 7.9.1 Running a Container (`docker run`)

Once you've downloaded (or built) an image, the `docker run` command starts a new container instance from that image.

**The Syntax:**
```bash
# Example syntax - do not run blindly
docker run -d -p hostport:containerport namespace/name:tag
```

*   **`-d` (Detached Mode)**: Runs the container in the background and prints the new container ID. If you omit this, the container hijacks your terminal, and closing the terminal kills the container.
*   **`-p hostport:containerport` (Port Publishing)**: This bridges the host's networking namespace and the container's.
    *   `hostport`: The port exposed on your actual local machine's network interface (e.g., `8080`).
    *   `containerport`: The port the application is explicitly listening on *inside* the container (e.g., `80`).
    *   *Principal's Take*: "If you forget `-p`, your container is spinning its wheels in solitary confinement. It's perfectly healthy, but no one on the outside network can reach it."
*   **`namespace/name`**: The name of the image (often in the format `username/repo`, like `postgres` or `mycompany/auth-service`).
*   **`tag`**: The version of the image (e.g., `1.2.3` or `latest`). Always pin this to a specific version in production.

### 7.9.2 Stopping a Container (`docker stop` vs. `docker kill`)

Starting a container is easy; shutting it down gracefully without corrupting state is harder.

*   **`docker stop` (The Polite Request)**:
    *   This command stops the container by issuing a `SIGTERM` (Signal 15) to the main process (PID 1) inside the container.
    *   It gives the application time to catch the signal, finish processing in-flight requests, flush buffers, and close database connections securely.
    *   If the process doesn't exit after a grace period (default 10 seconds), Docker loses patience and sends a `SIGKILL`.
    *   *Usage*: This is the standard, safest way to stop a container.

*   **`docker kill` (The Executioner)**:
    *   This command bypasses the application entirely and sends a `SIGKILL` (Signal 9) directly to the OS kernel.
    *   The container is immediately terminated. The application gets zero warning, cannot clean up transient state, and will likely drop active connections.
    *   *Usage*: Use this strictly as a last resort when a container is completely deadlocked, ignoring `docker stop`, and you need it gone immediately.

*   *Principal's Take*: "If your application requires `docker kill` to shut down because it ignores `SIGTERM`, your application is fundamentally broken. Orchestrators like Kubernetes rely on `SIGTERM` to gracefully drain traffic during deployments. If you ignore it, you will drop user requests on every release."

    *   *The Fix*: Pin images to specific, immutable SHAs or strict semantic version tags (`myapp:1.4.2`). "Latest" is a moving target.

## 7.10 Container Orchestration: From One to Many

> *Docker is great for running one container on your laptop. But when you need to run 5,000 containers across 50 physical servers, handle hardware failures, and route internet traffic to them without dropping a packet, you need an orchestrator.*

If Docker is the shipping container, the Orchestrator is the port authority, the crane operator, and the fleet manager all rolled into one. 

### 7.10.1 Docker Compose (The Local Developer's Best Friend)

*   **The Problem**: A modern app isn't just one container. It's a React frontend, a Node.js API, a PostgreSQL database, and a Redis cache. Remembering the exact `docker run` shell command with 15 different `-e` (environment variable) and `-v` (volume) flags for four different containers is impossible.
*   **The Solution**: Docker Compose. You define your entire multi-container stack in a single, declarative YAML file (`docker-compose.yml`). 
*   **How it Works**: You run `docker-compose up`. Compose reads the YAML, automatically creates a dedicated virtual bridge network for the stack, builds the images if necessary, and starts all the containers in the correct dependency order.
*   **Limitations**: It runs on a *single host*. It is strictly for local development, CI/CD pipelines, or very small single-server deployments. It does not handle high availability or multi-node clustering.

### 7.10.2 Docker Swarm (The Built-In Cluster)

*   **The Problem**: Compose only works on one machine. What if that machine's motherboard catches fire? We need a cluster of machines acting as one logical Docker host.
*   **The Solution**: Docker Swarm. It is orchestration built natively into the Docker CLI. 
*   **How it Works**: You group multiple physical/virtual machines into a "Swarm." Some act as *Managers* (handling the cluster state using the Raft consensus algorithm), and others act as *Workers* (actually running the containers).
*   **The API**: You use the exact same `docker-compose.yml` file you used for local development, but instead of `docker-compose up`, you run `docker stack deploy`. Swarm takes that definition and intelligently distributes the container replicas across the available worker nodes.
*   **The Routing Mesh**: The most powerful feature of Swarm. If you run 3 replicas of a web server on a 10-node cluster, Swarm exposes the port on *all 10 nodes*. If a user hits Node 7 (which isn't running the web server), Node 7's kernel transparently routes the TCP packet across the Swarm overlay network to Node 2 (which is running it) and returns the response.
*   *Principal's Take*: "Swarm is brilliant because it is incredibly simple to set up and maintain. But it lost the orchestration war. The industry collectively decided that simplicity wasn't enough; they wanted the infinite extensibility of Kubernetes, even if it cost them their sanity."

### 7.10.3 Kubernetes (K8s) (The Datacenter OS)

*   **The Origin**: Based on Google's internal Borg system. It is the undisputed industry standard for container orchestration.
*   **The Paradigm Shift**: Kubernetes does not care about your containers. It cares about **State**. You write a declarative YAML manifest saying: "I want 5 copies of this web server running." You submit this to the Kubernetes API. The Kubernetes *Control Plane* acts as an infinite control loop, constantly comparing the *Desired State* (your YAML) with the *Actual State* (the cluster).
*   **The Self-Healing Loop**: If a worker node's power supply blows up, taking 2 web server containers down with it, the Control Plane notices immediately: *Actual State (3) != Desired State (5)*. It immediately schedules 2 new containers onto healthy nodes to restore the state. No human intervention required.

**Key Kubernetes Primitives to Know:**
1.  **Pod**: The smallest deployable unit. Kubernetes doesn't run containers; it runs Pods. A Pod usually contains one container, but can contain multiple tightly-coupled containers that share the exact same localhost network namespace and storage volumes (e.g., an app container and a logging sidecar container).
2.  **Deployment**: The controller that manages your Pods. It handles scaling them up, scaling them down, and performing zero-downtime rolling updates when you change the image version.
3.  **Service**: Pods are ephemeral. Their IP addresses change every time they are recreated. A Service provides a highly available, stable IP address and DNS name that load-balances traffic across a dynamic set of underlying Pods.
4.  **Ingress**: The API gateway. It exposes HTTP/HTTPS routes from outside the cluster to Services within the cluster.

*   *Principal's Take*: "Kubernetes is not a deployment tool. It is a highly extensible platform for building distributed system platforms. It has a brutal learning curve. If you only have three microservices, running Kubernetes is like buying a Boeing 747 to commute to the grocery store. Start with ECS or Cloud Run. Move to Kubernetes only when your organizational complexity demands its governance and flexibility model."

## 7.11 Installing Docker on Ubuntu (The Right Way)

> *Don't just `apt install docker.io`. The default Ubuntu repositories often have ancient versions of Docker. If you want the latest features and security patches, install it directly from Docker's official repository.*

### 1. Clean the Slate
First, ensure you don't have conflicting, older packages installed (like `docker.io`, `docker-doc`, `docker-compose`, or `podman-docker`).
```bash
sudo apt-get remove docker docker-engine docker.io containerd runc
```

### 2. Install Prerequisites
Docker needs a few packages so `apt` can consume repositories over HTTPS securely.
```bash
sudo apt-get update
sudo apt-get install \
    ca-certificates \
    curl \
    gnupg \
    lsb-release
```

### 3. Add Docker's Official GPG Key
This proves the packages you download are actually from Docker, preventing man-in-the-middle attacks.
```bash
sudo mkdir -m 0755 -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
```

### 4. Set Up the Repository
Add the official Docker repository to your apt sources list. This command automatically detects your Ubuntu architecture (e.g., amd64, arm64) and release version (e.g., jammy, focal).
```bash
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
```

### 5. Install the Docker Engine
Now, update the package index again (so it sees the new Docker repo) and install the latest versions of the Engine, containerd, and standard plugins.
```bash
sudo apt-get update
sudo apt-get install docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

*   *Principal's Take*: "If you forget the `ca-certificates` or the GPG key, your apt package manager will refuse to install Docker, and you'll spend two hours debugging a TLS error. Always establish cryptographic trust before installing a system-level daemon."

## 7.12 Deep Dive: The Docker Daemon (`dockerd`)

> *When you type `docker run`, the CLI tool doesn't actually run the container. It just makes an HTTP API call to the Docker Daemon. The Daemon is the brain; the CLI is just a remote control.*

The Docker architecture is strictly client-server. 
1.  **The Client (`docker` CLI)**: The binary you interact with. It can live on your laptop.
2.  **The Host (Server)**: The machine actually running the containers. It runs two critical background services: `dockerd` and `containerd`.

### What Does `dockerd` Actually Do?

*   **API Listener**: `dockerd` listens for Docker Engine API requests (via a local UNIX socket (`/var/run/docker.sock`) or over a secure TCP port). If you send a REST payload to `POST /containers/create`, `dockerd` receives it.
*   **Image Management**: When you run `docker pull ubuntu`, `dockerd` is the component that reaches out to Docker Hub, negotiates the download, pulls the multiple tarball layers, validates their checksums, and unpacks them into the local Union File System (OverlayFS).
*   **Resource Privisoning**: It interfaces with the Linux kernel to carve out the networking namespaces (creating the `docker0` bridge) and sets up the strict cgroups limitations for RAM and CPU.

### The Container Runtime (`containerd` & `runc`)
Historically, `dockerd` did *everything*. It was a monolithic beast. Today, Docker has refactored the architecture according to the OCI (Open Container Initiative) standard.

1.  `dockerd` prepares the environment (storage, network, images).
2.  It then hands the raw execution over to **`containerd`** (an industry-standard supervisory daemon).
3.  `containerd` then calls **`runc`** (a lightweight CLI wrapper around Linux kernel primitives).
4.  `runc` is the component that actually talks directly to the kernel, creates the namespaces, configures the cgroups, starts the container process (PID 1), and then immediately exits. 
5.  `containerd` stays running to monitor the container's lifecycle, stream its stdout/stderr logs back over the API, and report its exit code when it dies.

*   *Principal's Take*: "Knowing that `dockerd` is just an API server changes how you manage it. You can run the Docker Client on your Mac, point the `DOCKER_HOST` environment variable to a remote EC2 instance's IP address, and seamlessly build and run containers in AWS as if they were local. But beware: if you expose your `dockerd` TCP socket without TLS certificates, anyone on the internet can spawn a root container on your server and mine Bitcoin. Treat the Docker socket with the same paranoia as `root` SSH access."

## 7.13 Summary

Containers mathematically simplify distributed engineering. They allow us to package our complex application state into deterministic artifacts. However, a container is just a box. While Docker solved the problem of packaging the box, it introduced the next great distributed systems nightmare: How do we manage 10,000 of these boxes across 500 servers when a network partition happens? 

That is the domain of Orchestration (Kubernetes).
