# Distributed Systems Midterm 1 - Cheat Sheet

**Q: In the simple queuing model of a centralized service, if the utilization U increases from 0.5 to 0.8, what is the effect on the response-to-service time ratio $\frac{S}{R}$?**
- **A:** The ratio increases from 2 to 5. Using the formula $\frac{S}{R} = \frac{1}{1-U}$, the ratio at $U=0.5$ is 2 and at $U=0.8$ is 5.

**Q: Name all types of transparencies and explain the difference between them.**
- **Access transparency:** Hides differences in data representation and how a resource is accessed.
- **Location transparency:** Hides where a resource is located.
- **Migration transparency:** Hides that a resource may move to another location.
- **Relocation transparency:** Hides that a resource may be moved to another location while in use.
- **Replication transparency:** Hides that a resource is replicated.
- **Concurrency transparency:** Hides that a resource may be shared by several competitive users.
- **Failure transparency:** Hides the failure and recovery of a resource.

**Q: How do request-level interceptors differ from message-level interceptors in an object-based distributed system?**
- **A:** Request-level interceptors operate at a higher logical level (right after a generic object invocation) and handle logic like *replication* transparency. Message-level interceptors operate at a lower level (close to the OS network stack) and handle tasks like network *fragmentation* or compression.

**Q: In the context of scaling techniques, what is the primary drawback of using replication to improve performance?**
- **A:** It can lead to significant consistency problems that require expensive global synchronization to resolve.

**Q: Which of the following describes the 'Monotonic Reads' client-centric consistency model?**
- **A:** If a process reads the value of a data item $x$, any successive read on $x$ by that process will always return that same value or a more recent one.

**Q: In Lamport’s logical clocks, what does the condition $L(a) < L(b)$ imply about the relationship between events $a$ and $b$?**
- **A:** It implies that under the total ordering established by the logical clocks, $a$ comes before $b$. However, this does **not** guarantee that $a$ causally happened before $b$. (Remember: If $a \rightarrow b$, then $L(a) < L(b)$, but $L(a) < L(b)$ does not necessarily mean $a \rightarrow b$).

**Q: What is the primary trade-off when comparing Virtual Machines (VMs) to Containers in distributed systems?**
- **A:** **Isolation vs. Performance.** VMs provide strong isolation by virtualizing the entire hardware stack (including a guest OS), but require more resources and slower startup times. Containers share the host OS kernel, making them lightweight, fast, and less resource-intensive, but offer weaker isolation.

**Q: Which metric is used to measure the ratio of the delay between two nodes in an overlay network to the delay between them in the underlying physical network?**
- **A:** Stretch (or Relative Delay Penalty / RDP).

**Q: In the 'Bully' election algorithm, what happens if a process $P$ with a high identifier notices the coordinator has failed?**
- **A:** $P$ holds an election by sending an `ELECTION` message to all processes with identifiers higher than its own. If no higher process responds, $P$ wins and broadcasts a `COORDINATOR` message to all lower-ID processes. If a higher process responds, $P$ steps back and simply waits for them to take over.

**Q: What is the primary function of a Transaction Processing (TP) monitor in an enterprise information system?**
- **A:** A TP monitor acts as a middleware component that securely coordinates the execution of distributed transactions across multiple heterogeneous systems or databases, ensuring ACID properties are maintained (typically using a 2-Phase Commit protocol).

**Q: In the context of fault tolerance, how does 'Reliability' differ from 'Availability'?**
- **A:** **Reliability** is a measure of uninterrupted continuous operation (e.g., MTBF). **Availability** is the probability or fraction of time that a system is operational and accessible at a given moment (e.g., 99.999% uptime). A system can be highly available if it crashes often but recovers instantly, but it would have low reliability.

**Q: What is the purpose of a 'Death Certificate' (Tombstone) in the context of gossip-based data dissemination?**
- **A:** In gossip protocols, simply deleting a local record would cause other nodes to eventually gossip the "missing" record back, effectively resurrecting it. A death certificate is a special marker circulated to prove that an item was intentionally deleted, ensuring the deletion propagates through the network.

**Q: Which design principle for secure systems argues against relying on the secrecy of a system's implementation details?**
- **A:** Kerckhoffs's Principle ("security by obscurity is a bad idea"). A system should remain secure even if its entire inner workings (except for the cryptographic keys) are public knowledge.

**Q: In the 'Flat Naming' scheme, how does a Home-based approach resolve the location of a mobile entity?**
- **A:** Every mobile entity has a fixed "home" location. When the entity moves, it acquires a temporary "care-of-address" and updates its home location. Clients resolving the entity always query the home location first to retrieve the current care-of-address.

**Q: What is the primary purpose of a 'Skeleton' in the context of remote-object invocation?**
- **A:** It acts as the server-side stub. It unmarshals incoming network requests, directly invokes the actual method on the standard server object, marshals the result, and sends the reply back to the client.

**Q: In message-oriented persistent communication, what is a key characteristic of the communication middleware?**
- **A:** The middleware permanently stores messages until they are delivered to the receiver. This allows asynchronous communication where the sender and receiver do strictly not need to be active or connected simultaneously.

**Q: Which design pitfall involves assuming that the structure of the network remains constant over time?**
- **A:** "Topology does not change" — this is one of the classic Eight Fallacies of Distributed Computing.

**Q: How does the 'Token-Ring' mutual exclusion algorithm handle the loss of a token?**
- **A:** It requires a detection mechanism (usually a timeout) to realize the token is lost. Once detected, an election algorithm must be invoked to appoint a coordinator responsible for generating a new token. (The difficulty is distinguishing a lost token from a token that is simply being held for a long time).

**Q: In a distributed system, what is the 'Stretch' (or RDP) of a route that takes 120 ms in the overlay but only 40 ms in the underlying network?**
- **A:** $Stretch = \frac{\text{Overlay Delay}}{\text{Physical Delay}} = \frac{120}{40} = 3$.

**Q: What is 'Code Migration' in heterogeneous systems primarily concerned with?**
- **A:** It is concerned with safely moving executable code or active processes to machines with totally different hardware architectures or underlying operating systems. It generally relies on virtual machines or intermediate representations (like Java bytecodes) to provide a standardized execution environment.

**Q: Which of the following describes a 'Value Failure' in a distributed system?**
- **A:** A server processes the request and responds, but the value returned is incorrect or corrupted (a semantic error), rather than failing to respond at all (omission failure).

**Q: In a 'Service-Oriented Architecture' (SOA), what does 'Loose Coupling' imply?**
- **A:** Services interact over well-defined, standardized interfaces without making assumptions about each other's internal state, underlying technology, or exact location. They can be updated, scaled, or replaced entirely independently as long as the interface contract holds.

**Q: What is the primary motivation for 'Edge Computing' in a cloud-based hierarchy?**
- **A:** To move computation, data processing, and storage physically closer to the data sources (such as IoT devices or local users) to drastically reduce latency, minimize internet bandwidth usage, and maintain partial offline functionality.

**Q: How do 'Vector Clocks' improve upon 'Lamport Clocks'?**
- **A:** While Lamport clocks provide a total order, they cannot reliably capture causality. Vector Clocks maintain a vector of counters (one per process), enabling exact causal tracking: $V(a) < V(b)$ holds true **if and only if** event $a$ causally preceded event $b$.

**Q: In the context of 'Openness,' what is the role of an 'Interface Definition Language' (IDL)?**
- **A:** It acts as a strict, language-agnostic contract defining the syntax of services, parameters, and return types. This allows software components written in different programming languages to communicate predictably over RPCs.

**Q: What is 'Link Stress' in an application-level multicast network?**
- **A:** It is the number of identical copies of a specific multicast message that redundantly traverse the exact same underlying physical network link due to the routing topology of the overlay network.

**Q: In 'Attribute-based Naming,' how are entities typically resolved?**
- **A:** Entities are described by sets of key-value properties. Resolution involves querying a directory service (like LDAP) with a search specifying desired attributes, which then returns a list of matching entities.

**Q: What happens during the 'Downtime' phase of virtual machine migration?**
- **A:** After live pre-copying of memory, the "downtime" or "stop-and-copy" phase briefly pauses the VM on the source host. During this short window, the final modified memory pages and the CPU state are transferred to the destination host, and the VM is resumed. The VM is entirely unresponsive during this interval.

**Q: In the context of distribution transparency, why might a system intentionally sacrifice failure transparency?**
- **A:** Fully masking failures can lead to unbounded latency or mask fatal errors (e.g., trying to endlessly reconnect to a dead sensor). It is often better to expose the failure to the application or user so they can make an informed decision (like canceling an operation) rather than hanging indefinitely.

**Q: Which mechanism is specifically responsible for ensuring that a container does not monopolize the host machine's CPU or memory?**
- **A:** Linux Control Groups (`cgroups`). While `namespaces` provide isolation of resources (like PIDs and networks), `cgroups` enforce limits and quotas on the usage of physical resources like CPU, memory, and disk I/O.

**Q: In a three-tiered architecture, what is the primary role of the middle tier?**
- **A:** The middle tier (Application or Business Logic tier) is responsible for executing the core processing and business rules. It bridges the gap between the Presentation tier (UI layer) and the Data tier (database).

**Q: What is a 'connector' in the context of software architecture styles?**
- **A:** A connector is a mechanism that mediates communication, coordination, or cooperation among components. Examples include RPCs, message brokers, shared memory, or event buses.

**Q: Why is 'referential decoupling' a key characteristic of publish-subscribe systems?**
- **A:** It means publishers and subscribers do not need to know of each other's existence. Publishers simply broadcast events to a topic, and subscribers independently consume from that topic. This allows components to be added, removed, or scaled entirely independently without breaking connections.


**Q: Which of the following is an example of 'horizontal distribution' in a system architecture?**
- **A:** Partitioning data across multiple machines (e.g., Sharding a database so Server A holds users A-M and Server B holds users N-Z) or replicating an identical service across multiple servers behind a load balancer.

**Q: What is the primary advantage of using a 'hosted' virtual machine monitor (Type 2) over a 'native' one (Type 1)?**
- **A:** Type 2 VMMs run as applications on top of a standard host OS (like Windows or macOS). Their primary advantage is ease of setup, hardware compatibility (they leverage the host OS device drivers), and the ability for the user to run regular applications alongside the VM. (Type 1 provides better performance by running directly on the hardware).

**Q: In message-oriented persistent communication, how are messages typically handled when the destination is temporarily unavailable?**
- **A:** The middleware (e.g., a Message Broker) stores the message safely on disk or in persistent memory. It will continuously try to deliver the message later when the destination comes back online.

**Q: Which mechanism allows middleware to be adapted at runtime to handle new requirements like replication without modifying the application code?**
- **A:** Interceptors (such as request-level or message-level interceptors).
**Q: What is the 'expansive view' of distributed systems as described in the text?**
- **A:** It is the perspective that a distributed system acts as an open infrastructure (like the internet or a massive microservice architecture) where completely independent computers seamlessly cooperate and integrate into a single coherent system from the outside.

**Q: In the context of scalability, what does 'administrative scalability' refer to?**
- **A:** It refers to how easily a system can be managed exactly as it grows across multiple independent organizations or administrative domains. (e.g., A system crossing two different companies brings massive security, payment, and policy issues that are hard to scale).

**Q: What is the primary drawback of using 'pure' layered architectures for all communication?**
- **A:** Pure layering requires every request to pass strictly downwards through every single layer beneath it (and jump back up layer by layer), introducing significant latency and overhead compared to allowing an upper level to skip layers and talk directly to the OS or network layer.

**Q: In the 'Namespace' mechanism for Linux containers, why is the `unshare` command used?**
- **A:** The `unshare` command runs a program in a completely new, isolated namespace (like a new Process ID or Network namespace), disassociating it from the parent's namespaces, which is the foundational building block for containerizing a process.
**Q: Which of the following best describes the 'Representational State Transfer' (REST) architectural style?**
- **A:** It is a resource-based architectural style where everything is treated as a component (a resource) uniquely identified by a URI. Operations on resources are strictly defined by standard stateless HTTP methods (GET, PUT, POST, DELETE).

**Q: What is a 'death certificate' in the context of gossip-based data dissemination?**
- **A:** *(Duplicate concept)* Also known as a "tombstone", it is a special marker circulated to prove that an item was intentionally deleted so other gossiping nodes don't assume the data was accidentally lost and "resurrect" it.

**Q: Why is 'concurrency transparency' difficult to implement when scalability is a primary goal?**
- **A:** True concurrency transparency ensures concurrent access looks perfectly sequential (often requiring strict locking Mechanisms like 2PL). Locking resources fundamentally limits concurrency, creating a massive bottleneck that inherently prevents the system from scaling to handle massive simultaneous load.

**Q: What is the primary function of a 'Transaction Processing (TP) monitor' in a distributed information system?**
- **A:** *(Duplicate concept)* It acts as a middleware coordinator for distributed transactions across multiple heterogeneous systems or databases, ensuring ACID properties using a 2-Phase Commit protocol.

**Q: In the context of MPI (Message Passing Interface), what is the behavior of the `MPI_SSEND` operation?**
- **A:** It is a **Synchronous Send**. The sender blocks andwaits not just until the message has safely left its local buffer, but until the destination process has actually started explicitly receiving the message.

**Q: What is a 'hard link' in the context of a naming graph?**
- **A:** A hard link points directly to the underlying physical resource or inode, meaning the physical data is not deleted from disk until every single hard link pointing to it is removed. (As opposed to a symbolic link, which just points to a *name*).

**Q: How does 'asynchronous RPC' differ from a traditional RPC?**
- **A:** In traditional RPC, the calling client thread completely blocks and waits indefinitely for the server's reply. In asynchronous RPC, the client sends the request and immediately continues executing its own code; the server's reply (if any) is handled later via a callback or polling mechanism.

**Q: In a 'client-centric consistency' model like 'Read Your Writes,' what is guaranteed to the user?**
- **A:** It guarantees that if a process/client updates a data item, any subsequent read operation done by *that exact same process/client* on that data item will always reflect the updated value, regardless of which physical replica they connect to.

**Q: What is the primary purpose of a 'message broker' in enterprise application integration?**
- **A:** It sits between senders and receivers to decouple them temporally and referentially. Its primary purpose is to receive messages, translate them if necessary, and route them asynchronously to the correct destination(s) using queues or publish-subscribe topics.

**Q: According to the text, what is one of the 'pitfalls' of distributed systems development?**
- **A:** Believing any of the "Eight Fallacies of Distributed Computing" (e.g., the network is reliable, latency is zero, bandwidth is infinite, the network is secure).

**Q: What does 'temporal coupling' imply in process communication?**
- **A:** It implies that both the sender and the receiver must be actively running and connected at the exact same time for communication to happen (e.g., standard TCP/IP or RPCs). Message brokers and queues eliminate temporal coupling (providing *temporal decoupling*).

**Q: In a 'mixed layered organization,' how can an application at Layer N interact with a service at Layer N−3?**
- **A:** The system permits "downcalls" where a higher layer can completely bypass intermediate layers and communicate directly with a lower layer (e.g., an application skipping the middleware to talk directly to the transport layer for performance reasons).

**Q: What is the key mechanism used in 'Union file systems' for containers?**
- **A:** It stacks multiple distinct file system directories (branches or layers) on top of each other so they appear as a single unified, coherent file system. It relies heavily on Copy-on-Write (CoW); the base layers remain read-only, and any modifications by the container are written to a thin, temporary top layer without destroying the original images.

---
### Distributed Systems Metrics and Their Purposes

When architecting and analyzing a distributed system, the following mathematical metrics define a system's limits, scale, and performance:

*   **Utilization ($U$):** Used in queuing theory. It measures the fraction of time a server or resource is busy versus idle ($U = \lambda / \mu$ where $\lambda$ is arrival rate and $\mu$ is service rate).
    *   *Purpose:* To predict response time explosions. As $U$ approaches 1, response times approach infinity.
*   **Response-to-Service Time Ratio ($S/R$):** The ratio between the actual time needed to process a request ($S$) and the total time the user spends waiting ($R$, which includes queue time + service time). Formula: $\frac{S}{R} = \frac{1}{1-U}$.
    *   *Purpose:* To mathematically prove how severely a highly utilized system degrades the user experience.
*   **Stretch (Relative Delay Penalty / RDP):** The ratio of the artificial delay caused by an overlay network's routing versus the optimal physical delay in the underlying network. Formula: $\frac{\text{Overlay Delay}}{\text{Physical Delay}}$.
    *   *Purpose:* To quantify the inefficiency introduced by logical routing in P2P networks or application-level multicasting.
*   **Link Stress:** The number of identical copies of a specific message that traverse the exact same physical network link simultaneously.
    *   *Purpose:* Used to optimize application-level multicast trees to prevent saturating specific physical routers.
*   **Mean Time Between Failures (MTBF) / Reliability:** The average continuous uptime of a component before it inevitably breaks.
    *   *Purpose:* To measure hardware quality and raw *reliability*. (Not to be confused with availability).
*   **Mean Time To Repair (MTTR):** The average time severely required to detect an incident, diagnose the problem, fix it, and restore the service.
    *   *Purpose:* Crucial for calculating Availability. A terrible MTBF can be masked by a near-instant MTTR.
*   **Availability:** The probability that a system is up and functioning correctly at any given random moment in time. Formula: $\frac{\text{MTBF}}{\text{MTBF} + \text{MTTR}}$. (Usually measured in "Nines", e.g., 99.999%).
    *   *Purpose:* The ultimate metric for distributed system SLAs. Focus on lowering MTTR rather than purely fighting to increase MTBF.



---
### MPI (Message Passing Interface) Deep Dive

MPI is the standard for high-performance parallel computing, focused on **transient communication** where messages are not stored long-term by the middleware.

#### 1. Point-to-Point Communication (Primitives)
MPI distinguishes between different "sending modes" based on how they interact with local buffers and receiver state:

*   **`MPI_SEND` (Standard Mode):** MPI decides whether to buffer the message or block until a receive is posted. It is non-prescriptive.
*   **`MPI_BSEND` (Buffered Mode):** The sender copies the message into a local buffer and returns immediately. If the buffer is full, it fails. Use this to avoid blocking the sender.
*   **`MPI_SSEND` (Synchronous Mode):** The "Safe" send. The sender blocks until the receiver has actually started receiving the message. It requires a 3-way handshake, providing strong synchronization.
*   **`MPI_RSEND` (Ready Mode):** An optimization. The sender assumes a matching receive is **already posted**. If not, it results in an error. Use only when you are certain of the order.
*   **`MPI_RECV`:** The standard blocking receive. It waits until a message matching the specified tag/source is available.

**Non-blocking Primitives (`MPI_ISEND`, `MPI_IRECV`):**
The `I` stands for **Immediate**. These return a request handle immediately without waiting for the operation to complete.
*   *Purpose:* To overlap computation with communication (Latency Hiding).
*   *Verification:* Use `MPI_WAIT` or `MPI_TEST` to check if the buffer is safe to reuse.

#### 2. Collective Communication (Operations on Groups)
These involve all processes in a communicator (group) and are typically blocking.

*   **`MPI_BARRIER`:** Forces all processes to wait until every process in the group has reached this call. (Synchronization point).
*   **`MPI_BCAST`:** One process (the root) sends the exact same data to every other process in the group.
*   **`MPI_SCATTER`:** The root process takes an array, splits it into equal chunks, and sends one unique chunk to each process in the group.
*   **`MPI_GATHER`:** The inverse of scatter. Each process sends a chunk of data, and the root process collects them into a single ordered array.
*   **`MPI_REDUCE`:** Collects data from all processes and applies a mathematical operation (like `SUM`, `MIN`, `MAX`, or `PROD`), storing the result on the root process.
*   **`MPI_ALLGATHER` / `MPI_ALLREDUCE`:** Similar to Gather/Reduce, but the final result is distributed to **all** processes, not just the root.

---

### Transitioning from a Monolith to Microservices

* **Point:** Shift from a monolithic "integrative view" to a containerized microservices "expansive view".
* **Reason:** In a monolith, all components share the exact same memory space, meaning a memory leak in one area (like a payment module) will consume all resources and crash the entire application. Microservices provide strict isolation so failures are contained. 
* **Point:** Utilize Docker containers instead of Virtual Machines (VMs) for hosting these independent services.
* **Reason:** Virtual machines require a hypervisor to run an entire, heavy guest operating system kernel for every single instance, which exhausts RAM. Docker containers share the host operating system's kernel, using features like namespaces and cgroups to provide isolation with a fraction of the computational overhead. 
### Enforcing Boundaries and Decoupling Communication

* **Point:** Enforce strict service boundaries using an Interface Definition Language (IDL).
* **Reason:** To combat the fallacy that "the network is reliable," an IDL acts as a strict contract between services. It compiles into local stubs, allowing a service written in one language (like Go) to seamlessly and safely communicate with a service written in another (like Java), handling all the complex data serialization under the hood.
* **Point:** Transition from Synchronous Remote Procedure Calls (RPC) to Asynchronous Publish-Subscribe (Pub/Sub) messaging for high-demand workflows.
* **Reason:** Synchronous RPC forces the calling service to block its thread and wait for a response; under massive load, this leads to thread pool exhaustion and cascading system failures. Pub/Sub utilizes a message broker to act as a buffer, queueing up events asynchronously so services can process them at their own pace without crashing. 
### Handling State and Data 

* **Point:** Move toward a stateless server architecture and shift state management to the client side.
* **Reason:** Storing session data for tens of thousands of idle users directly on the server rapidly consumes all available memory. By moving state to the client—such as using JSON Web Tokens (JWTs) attached to request headers—the server forgets the user exists between requests, freeing up massive amounts of RAM.
* **Point:** Offload basic processing and logic to the client's browser (the front end).
* **Reason:** Having the server validate simple things like zip code formats wastes valuable CPU cycles and network bandwidth. Using a heavy JavaScript framework on the client side handles this validation locally, drastically reducing unnecessary server trips. 
* **Point:** Shatter the massive, centralized relational database into isolated, resource-based databases owned by individual microservices.
* **Reason:** A single massive database becomes a deadly bottleneck; if thousands of users try to buy an item at once, the database locks up to prevent race conditions and grinds to a halt. Splitting the database ensures that a high load on the inventory service doesn't lock up the user profile service.

### Achieving the "Big Three" Design Goals

* **Point:** Implement Concurrency Transparency using Two-Phase Locking (2PL).
* **Reason:** Transparency means hiding the distributed nature of the system from the user. When multiple users try to buy the exact same limited item at the exact same millisecond, 2PL ensures mathematical order, preventing data corruption and race conditions without the user ever realizing they were placed in a queue.
* **Point:** Achieve Size Scalability via horizontal scaling behind a load balancer. * **Reason:** Vertical scaling (buying a bigger, more expensive server) has a hard physical ceiling. Horizontal scaling allows you to clone endless identical containers to handle demand. Queuing theory dictates this necessity; as utilization ($U$) approaches 100%, response time ($R$) shoots to infinity, governed by the formulas $U=\lambda/\mu$ and $R=S/(1-U)$.
* **Point:** Tackle Geographical Scalability using Content Delivery Networks (CDNs).
* **Reason:** Light physically takes time to travel, creating unavoidable network latency over long distances. CDNs solve this by caching static assets (like images and UI files) on edge servers geographically close to the user, masking the communication latency to the main database. * **Point:** Separate Availability from Reliability by masking failures with redundancy.
* **Reason:** A system doesn't need to be perfectly reliable (never crashing) to be highly available. By running redundant replicas behind a load balancer that utilizes heartbeat health checks, a crashed node is instantly bypassed, keeping the system available to the user despite hardware failures.

### Naming, Location, and Predictive Architectures

* **Point:** Decouple logical service names from physical IP addresses.
* **Reason:** Because containers scale up and down dynamically, their physical IP addresses constantly change. Hardcoding IPs guarantees broken connections. Using Structured naming (like DNS) or Flat naming (like Distributed Hash Tables) allows a service to simply ask for "inventory.internal" and be routed to the correct, currently active IP address.
* **Point:** Evolve from a reactive architecture to a predictive, machine learning-driven architecture.
* **Reason:** Relying purely on redundancy still results in a brief interruption when a node dies before a new one spins up. Feeding server telemetry (like CPU voltage drops) into an ML model allows the system to predict a crash *before* it happens, preemptively migrating traffic and achieving near-perfect fault tolerance.

---

### The Eight Fallacies of Distributed Computing

When architecting distributed systems, assuming any of the following to be true will lead to flawed designs and eventual system failure:

1. **The network is reliable:** Hardware fails, cables get cut, switches crash, and packets drop. Always design with timeouts, retries, and fallback mechanisms.
2. **Latency is zero:** Network calls take orders of magnitude longer than local in-memory calls. Design APIs to be coarse-grained (transferring more data per call) rather than fine-grained to minimize the number of round trips.
3. **Bandwidth is infinite:** Network capacity has limits. Transferring massive payloads or unbounded datasets will saturate the network. Use pagination, compression, and transmit only necessary data.
4. **The network is secure:** Packets pass through untrusted intermediaries. Always authenticate endpoints and encrypt data in transit (e.g., using mTLS/HTTPS) regardless of whether the network is "internal."
5. **Topology doesn't change:** IP addresses change, servers are added or removed dynamically (autoscaling), and routes adjust. Never hardcode IP addresses; use dynamic discovery services (like DNS or Consul).
6. **There is one administrator:** A distributed system often spans multiple teams, organizations, or even companies. Deployments, updates, and configurations will not be synchronized globally. Strong decoupling and backward-compatible APIs are required.
7. **Transport cost is zero:** Moving data costs money and compute overhead (serialization/deserialization). Be mindful of chatty protocols and the physical cost of data egress, particularly across availability zones or cloud providers.
8. **The network is homogeneous:** Real-world networks consist of diverse hardware, operating systems, varying link speeds, and different protocols. Standardize communication using vendor-neutral formats (like JSON or Protocol Buffers) and IDLs.