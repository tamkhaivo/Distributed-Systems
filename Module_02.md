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


## Software Architectures: The Critical Foundation

> *Research on software architectures has matured considerably, and it is now commonly accepted that designing or adopting an architecture is crucial for the successful development of large software systems.*

*   **The "Why" vs. The "How"**:
    *   Previous sections (Cluster, Grid, Cloud) dealt with *System Architecture* (the hardware/infrastructure topology).
    *   This section pivots to *Software Architecture* (the logical organization of code and state).
    *   *Insight*: You can run a Monolith on AWS (Cloud) or a Microservices mesh on a Mainframe (technically possible, morally wrong). The infrastructure enables; the software architecture creates value (and headaches).

*   **Maturity of the Field**:
    *   **The Go-to-Hell phase (1990s)**: "Just put it in a class." CORBA was trying to unify everything and failed under its own weight.
    *   **The Standardization phase (2000s)**: Patterns (Gang of Four), Layered Architecture became the bible.
    *   **The Fragmentation phase (2010s-Present)**: Microservices, Serverless, Event-Sourcing. We realized "one size fits none".

*   **Why It Matters (The "Principal" View)**:
    *   **Architecture = Constraints**: A good architecture doesn't tell you what you *can* do; it tells you what you *cannot* do. (e.g., "The UI cannot talk directly to the DB").
    *   **Irreversibility**: Ralph Johnson defined architecture as "the decisions that are hard to change later."
    *   **Scalability of Teams**: You need modularity not just for code, but so two teams can work without blocking each other. Conway's Law is the invisible hand of architecture.

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
    *   **Encapsulation & Interfaces**:
        *   Object-based architectures provide a natural way of *encapsulating data* (state) and *operations* (methods).
        *   The **interface** conceals implementation details. This means an object is (in principle) independent of its environment.
        *   *Implication*: Interfaces can reside on one machine while the implementing Object resides on another. This is the definition of a **Distributed Object** (or Remote Object).
    *   **The Mechanics (Proxies & Skeletons)**:
        *   **Client Side (The Proxy)**: When a client binds to a distributed object, it loads an implementation of the interface called a *Proxy*. This is the "Client Stub". It marshals arguments and sends them over the wire.
        *   **Server Side (The Skeleton)**: The server stub. It provides the means for the middleware to access user-defined objects. Often requires developer specialization.
    *   **State Distribution (The Counterintuitive Part)**:
        *   The *State* of the object is usually **NOT** distributed. It resides on a single machine.
        *   Only the *Interfaces* are distributed/available on other machines.
        *   *Insight*: You aren't "moving the object"; you are "moving the pointer to the object across the network".
    *   **Example (Java RMI)**:
        *   You define an interface `Hello`.
        *   Server implements `Hello`.
        *   Client looks up `Hello` in registry and calls `hello.sayHi()`.
        *   *The Trap*: Hidden latency. Calling `getAccountBalance()` looks like a memory read, but it's actually a network round-trip.

*   **2. Resource-Based Systems (REST)**
    *   **The Metaphor**: "Everything is a Noun (Resource) manipulated by standard Verbs."
    *   **Context (The Integration Nightmare)**:
        *   Service composition is hard. Connecting various components can turn into an "integration nightmare."
        *   *Alternative View*: View the distributed system as a huge collection of resources, individually managed by components.
    *   **REST (Representational State Transfer)**:
        *   Adopted for the Web [Fielding, 2000].
        *   Resources can be added, removed, retrieved, or modified by remote applications.
    *   **The 4 Key Characteristics [Pautasso et al., 2008]**:
        1.  **Single Naming Scheme**: Resources are identified through a ubiquitous scheme (URIs).
        2.  **Uniform Interface**: All services offer the same interface (GET, PUT, POST, DELETE). "At most 4 operations."
        3.  **Self-Describing Messages**: Messages contain enough info to process them (MIME types).
        4.  **Stateless Execution**: After executing an operation, the service forgets everything about the caller. (No sessions!).
    *   **Example (AWS S3)**:
        *   Resource: `https://s3.aws.com/my-bucket/puppy.jpg`
        *   Operation: `PUT` (Upload), `GET` (Download).
        *   *The Win*: Decoupling. The server doesn't know about your object classes. It just knows typical web formats (JSON, XML).
    *   **Critique: Where REST Fails**:
        *   **Simplicity vs. Complexity**: REST is popular because it's simple. But simple prohibits easy solutions to intricate schemes [Pautasso et al., 2008].
        *   **The Transaction Problem**: Distributed transactions need to keep track of *state of execution* (e.g., "Step 1 done, waiting for Step 2").
        *   *Conflict*: REST is stateless. Building a distributed transaction on top of stateless REST services is like trying to build a castle on quicksand. You end up managing state on the client side, which is heavy and error-prone.
        *   *Conclusion*: REST is perfect for simple integration (Mashups), but the myriad of interfaces in service-specific architectures might actually be better for complex state management.
    *   **Trade-off Deep Dive: Typed vs. Untyped (The "Bucket" Example)**:
        *   *Scenario*: Creating a bucket named "mybucket".
        *   **Option A (RPC/Object-Based)**:
            ```python
            import bucket
            bucket.create("mybucket")
            ```
            *   *Pro*: **Compile In Time Safety**. If you pass an integer, the compiler yells at you. Semantics are explicit in the interface.
        *   **Option B (REST)**:
            ```http
            PUT "https://mybucket.s3.amazonaws.com/"
            ```
            *   *Con (Risk)*: **Runtime checking**. The call is just a string. You won't know you made a typo until the request fails in production.
            *   *Pro (Flexibility)*: **Generic Operations**. Changing the underlying implementation doesn't break the client's code as long as the URL scheme remains valid. It's easier to evolve.

*   **3. Microservices (Protocol Agnostic)**
    *   **The Metaphor**: "Small, autonomous services that do one thing well."
    *   **From Objects to Services (The Evolution)**:
        *   Object-based architectures were the foundation: encapsulating state and operations.
        *   *The Step Up*: Microservices take this encapsulation to the process level. "The service as a whole is a self-contained entity."
        *   **SOA vs. Unix Philosophy**: Inspired by Unix ("small independent programs composed to form larger ones"), we moved to SOA, and then refined it to Microservices.
    *   **Key Characteristics**:
        *   **Process Isolation**: Essential. Each microservice runs as a separate network process. (It *can* be a remote object, but doesn't have to be).
        *   **Independence**: Modularization is key. [Wolff, 2017].
        *   **Size Debate**: "There is no common agreement on what the size of such a service should be." (Is it 10 lines of code? Is it a whole domain?).
        *   *Principal's Take*: If it takes two pizzas to feed the team maintaining it, it's too big (Bezos). If it takes a distributed transaction to rename a user, it's too small.
    *   **The Placement Problem (Edge/Fog)**:
        *   Since they are separate processes, we have a choice of *where* to place them.
        *   *Modern Context*: Deployment orchestration across Cloud, Fog, and Edge is the new frontier.
    *   **Example (Netflix/Uber)**:
        *   Service A calls Service B via gRPC.
        *   The *Network Layer* is managed by smart proxies (Service Mesh). The *Application Layer* focuses on business logic.
    *   **The Harmony of Composition**:
        *   Developing a distributed system is partly a problem of **Service Composition**: making sure services operate in harmony.
        *   *Analogy*: It's identical to the Enterprise Application Integration (EAI) issues.
        *   **Crucial Rule**: Each service MUST offer a well-defined interface. Without that, you don't have harmony; you have a cacophony of broken contracts.

*   **4. Publish-Subscribe Architectures**
    *   **The Metaphor**: "Talking to everyone, listening to no one specific."
    *   **The Problem**: As systems grow, processes need to join/leave easily. We need **Loose Coupling**.
    *   **Coordination Models (Taxonomy by Cabri et al. [2000])**:
        *We can classify coordination along two dimensions: **Temporal** (Time) and **Referential** (Name).*
        
        | Model | Referential Coupling (Who?) | Temporal Coupling (When?) | Example |
        | :--- | :--- | :--- | :--- |
        | **Direct** | Coupled (Must know Name) | Coupled (Must be active now) | Cell Phone call |
        | **Mailbox** | Coupled (Must know Name) | Decoupled (Can read later) | Email |
        | **Event-Based** | **Decoupled** (Don't know who) | Coupled (Must be active now) | Pub/Sub (Notification) |
        | **Shared Data Space** | **Decoupled** (Don't know who) | **Decoupled** (Can read later) | Tuple Spaces (JavaSpaces) |

    *   **Direct Coordination**:
        *   Referentially & Temporally coupled.
        *   *Analogy*: Cell phone call. I need your number (Reference) and you need to answer now (Time).
    *   **Mailbox Coordination**:
        *   Referentially coupled, Temporally decoupled.
        *   *Analogy*: Email. I need your address, but you can read it tomorrow.
    *   **Event-Based Coordination (Pub/Sub)**:
        *   Referentially Decoupled, Temporally Coupled.
        *   *Mechanism*: Publishers publish notifications. Subscribers signal interest. The **Event Bus** matches them.
        *   *Constraint*: Generally requires the subscriber to be up-and-running to receive the event.
    *   **Deep Dive: The Mechanics of Events**:
        *   **Naming & Subscriptions**:
            *   *Topic-Based*: "I want everything on channel `#distsys`." (Keywords, Attribute-Value pairs).
            *   *Content-Based*: "I want events where `temperature > 30` AND `humidity < 50`." (Range queries, Predicates).
        *   **The Matching & Delivery Problem**:
            *   *Push Model*: Middleware matches and immediately forwards data to subscribers. (No storage, Temporally Coupled).
            *   *Pull Model*: Middleware sends a "notification only". Subscriber must call back to fetch data. (Middleware needs storage/leases).
        *   **Complex Event Processing (CEP)**:
            *   Real world isn't isolated. "Notify when room is unoccupied AND door is unlocked."
            *   *Challenge*: Composing primitive events from dispersed sensors is hard.
        *   **The Scalability Paradox**:
            *   Pub/Sub is praised for loose coupling (great for scale).
            *   BUT, the *matching logic* (especially Content-Based) is a bottleneck. Checking every event against 1 million SQL-like queries is slow.
            *   Security/Privacy also complicates matching (can I trust the middleware to read my content?).
    *   **Shared Data Spaces (The Holy Grail?)**:
        *   Referentially & Temporally Decoupled.
        *   *Implementation*: **Tuples**. Structured data records (like a DB row) put into a shared space.
        *   *Retrieval*: Associative search (Pattern matching). "Give me any tuple that looks like `{Type: Job, Status: Pending}`".
        *   *Insight*: This is the loosest coupling possible. Processes don't know each other exists, and they don't even have to exist at the same time.

    *   **Deep Dive: Linda Tuple Spaces (The "Old School" Cool)**:
        *   **Origin**: Developed by Carriero & Gelernter (1989).
        *   **Operations**:
            *   `out(t)`: Write tuple `t` to space.
            *   `in(t)`: Read *and remove* matching tuple (Blocking).
            *   `rd(t)`: Read *copy* of matching tuple (Blocking).
        *   **Example: The Microblog (Alice & Bob)**:
            *   *Concept*: Messages are tuples `<string poster, string topic, string content>`.
            *   *Alice's Code (Poster)*:
                ```python
                blog = linda.universe._rd(("MicroBlog", linda.TupleSpace))[1]
                blog._out(("alice", "gtcn", "This graph theory stuff is not easy"))
                blog._out(("alice", "distsys", "I like systems more than graphs"))
                ```
            *   *Bob's Code (Poster)*:
                ```python
                blog = linda.universe._rd(("MicroBlog", linda.TupleSpace))[1]
                blog._out(("bob", "distsys", "I am studying chap 2"))
                ```
            *   *Chuck's Code (Reader)*:
                ```python
                # Read any message about 'distsys' from anyone (Pattern Matching)
                # The 'str' is a wildcard
                t = blog._rd((str, "distsys", str)) 
                print(f"Found post on distsys: {t}")
                ```
        *   *The Magic*: Chuck doesn't know Alice exists. Alice doesn't know Chuck exists. They communicate purely through the *content* of the data. This is **Generative Communication**.

## 2.2 Middleware Organization

> *How do we build this magic layer without it becoming a monolithic mess?*

### 2.2.1 Wrappers (The "Adapter" Pattern)
*   **The Problem**: Legacy components have incompatible interfaces. You can't just rewrite the Mainframe.
*   **The Solution**: A **Wrapper** (or Adapter) transforms the client's preferred interface into the component's native interface.
*   **Example (Amazon S3)**: S3 has a REST interface (HTTP) and a traditional interface. The HTTP server acts as an adapter, dissecting a `PUT` request and handing it off to the internal storage engine.
*   **The Scalability Trap ($O(N^2)$ problem)**:
    *   If you have N applications, and each needs to talk to every other, you need $N \times (N-1)$ wrappers. This explodes.
    *   **The Broker Solution**: Use a centralized Broker (like a Message Broker). Everyone talks to the Broker.
    *   *Result*: You only need $2N$ wrappers (one for each app to talk to the broker). $O(N^2) \rightarrow O(N)$.

### 2.2.2 Interceptors (AOP for Distributed Systems)
*   **Concept**: Software constructs that break the flow of control to execute specific code *transparently*.
    *   *Why?* To achieve **Openness**. You can add functionality (replication, security, logging) without changing the object's code.
*   **How it works (The 3 Steps)**:
    1.  **Call**: Object A calls `B.doit(val)`.
    2.  **Transformation**: The call is turned into a generic invocation `invoke(B, &doit, val)`.
    3.  **Transmission**: The generic invocation is sent over the network.
*   **Types of Interceptors**:
    *   **Request-Level**: Acts at Step 2.
        *   *Use Case*: **Replication**. The interceptor catches `invoke(B)` and redirects it to `Replica1.doit()`, `Replica2.doit()`, etc. Object A has no idea B is replicated.
    *   **Message-Level**: Acts at Step 3 (OS/Network level).
        *   *Use Case*: **Fragmentation**. If `val` is a 1GB array, the interceptor chops it into packets. The middleware doesn't even need to know.

### 2.2.3 Modifiable Middleware
*   **The Driver**: The environment changes.
    *   **Mobility**: Users move, IP addresses change.
    *   **QoS Variance**: Bandwidth drops, latency spikes.
    *   **Hardware Failure**: Nodes die. Batteries drain.
*   **The Concept**: Middleware shouldn't just be *adaptive* (reacting to changes); it should be **Modifiable** [Parlavantzas & Coulson 2007].
    *   *Goal*: Change behavior *without* shutting down the system (Dynamic reconfiguration).
*   **Component-Based Design**:
    *   **Composition**: Build middleware from small, swappable components.
    *   **Late Binding**: Decide *which* component to load at runtime (like loading a DLL/Shared Object).
    *   **The Hard Part**: State Management.
        *   If you swap out a TCP component for a UDP component while a transfer is active, what happens to the buffer?
        *   *Principal's Take*: "Hot-swapping code is like changing a tire while driving on the highway. Theoretically possible; practically suicidal unless you really know what you're doing."
## 2.3 Layered System Architectures

> *The "Vanilla Flavor" of Distributed Systems. Boring, but it works.*

*   **Definition**: Organizing components into layers (Clients requesting services from Servers).
*   **Key Insight**: Thinking in terms of Client/Server helps manage complexity [Saltzer & Kaashoek 2009].

### 2.3.1 Simple Client-Server Architecture (Request-Reply)
*   **The Model**: 
    1.  Client packs a request (Service ID + Data).
    2.  Server processes it.
    3.  Server packs a reply.
*   **Protocol Choice**:
    *   **Connectionless (UDP)**:
        *   *Pro*: Fast. No setup handshake.
        *   *Con*: Unreliable. Packets get lost.
        *   *The "Retry" Trap*: Using UDP requires the client to handle retries. 
    *   **Connection-Oriented (TCP)**:
        *   *Pro*: Reliable. Order guaranteed.
        *   *Con*: Slow setup (3-way handshake). Costly for short messages.
        *   *Reality*: HTTP/3 (QUIC) is moving back to UDP to avoid TCP's head-of-line blocking. History repeats itself.

*   **Handling Failures: The "Idempotency" Rule**:
    *   If a client sends a request and gets no reply, was the request lost? Or the reply?
    *   *Scenario A*: "Tell me my balance." -> **Safe to retry** (Idempotent).
    *   *Scenario B*: "Transfer $10k." -> **Huge Risk**. Retrying might drain your account.
    *   *Principal's Take*: "Make everything idempotent. Use unique Request IDs. If the server sees ID #12345 again, it should return the *cached result* of the previous execution, not run it again."

### 2.3.2 Multitiered Architectures
*   **The Logical Layers**:
    1.  **User Interface Layer**: Presentation (UI/GUI).
    2.  **Processing Layer**: Business Logic.
    3.  **Data Layer**: Persistence (Database/File System).
*   **Two-Tiered Organizations**:
    *   **Thin Client**: Client only does UI. Server does Processing + Data.
        *   *Example*: X11 Window System, old Mainframe terminals.
        *   *Pro*: Easy to manage client (it's dumb).
        *   *Con*: Server bottleneck. Heavy network traffic for UI updates.
    *   **Fat Client**: Client does UI + Processing. Server does Data (DB).
        *   *Example*: A Desktop Banking App connecting to a SQL backend.
        *   *Pro*: Leveraging client CPU. Better UX.
        *   *Con*: **Management Hell**. Updating the app on 10,000 PCs is a nightmare. Vulnerable to "DLL Hell".
    *   **The Modern Swing**:
        *   We moved from Thin (Mainframes) to Fat (PCs) back to Thin (Web).
        *   *Cloud Computing*: Essentially a return to Thin Clients (Browser) with massive Server-Side processing.
*   **Three-Tiered Architecture (The Web Standard)**:
    *   **Layer 1**: Client (Browser).
    *   **Layer 2**: Application Server (Business Logic - Java/Python/Node).
    *   **Layer 3**: Database Server (Data - SQL/NoSQL).
    *   *Role*: The App Server acts as a client to the DB, but a server to the Browser.
    *   *Analogy*: Waiter (App Server) takes order from Customer (Client) and gives it to Chef (DB).

### 2.3.3 Example: The Network File System (NFS)
*   **Concept**: Transparent access to remote files.
*   **The Models**:
    *   **Remote Access Model (NFS)**:
        *   Client stays on client. Server stays on server.
        *   Operations (`read`, `write`) are sent over network.
        *   *Pro*: Don't need to copy the whole 10GB file to read the first 1KB.
    *   **Upload/Download Model (FTP/Git)**:
        *   Move the file to client. Modify it. Move it back.
        *   *Pro*: Simple. Good for offline work.
*   **NFS Architecture (The "VFS" Magic)**:
    *   **Goal**: The application shouldn't know it's accessing a remote file.
    *   **Mechanism**:
        1.  **System Call**: App calls `read(fd, buf)`.
        2.  **VFS (Virtual File System)**: The kernel interface that hides implementation.
        3.  **Decision**:
            *   If local: Send to `ext4` or `ntfs` driver.
            *   If remote: Send to `NFS Client` driver.
        4.  **RPC**: NFS Client packs the `read` request into an RPC and sends it to the server.
        5.  **Server side**: NFS Server receives RPC -> VFS -> Local FS -> Disk.
    *   *Insight*: VFS is the unsung hero of OS interoperability. It allows a Linux box to mount a Windows share (CIFS) and an NFS share simultaneously.

### 2.3.4 Example: The Web
*   **The Evolution**: From passive documents to dynamic services.
*   **Phase 1: Simple Web-Based Systems (Static)**
    *   **Architecture**: Two-Tiered (Browser <-> Server).
    *   **Core Concepts**:
        *   **URL**: Name + Protocol (`http`) + Path.
        *   **HTTP**: Stateless Request-Reply protocol.
        *   **HTML**: Markup Language. Instructions for the browser on how to render.
    *   *Mechanism*: Client sends `GET /index.html`. Server reads file from disk. Server sends bytes. Done.
*   **Phase 2: Dynamic Content (The "Multitiered" Shift)**
    *   **CGI (Common Gateway Interface)**:
        *   *The Shift*: Instead of reading a file, the server executes a program (Perl/Bash/C).
        *   *Flow*: Request -> Server -> Fork Process -> Program runs -> Output (HTML) -> Server -> Client.
        *   *Pro*: Infinite flexibility.
        *   *Con (Scale)*: Forking a process for every request is expensive.
    *   **Server-Side Scripting (PHP/ASP/JSP)**:
        *   *Concept*: Embed the logic *inside* the HTML document.
        *   *Example (PHP)*: `<strong> <?php echo $_SERVER['REMOTE_ADDR']; ?> </strong>`.
        *   *Mechanism*: Server parses the file, executes the code blocks, replaces them with the output, and sends the final HTML.
        *   *Impact*: This lowered the barrier to entry, leading to the explosion of the "LAMP Stack" (Linux, Apache, MySQL, PHP).
    *   *Principal's Take*: "We traded the simplicity of static files for the power of dynamic generation. Now we spend all our time caching the dynamic stuff to make it look like static files again (CDN, Varnish, Redis). Time is a flat circle."

## 2.4 Symmetrically Distributed System Architectures

> *Where everyone is equal, and everything is harder to find.*

### 2.4.1 Horizontal vs. Vertical Distribution
*   **Vertical Distribution (Multitiered)**:
    *   Splitting by **Function**. (UI tier, Logic tier, Data tier).
    *   *Analogy*: An assembly line. One person bolts, one person welds.
*   **Horizontal Distribution (Peer-to-Peer)**:
    *   Splitting by **Data/Load**.
    *   *Concept*: Every node is logically equivalent.
    *   *Analogy*: A potluck dinner. Everyone brings food, everyone eats.
    *   *The Servant*: Each process acts as both a **Server** and a **Client** simultaneously.

### 2.4.2 Peer-to-Peer (P2P) Systems
*   **Overlay Networks**:
    *   Nodes form a virtual network (Overlay) on top of the physical network (TCP/IP).
    *   *Link*: A logical connection (knowing a peer's IP).
*   **Structured P2P Systems**:
    *   **Goal**: Efficient Data Lookup. "Who has the file with ID `1234`?"
    *   **Mechanism**: **Distributed Hash Table (DHT)**.
        *   `Key = Hash(Data Value)`
        *   `Node ID = Hash(IP)`
        *   The system maps keys to Node IDs deterministically.
        *   *Semantic-Free Index*: The key tells you *nothing* about the data (it's just a number), only *where* to find it.

#### Example: The Chord System
*   **Topology**: A Logical Ring ($0$ to $2^m - 1$).
*   **Rule**: A key $k$ is stored at the **successor** node $succ(k)$ (the first node with $ID \ge k$).
*   **Routing (Finger Tables)**:
    *   Naive approach: Pass request to neighbor. $O(N)$ lookup (Slow!).
    *   Chord approach: Maintain shortcuts. Node $x$ knows about Nodes $x+1, x+2, x+4, ...$.
    *   *Result*: Lookup complexity drops to $O(\log N)$.
*   **Principal's Insight**:
    *   "Chord is mathematically beautiful but operationally brutal. In a high-churn network (nodes joining/leaving constantly), updating those finger tables becomes a full-time job for the network. Theory says $O(\log N)$; Reality says 'Connection Timeout'."

### 2.4.2 Unstructured Peer-to-Peer Systems
*   **The Chaos**: No deterministic topology. Constant churn.
*   **Graph Theory**: Random Graphs. Edge $<u,v>$ exists with probability $P$.
*   **Search Mechanisms (The Need to Hunt)**:
    *   **Flooding**:
        *   *Idea*: Shout "Who has file X?" to all neighbors. Neighbors shout to their neighbors.
        *   *Control*: **Time-to-Live (TTL)**. Stop forwarding after $k$ hops.
        *   *Risk*: Network storm. Exponential message growth.
    *   **Random Walks**:
        *   *Idea*: Ask *one* random neighbor. If they don't have it, they ask one of theirs.
        *   *Walker Efficiency*: Spawning $n=16$ walkers is often faster and less traffic-heavy than flooding.
        *   *Math*: With replication factor $r$, average search size $S \approx N/r$.

### 2.4.3 Hierarchically Organized P2P Networks
*   **The Compromise**: Pure P2P is too slow. Centralized is too fragile.
*   **Super Peers**:
    *   *Role*: Nodes with high availability/bandwidth act as local servers for "Weak Peers".
    *   *Architecture*: Weak Peers connect to a Super Peer. Super Peers connect to each other.
    *   *Example*: Skype (early versions), KaZaA.
    *   *Dynamic selection*: If a Super Peer dies, a Weak Peer can be promoted (Leader Election).

### 2.4.4 Example: BitTorrent
*   **Philosophy**: "Collaborative Downloading".
    *   *Free Riding*: The plague of P2P. People download but don't upload.
    *   *Solution*: **Tit-for-Tat**. If you don't upload to me, I "choke" (stop uploading to) you.
*   **Architecture**:
    *   **Torrent File**: Metadata (names, hashes of chunks).
    *   **Tracker**: A server that introduces peers. "Here is a list of IPs downloading this file."
    *   **Leecher**: Currently downloading (and uploading chunks they have).
    *   **Seeder**: Has the full file, only uploading.
*   **Evolution (Hybrid -> Decentralized)**:
    *   Centralized Trackers were Single Points of Failure (and legal targets).
    *   *Modern BitTorrent*: Uses a **DHT** (Distributed Hash Table, see 2.4.1) to find peers without a central tracker. **Magnet Links** replace .torrent files.

## 2.5 Hybrid System Architectures
> *Real-world systems are rarely pure. They are messy constructs of "Use whatever works".*

### 2.5.1 Cloud Computing
*   **The Utility Model**: Computing as a metered service (like Electricity).
*   **The Layers**:
    *   **IaaS (Infrastructure-as-a-Service)**: Renting raw VMs/Storage (AWS EC2).
    *   **PaaS (Platform-as-a-Service)**: Renting a runtime (Google App Engine).
    *   **SaaS (Software-as-a-Service)**: Renting an App (Gmail, Salesforce).
    *   **FaaS (Function-as-a-Service)**: Renting a single function execution (AWS Lambda).
*   *Observation*: The "Client" sees a monolithic service; the "Provider" runs a massive distributed system behind the curtain.

### 2.5.2 The Edge-Cloud Architecture
*   **The Problem**: The Cloud is far away (Latency). The bandwidth is finite cost.
*   **The Solution**: Move compute closer to the data source (IoT).
*   **Drivers**:
    *   **Latency**: Autonomous cars need <10ms response. Cloud is ~100ms.
    *   **Privacy**: Keep medical data on-premise (Edge), send anonymized stats to Cloud.
    *   **Edge vs Fog**: *Fog* is often used for the immediate tier above the Edge but below the Cloud.

### 2.5.3 Blockchain Architectures
*   **Concept**: A specific type of **Distributed Ledger**.
*   **Key Trust Model**:
    *   Banks = Trusted Third Party.
    *   Blockchain = **Trustless**. We assume everyone might be malicious.
*   **Mechanism**:
    1.  Transactions are grouped into **Blocks**.
    2.  Blocks are chained using Hashes (Immutability).
    3.  **Consensus**: The network agrees on the "valid" chain.
*   **Types**:
    *   **Permissioned (Private)**: Known validators (Consortiums). Faster, essentially a distributed database.
    *   **Permissionless (Public)**: Anyone can validate (Bitcoin). Slow, requires expensive Proof-of-Work/Stake to prevent sybil attacks.
*   *Principal's Take*: "Blockchain is the most expensive way to store a CSV file. Use it only when you absolutely cannot trust a single central authority."

## 2.6 Summary
*   **Architectural Styles**: Layered, Object-based, Event-based, Shared Data Space.
*   **Organization**:
    *   **Vertical**: Multitiered (Client-Server). Functional splitting.
    *   **Horizontal**: Peer-to-Peer. Data/Load splitting.
    *   **Hybrid**: Cloud, Edge, Blockchain.
*   **The Golden Rule**: There is no "perfect" architecture. There are only trade-offs between **Latency**, **Consistency**, **Complexity**, and **Cost**.



