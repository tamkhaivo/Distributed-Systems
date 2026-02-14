# Module 3: Processes & Virtualization (Wait, really?)

> *Wait, didn't we just do this? No, last module we talked about structural *Architecture*. Now we drill down into the execution units: Processes, Threads, VMs, and Containers. From the 10,000 ft view to the instruction pointer.*

## 3.1 Introduction to Processes

> *The fundamental unit of execution. If the Architecture is the blueprint, the Process is the construction crew.*

*   **Definition**: A program in execution.
    *   **State**: The Program Counter, Registers, Stack, Heap.
    *   **Isolation**: Every process thinks it owns the CPU and all of RAM (Virtual Memory).
    *   **Communication**: Processes don't share memory by default. They talk via IPC (Inter-Process Communication): Pipes, Sockets, Shared Memory segments.

*   **The OS Reality**:
    *   **Context Switching**: The hidden tax. Saving registers, flushing TLBs (Translation Lookaside Buffers), loading new state.
    *   *Principal's Take*: "Context switching is expensive. If you spawn a process for every HTTP request (looking at you, CGI/Perl), you spend 90% of your CPU just saving and restoring state, and 10% actually serving the request."

## 3.2 Threads: The Lightweight Alternative

> *Concurrency is hard. Parallelism is harder. Threads are the tool we use to hurt ourselves with both.*

*   **Definition**: A "Lightweight Process".
    *   **Shared State**: Threads share the same address space (Heap, Global variables).
    *   **Private State**: Each thread has its own Stack and Registers.
    *   **The Win**: Switching threads is cheaper than switching processes (no TLB flush).
    *   **The Risk**: Data Races. If two threads write to `x` at the same time, `x` becomes garbage. Welcome to Mutex/Semaphore hell.

*   **User Space vs. Kernel Space Threads**:
    *   **User-Level Threads (Green Threads)**:
        *   Managed by a library (e.g., Java Green Threads of old, Go Goroutines).
        *   *Pros*: Fast creation/switching. OS doesn't know they exist.
        *   *Cons*: If one thread blocks on I/O, the *entire process* blocks (OS sees 1 process).
    *   **Kernel-Level Threads**:
        *   Managed by the OS (pthreads, Windows Threads).
        *   *Pros*: If one thread blocks, others run. SMP (Symmetric Multi-Processing) support.
        *   *Cons*: Every creation/switch requires a System Call (Trap to Kernel Mode). Expensive.
    *   **Hybrid (M:N Model)**: M user threads on N kernel threads. (Go Scheduler). Best of both worlds, complex to implement.

*   **Multithreaded Clients**:
    *   **Why?** To hide latency.
    *   *Browser Example*: Web Browser uses multiple threads.
        *   Thread A: Fetches HTML.
        *   Thread B: Renders specific image.
        *   Thread C: Validates DNS.
        *   *Result*: The UI doesn't freeze while waiting for the network. The definition of "Responsiveness".

*   **Multithreaded Servers**:
    *   **Dispatcher/Worker Model**:
        1.  **Dispatcher Thread**: Accept `socket()`. Read request.
        2.  **Worker Thread**: Hand off request to worker.
        3.  *Benefit*: High throughput. The Dispatcher never blocks on disk I/O.
    *   **The "C10K" Problem**:
        *   Can we handle 10,000 concurrent connections?
        *   *Thread-per-request*: No. 10k threads * 2MB Stack = 20GB RAM. Plus scheduler thrashing.
        *   *Event-Driven (Single Threaded)*: Yes (Node.js, Nginx). One thread, non-blocking I/O.
        *   *Principal's Take*: "If your server is I/O bound (waiting on DB), use Async/Events. If your server is CPU bound (calculating Pi), use Threads/Processes. Know your bottleneck."

## 3.3 Advanced Concurrency & Parallelism

> *Writing a loop is easy. Writing a loop that runs on 64 cores without crashing is art.*

### 3.3.1 The Blocking Problem
*   **Blocking I/O**:
    *   `read()` waits for the disk. The thread does nothing (Sleep state).
    *   *Scale limit*: 1 thread per request = 10k threads for 10k users.
    *   *Issue*: 10k stacks * 2MB = 20GB RAM. Context switching 10k threads kills the CPU.
*   **Non-Blocking I/O (NIO)**:
    *   `read()` returns immediately with `EWOULDBLOCK` if no data is ready.
    *   **Multiplexing (select/poll/epoll)**:
        *   Ask the OS: "Tell me which of these 1000 sockets has data ready."
        *   One thread handles thousands of connections.
    *   **The Reactor Pattern**:
        *   Event Loop waits for events.
        *   When event triggers, callback runs.
        *   *Example*: Node.js, Netty, Nginx.
    *   *Principal's Take*: "NIO is mandatory for high-concurrency network servers (Gateways, Proxies). But for CPU-heavy tasks, it's useless because a single long calculation blocks the entire event loop."

### 3.3.2 Parallelism Challenges
*   **1. Race Conditions (The "Lost Update")**:
    *   Thread A reads X (10). Thread B reads X (10). Both add 1. Both write 11.
    *   *Result*: X is 11, should be 12. Correctness is gone.
*   **2. Deadlocks (The "Deadly Embrace")**:
    *   Thread A holds Lock 1, wants Lock 2.
    *   Thread B holds Lock 2, wants Lock 1.
    *   *Result*: Both wait forever.
    *   **The 4 Coffman Conditions**: Mutual Exclusion, Hold and Wait, No Preemption, Circular Wait. Break one to fix it.
*   **3. The Scalability Cap (Amdahl's Law)**:
    *   Speedup is limited by the serial part of the program.
    *   If 5% of code must be serial (locks), maximum speedup is 20x, even with infinite CPUs.
    *   *Principal's Rule*: "Don't just add hardware. Remove locks."

### 3.3.3 Solutions & Patterns
*   **Optimistic vs. Pessimistic Locking**:
    *   **Pessimistic**: "Acquire mutex, do work, release." (Safe but slow).
    *   **Optimistic**: "Read version, do work, try to save. If version changed, retry." (Fast for low contention).
*   **Lock-Free Programming (CAS)**:
    *   **Compare-And-Swap (CAS)**: Atomic CPU instruction. `CAS(addr, old, new)`.
    *   *Usage*: Java `AtomicInteger`, ConcurrentHashMaps.
    *   *Benefit*: No context switch, no sleep.
    *   *Risk*: ABA Problem (Value changed A->B->A, thread thinks nothing changed).
*   **Alternative Models**:
    *   **CSP (Communicating Sequential Processes)**: Go Channels. "Don't communicate by sharing memory; share memory by communicating."
    *   **Actor Model**: Erlang/Akka. Actors have private state and a mailbox. Message passing only. (Naturally deadlock-free... mostly).
    *   **Software Transactional Memory (STM)**: Haskell/Clojure. Treat memory like a database transaction. (Rollback on conflict).

### 3.3.4 The FSM Scalability Secret (Finite State Machines)
> *Why have 10,000 threads when you can have 1 thread and 10,000 state objects?*

*   **The Problem with Threads**:
    *   In a traditional model, "State" is implicit in the thread's stack pointer. (e.g., "I am at line 50, waiting for DB").
    *   This stack is heavy (1-2MB).
*   **The FSM Solution**:
    *   **Explicit State**: Turn the instruction pointer into a variable. `State = WAITING_FOR_DB`.
    *   **Event Driven**: When DB returns, transition `State -> PROCESSING`.
    *   **Memory Footprint**: An FSM is just a small struct/object (maybe 100 bytes).
    *   **Parallelism**:
        *   You can have **millions** of FSMs in memory.
        *   You only need a small pool of threads (equal to CPU cores) to process events.
        *   *Analogy*: One waiter (Thread) serving 50 tables (FSMs). The waiter doesn't sit at the table while the customer decides; they handle other tables.
*   **Use Cases**:
    *   **Game Servers**: Every player connection is an FSM.
    *   **Telco Switches**: Handling millions of calls. Erlang was built for this.
    *   **Cloud Gateways**: AWS API Gateway, Nginx.

### 3.3.5 Low-Level Optimizations
*   **Thread Pools**:
    *   Reuse threads. Avoid creation overhead.
    *   *Sizing*:
        *   CPU Bound: threads = CPU cores + 1.
        *   I/O Bound: threads = CPU cores * (1 + Wait/Compute time).
*   **False Sharing**:
    *   Two threads write to two different variables (`x`, `y`) that happen to sit on the same Cache Line (64 bytes).
    *   Cores fight over ownership of the cache line. Performance tanks.
    *   *Fix*: Padding (Add 64 bytes of junk between `x` and `y`).

## 3.4 Virtualization (The Big Lie)

> *Virtualization is the art of lying to software, telling it 'You own this hardware', when in fact it is renting a small slice of it.*

*   **Role of Virtualization**:
    *   Legacy support (Run Windows 95 on Linux).
    *   Isolation (Sandboxing malicious code).
    *   **Resource Consolidation**: Run 10 Servers (10% utilization each) on 1 Physical Box (100% utilization). This is the *economic engine* of the Cloud.

*   **Architectures**:
    *   **Types of Interfaces**:
        1.  **Hardware/ISA**: The instruction set (x86, ARM).
        2.  **System Call**: The OS interface (Linux Kernel API).
        3.  **Library/API**: The application level (libc).
    *   **Hypervisor (Virtual Machine Monitor - VMM)**:
        *   **Type 1 (Bare Metal)**: Runs directly on hardware (VMware ESXi, Xen). High performance. The OS is a guest.
        *   **Type 2 (Hosted)**: Runs on top of a Host OS (VirtualBox, VMWare Fusion). Good for desktops, bad for servers (Double layer of overhead).

*   **Para-Virtualization vs. Full Virtualization**:
    *   **Full Virtualization**: The Guest OS is unmodified. It doesn't know it's virtualized. The VMM traps every privileged instruction. *Slow* (Trap-and-Emulate).
    *   **Para-Virtualization**: The Guest OS is modified to know it's virtualized. Instead of trying to access hardware directly, it calls the VMM API (Hypercalls). *Fast*.

## 3.5 Containerization (The New Standard)

> *VMs are heavy. What if we just isolated the processes but shared the kernel?*

*   **The Concept**: OS-Level Virtualization.
    *   One Kernel (Linux).
    *   Multiple Userspaces (Ubuntu, Alpine, CentOS).
*   **The Mechanics (Linux Primitives)**:
    1.  **Namespaces**: What you can *see*. (PID 1 in the container is PID 12345 on the host).
    2.  **Cgroups (Control Groups)**: What you can *use*. (Limit CPU to 0.5 cores, RAM to 512MB).
    3.  **Chroot/UnionFS**: What your *filesystem* looks like. (Overlay filesystems make images efficient).

*   **VM vs. Container**:
    *   **VM**: App + Libs + Guest OS + Hypervisor + Hardware. (Heavy, 1GB+, Seconds to boot).
    *   **Container**: App + Libs + Shared Kernel + Hardware. (Light, 10MB+, Milliseconds to boot).
    *   *Security Trade-off*:
        *   VM: Strong isolation. Attack must break out of Guest OS *and* Hypervisor.
        *   Container: Weak isolation. Attack just needs a Kernel Exploit (Shared Kernel!).
        *   *Principal's Take*: "Containers are for deployment velocity. VMs are for security boundaries. If you run untrusted code (Multi-tenant), wrap the Container in a MicroVM (Firecracker)."

## 3.6 Code Migration

> *Moving the computation to the data, or the data to the computation.*

*   **Reasons to Migrate**:
    1.  **Performance**: Move the query to the database (Stored Procedures), not the database to the query.
    2.  **Flexibility**: Load balancing (Move a running process from a busy node to an idle node).
*   **Models**:
    *   **Weak Mobility**: Move code only. Restart execution. (Java Applets).
    *   **Strong Mobility**: Move code + *Execution State* (Stack/PC). Pause, serialize, move, resume.
    *   *Difficulty*: Strong mobility is incredibly hard. "What if the process has an open file handle? Or a TCP socket?" You can't easily migrate open resources.

## 3.7 Case Study: Global Distributed Systems

*   **Planetary Scale**:
    *   When processes span the globe, speed of light becomes the dominant constraint.
    *   **CDN (Content Delivery Network)**:
        *   Push *static* content (Images, JS) to the Edge (Processes running near the user).
        *   *Dynamic* content stays central ... for now.
    *   **Edge Computing**:
        *   Running logic (AWS Lambda @ Edge) on the CDN nodes.
        *   *Goal*: Respond to the user in < 10ms.

*   **Principal's Closing thought**:
    *   "The line between Process, VM, and Container is blurring. Is a WASM (WebAssembly) module a process? A container? It's an isolation boundary. Focus on the boundary: What do you share? What do you hide? What is the cost of crossing the line?"
