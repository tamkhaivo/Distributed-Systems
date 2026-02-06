# Module 2: Processes & Virtualization

*   *Note*: Code migration is cool in theory, terrifying in practice (security nightmares).
*   *Real-word check*: Containers (Kubernetes) have largely solved the packaging problem, but introduced a networking complexity layer that rivals the original problem.

## Architectures: Cluster Computing

> *The "Beowulf" approach: Duct-taping commodity hardware to build a supercomputer.*

*   **Homogeneous Environment**
    *   **Concept**: Same OS (usually Linux), same hardware specs, same libraries.
    *   **The "Why"**: Simplifies management. "Single System Image" (SSI) is the holy grail/myth here. If Node 1 is identical to Node 100, you don't have to debug why the binary segfaults only on the ones with the slightly older Southbridge chipset.
    *   **Ops Reality**: In practice, "homogeneous" drifts. Firmware updates fail, slight batch variations in RAM. But we *pretend* they are the same.

*   **Network (The fabric)**
    *   Connected via high-speed, low-latency LAN (Infiniband, high-throughput Ethernet).
    *   **Critical constraints**: Tensely coupled systems (running MPI jobs) die on latency. This isn't "interconnected via the internet" (that's Grid). This is "interconnected via a switch that costs more than my car".

*   **High Performance Computing (HPC)**
    *   The primary driver. Weather forecasting, fluid dynamics, crypto-mining (unfortunately).
    *   **Parallelism**: Data parallelism or Task parallelism.
    *   **Programming model**: Often MPI (Message Passing Interface). If you think debugging threads is hard, try debugging race conditions across 1000 nodes where the debugger itself causes the race condition to hide.

*   **...ETC (Evolution & Context)**
    *   **Cluster Middleware**: Tools needed to manage this mess (Kubernetes is the modern descendant, but Slurm/Torque were the ancestors).
    *   **vs. Grid Computing**: Clusters are tightly coupled & homogeneous. Grids are loosely coupled & heterogeneous (federated).

## Architectures: Grid Computing (The "Pre-Cloud" Dinosaur)

> *Historical Note: Before AWS, there was "The Grid". Academics dreaming of a world where I could borrow your CPU cycles while you slept. SETI@home is the most famous survivor.*

*   **Heterogeneous Environment**
    *   **Concept**: Different OSs, different hardware, spanning different administrative domains.
    *   **The Nightmare**: Security/Trust. Why should I run your binary on my server?
    *   **Legacy**: It paved the way for Cloud, but "Grid" as a term is mostly dead outside specific academic circles (CERN).

## Architectures: Cloud Computing

> *The definition implies "Utility Computing", but in reality, it's just "renting someone else's computer by the second".*

*   **The Paradigm Shift**:
    *   **CapEx (Capital Expenditure) -> OpEx (Operational Expenditure)**: You don't buy servers; you lease them. Make the CFO happy until the first AWS bill arrives and it's 3x the estimate.
    *   **Elasticity**: The "Killer Feature". Scale up when Reddit hugs you, scale down when everyone goes to sleep.
    *   **Virtualization is Key**: You aren't getting a bare-metal server (usually). You're getting a slice of a hypervisor.

*   **Service Models (The Pizza Analogy)**:
    *   **IaaS (Infrastructure as a Service)**: AWS EC2. "Here's a VM, good luck." You manage the OS, updates, patches.
    *   **PaaS (Platform as a Service)**: Heroku, Google App Engine. "Here's a runtime, upload your JAR." You lose control but gain sleep.
    *   **SaaS (Software as a Service)**: Gmail, Salesforce. "Here's a login." You own nothing, not even your data format usually.

## Architectures: Enterprise Systems

> *Where software goes to die (or run the global economy).*

*   **Characteristics**:
    *   **Massive Complexity**: It's not just "A talks to B". It's "A talks to B via a Message Bus that syncs with Mainframe C which updates Data Warehouse D".
    *   **Interoperability**: The hardest problem. Getting a modern Go microservice to talk to a COBOL backend from 1982.
    *   **Transactionality (ACID)**: Data loss is not an option. "Eventual Consistency" is a hard sell to a bank auditor.

*   **Evolution**:
    *   **Monoliths**: One giant binary. Easy to debug, impossible to deploy without downtime.
    *   **SOA (Service Oriented Architecture)**: The bloated precursor to microservices. SOAP, WSDL, and XML XML XML.
    *   **Microservices**: The current "answer". Decompose the monolith. Now you have 1000 tiny binaries and you need Distributed Tracing just to find out where the request died.

## Architecture Styles: The "Blueprint"

> *Software Architecture tells us how the components are organized and how they interact. It's the difference between a pile of bricks and a cathedral (or a pile of bricks that looks like a cathedral but collapses in a breeze).*

*   **Physical vs. Logical Architecture**
    *   **Logical**: The whiteboard diagram. "The Authentication Service talks to the User Database." Clean, ideal, abstract.
    *   **Physical**: The headache. "The Authentication Service runs on Node A (AWS us-east-1) and talks to the User Database on Node B (AWS us-east-2) over a flaky TCP connection with 20ms latency."
    *   *Rule #1*: Logical architecture lies; Physical architecture kills.

*   **Components vs. Connectors**
    *   **Component**: A unit of computation or data store (e.g., a Service, a Database, a Filter). The "Noun".
    *   **Connector**: The mechanism of interaction (e.g., RPC, Message Queue, Shared Memory, Socket). The "Verb".
    *   *Insight*: Junior engineers focus on Components. Senior engineers obsess over Connectors (because that's where the failure happens).

*   **Middleware: The "Magic" Glue**
    *   **Definition**: The software layer that sits between the Application and the OS/Network.
    *   **Purpose**: Hides heterogeneity. It makes a remote call look local (RPC) or manages the chaos of message passing.
    *   **Examples**: CORBA (ancient evil), gRPC (modern implementation), RabbitMQ.

*   **Common Styles**:
    1.  **Layered Architecture**:
        *   The classic "Wedding Cake". Presentation -> Business Logic -> Data Access -> Database.
        *   **Pro**: Separation of concerns.
        *   **Con**: The "Sinkhole Anti-Pattern". Requests pass through layers adding no value, just latency.
    2.  **Service-Oriented Architecture (SOA)**:
        *   Services as the primary building block. "Services" are coarser-grained than microservices (think "Billing" vs "GenerateInvoicePDF").
        *   Often associated with an Enterprise Service Bus (ESB) which becomes a single point of failure.
    3.  **Publish-Subscribe (Event-Driven)**:
        *   **Decoupling**: Publishers don't know who subscribers are. "Fire and forget."
        *   **The Trap**: "I sent the event, why didn't X happen?" Debugging flow in a pure event system is a nightmare of tracing logs.
    4.  **Hybrid (Real World)**:
        *   Pure architectures are academic myths. Real systems are Layered implementations where some layers call Microservices that communicate via Pub/Sub, with a legacy Mainframe attached via Middleware.
        *   *Result*: A "Big Ball of Mud" that prints money.

## The 8 Fallacies of Distributed Computing

> *The classic list by Peter Deutsch (Sun Microsystems). If you believe any of these, you will be woken up at 3 AM.*

1.  **The network is reliable**: It's not. Switches reboot, fibers get cut by backhoes, sharks eat undersea cables.
2.  **Latency is zero**: It takes time for light to travel. In-memory calls are nanoseconds; cross-region calls are milliseconds. That's a 1,000,000x difference.
3.  **Bandwidth is infinite**: Sending terabytes of JSON will clog even a 100Gbps pipe.
4.  **The network is secure**: Assume the network is hostile. Zero Trust is the only way.
5.  **Topology doesn't change**: Nodes come up, nodes die, autoscalers fire. The map is always wrong.
6.  **There is one administrator**: No. You have the DB team, the NetOps team, the AWS support team, and the intern who pushed bad config. They don't talk to each other.
7.  **Transport cost is zero**: Serialization/Deserialization (SerDes) burns CPU. AWS charges for cross-AZ traffic.
8.  **The network is homogeneous**: Mobile phones on 3G are accessing your API alongside servers on 100Gbps fiber.

## Architectural Characteristics (The "Ilities")

> *Functional requirements are what the system does. Architectural characteristics are how the system breaks.*

*   **Scalability** (The most abused buzzword)
    *   **Definition**: The ability to handle increased load by adding resources *without* changing the design.
    *   **Scaling Dimensions (The Scale Cube)**:
        1.  **Size (Numerical)**: Adding more users/requests.
            *   *Solution*: Add more web servers behind a Load Balancer.
            *   *Bottleneck*: The centralized database (eventually you can't add more web servers if the DB hangs).
        2.  **Geographical (Distance)**: Users are far away, latency is high.
            *   *Solution*: CDNs for static content, Edge Computing (Lambdas at the Edge), Multi-region DB replicas.
            *   *Bottleneck*: Speed of light. Consistency (syncing data across oceans implies high latency).
        3.  **Administrative (Management)**: Too many systems for one team to manage.
            *   *Solution*: Break into Microservices/Domains (Conway's Law).
            *   *Bottleneck*: Bureaucracy. Now you need API contracts and political negotiations to change a field name.
    *   **Vertical vs. Horizontal**:
        *   *Up (Vertical)*: Making the node stronger (RAM/CPU). Limited by physics/budget.
        *   *Out (Horizontal)*: Adding more nodes. Unlimited theoretically, but software complexity explodes (partitioning, sharding).

*   **Availability vs. Reliability** (They are NOT the same)
    *   **Reliability**: The probability that a system performs correctly for a duration. (No crashes, no wrong data).
    *   **Availability**: The probability that a system is operational at a point in time. (Uptime).
    *   *Paradox*: A system can be highly available (always responds) but unreliable (responds with 500 Errors).
    *   **The "Nines"**: 99.9% is easy. 99.999% (Five Nines) is 5 minutes of downtime *per year*. If you claim five nines, I assume you are lying or don't know how to measure.

*   **Performance**
    *   **Latency**: Time to complete one task (ms). "I want it FAST".
    *   **Throughput**: Tasks completed per unit time (RPS). "I want A LOT of it".
    *   *Trade-off*: Often, optimizing for throughput (batching) hurts latency.

*   **Security**
    *   **The Distributed Nightmare**:
        *   **Confidentiality**: Data in transit is now everywhere. TLS is mandatory, not optional.
        *   **Integrity**: Did the message get corrupted by a flaky switch?
        *   **Availability (Security)**: DDoS. In a distributed system, a client *is* a DDoS attacker if they retry fast enough (The "Thundering Herd" problem).

## Critical Theoretical Concepts

### Safety vs. Liveness
> *How to prove your code isn't just lucky.*

*   **Safety**: "Something bad will never happen."
    *   *Examples*: No deadlock, guaranteed mutual exclusion, no data corruption (ACID).
    *   *Violation*: Detectable in a finite trace (e.g., "Look, two processes wrote to the file at the same time!").
*   **Liveness**: "Something good will eventually happen."
    *   *Examples*: The request will eventually receive a response. The algorithm will eventually terminate.
    *   *Violation*: Cannot be detected in finite time (you never know if it's dead or just *really* slow).
*   *Battle Scar*: We usually sacrifice Liveness for Safety. (Better to hang than to corrupt data).

### Data in Distributed Systems: The ACID Myth

> *In a single node, ACID is law. In a distributed system, ACID is a negotiation.*

*   **ACID (The Gold Standard)**:
    *   **Atomicity**: All or nothing. (Distributed Transactions / 2PC are slow and block).
    *   **Consistency**: Valid state transitions.
    *   **Isolation**: Transactions don't interfere. (Serializable is expensive).
    *   **Durability**: Committed data survives power loss. (fsync() is your friend).
*   **The Reality Check (CAP/BASE)**:
    *   You cannot have ACID across partitions without sacrificing Availability (CAP Theorem).
    *   **BASE**: **B**asically **A**vailable, **S**oft State, **E**ventual consistency.
    *   *Trade-off*: Do you want to be right (ACID) or do you want to be online (BASE)? Amazon shopping cart is BASE (never reject an order). Bank transfer is ACID (never lose money).

## Communication Paradigms: Layered Protocols

> *How do we talk? It depends on who "we" are.*

*   **1. Object-Based Systems (RPC/RMI/CORBA)**
    *   **The Metaphor**: "Remote objects are just local objects that live far away."
    *   **Layering**:
        *   *Application*: `object.method(args)`
        *   *Middleware (Stub/Skeleton)*: Marshals arguments into bytes (Serialization). Handles finding the object (Naming Service).
        *   *Transport*: TCP/IP.
    *   **Example (Java RMI)**:
        *   You define an interface `Hello`.
        *   Server implements `Hello`.
        *   Client looks up `Hello` in registry and calls `hello.sayHi()`.
        *   *The Trap*: Hidden latency. Calling `getAccountBalance()` looks like a memory read, but it's actually a network round-trip.

*   **2. Resource-Based Systems (REST)**
    *   **The Metaphor**: "Everything is a Noun (Resource) manipulated by standard Verbs."
    *   **Layering**:
        *   *Application*: "I want the state of Resource X."
        *   *Middleware (Application Protocol)*: HTTP (GET, POST, PUT, DELETE). Uniform Interface.
        *   *Transport*: TCP/IP.
    *   **Example (AWS S3)**:
        *   Resource: `https://s3.aws.com/my-bucket/puppy.jpg`
        *   Operation: `PUT` (Upload), `GET` (Download).
        *   *The Win*: Decoupling. The server doesn't know about your object classes. It just knows typical web formats (JSON, XML).

*   **3. Microservices (Protocol Agnostic)**
    *   **The Metaphor**: "Small, autonomous services that do one thing well."
    *   **Layering**:
        *   *Application*: Domain Logic (e.g., "ProcessPayment").
        *   *Middleware (Service Mesh)*: Sidecars (Envoy/Istio) handle retries, circuit breaking, and mTLS. The app barely knows network exists.
        *   *Protocol*: Often gRPC (Object-like performance) or REST (Resource-like simplicity).
    *   **Example (Netflix/Uber)**:
        *   Service A calls Service B via gRPC.
        *   The *Network Layer* is managed by smart proxies. The *Application Layer* focuses on business logic.



