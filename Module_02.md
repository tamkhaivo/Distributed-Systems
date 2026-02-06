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



