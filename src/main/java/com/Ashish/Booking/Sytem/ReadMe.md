
###################################################
# Chapter 1 - Solving Seat Booking Concurrency

## Problem Statement

Initially, the booking flow was designed as:

```
Validate Booking
        ↓
Create Booking
        ↓
Create BookedSeat
        ↓
Update ShowSeat
```

This implementation worked correctly when only a single user was making bookings. However, it failed when multiple users attempted to book the same seat simultaneously.

---

## The Concurrency Problem

Consider the following scenario:

```
Ashish                     Rahul

Reads A1                   Reads A1
Status = AVAILABLE         Status = AVAILABLE

        ↓                         ↓

Books A1                  Books A1
```

Both requests execute concurrently.

Since both users read the seat before either updates its status, both transactions believe the seat is available.

As a result, two successful bookings are created for the same seat.

This problem is known as a **Race Condition**.

---

## Why Does This Happen?

The booking logic performs two independent operations:

```
Read Seat Status
        ↓
Check Availability
        ↓
Update Seat Status
```

These operations are **not atomic**.

Another request can execute between the read and update operations, resulting in inconsistent data.

---

# Possible Solutions

## Solution 1 - Java synchronized

```
public synchronized BookingResponseDto createBooking(...)
```

### Advantages

* Very easy to implement.
* Prevents concurrent execution within a single JVM.

### Disadvantages

* Blocks every booking request, even for unrelated seats.
* Does not work when the application is deployed on multiple servers.
* Poor scalability.

### Conclusion

Not suitable for production systems.

---

## Solution 2 - Database Pessimistic Locking

The database locks the seat row before modification.

```
SELECT ... FOR UPDATE
```

### Advantages

* Prevents concurrent updates.
* Guarantees data consistency.
* Works across multiple application servers.

### Disadvantages

* Database transactions remain open while the user completes payment.
* Long-running transactions consume database resources.
* Heavy locking reduces throughput during peak traffic.

### Conclusion

Correct solution for many applications, but not ideal for long-running business processes such as ticket payment.

---

## Solution 3 - Optimistic Locking

Each row maintains a version number.

```
Version = 5

↓

Update WHERE version = 5

↓

Version becomes 6
```

If another transaction already updated the row, the update fails with an OptimisticLockException.

### Advantages

* No database locks.
* High throughput.
* Prevents double booking.

### Disadvantages

* Conflict is detected only after payment or booking confirmation.
* Users may spend time completing payment only to discover the seat has already been booked.

### Conclusion

Maintains database consistency but provides poor user experience for ticket booking systems.

---

## Solution 4 - Redis Based Seat Locking

Instead of locking the database row, a temporary lock is stored inside Redis.

```
AVAILABLE
      ↓
LOCKED (Redis)
      ↓
Payment
      ↓
BOOKED (Database)
```

### Advantages

* Immediate seat reservation.
* Excellent user experience.
* Database transactions remain very short.
* Temporary locks automatically expire using TTL.
* Significantly reduces load on the primary database.

### Disadvantages

* Additional infrastructure.
* Slightly more complex architecture.

### Conclusion

Preferred approach for large-scale ticket booking systems.

---

# Why Redis Instead of Database Locking?

Database locking keeps a database transaction open while the user completes payment.

```
Database Transaction
        ↓
Payment Gateway
        ↓
OTP Verification
        ↓
Commit
```

A payment may take several minutes.

Long-running transactions reduce database performance.

Redis changes the flow:

```
Acquire Redis Lock
        ↓
Payment
        ↓
Begin Database Transaction
        ↓
Persist Booking
        ↓
Commit
        ↓
Release Redis Lock
```

The database transaction now lasts only a few milliseconds.

Redis handles temporary reservation, while the database remains responsible for permanent storage.

---

# Key Learnings

* Race conditions occur because multiple threads execute concurrently.
* Read and update operations must be treated as a single logical unit.
* Data consistency and user experience are different problems.
* Optimistic locking solves consistency but not user experience.
* Database pessimistic locking solves consistency but creates long-running transactions.
* Redis is introduced not merely because it is fast, but because it supports temporary, expiring locks while keeping database transactions short.

---

# Final Architecture Decision

```
User clicks seat
        ↓
Redis creates temporary lock
        ↓
User completes payment
        ↓
Short database transaction
        ↓
Booking persisted
        ↓
ShowSeat updated
        ↓
Redis lock removed
```

This architecture provides:

* Excellent user experience.
* Strong consistency.
* Better database performance.
* High scalability.

# Chapter 2 - Understanding Redis

## Objective

Before integrating Redis into the Ticket Booking System, it is important to understand **why Redis exists**, **what problem it solves**, and **why it is preferred over traditional databases for temporary data such as seat reservations**.

The objective of this chapter is not to learn Redis commands, but to understand the reasoning behind introducing Redis into the system architecture.

---

# Why Was Redis Created?

Traditional relational databases such as MySQL are excellent at storing permanent data.

However, as applications scale, they begin receiving millions of repeated read and write operations.

Every request requires the database to:

```text
Application
      ↓
Database
      ↓
Disk
      ↓
Memory
      ↓
Response
```

Even when the data changes very infrequently.

This introduces unnecessary latency.

Redis was created to solve this problem by storing frequently accessed or temporary data entirely in **main memory (RAM)**.

---

# What is Redis?

Redis is an **in-memory Key-Value data store**.

Unlike relational databases, Redis does not organize data into tables and rows.

Instead, every piece of information is stored as:

```text
Key
    ↓
Value
```

Example:

```text
seat:show101:A1

↓

LOCKED
```

or

```text
user:101

↓

Ashish
```

This simple storage model allows Redis to retrieve data extremely quickly.

---

# Why is Redis Fast?

The primary reason Redis is fast is because it stores data in **RAM** instead of reading it from disk.

```text
MySQL

Persistent Storage

↓

Disk Based

↓

Milliseconds
```

```text
Redis

Temporary Storage

↓

RAM Based

↓

Microseconds
```

Because Redis keeps data directly in memory, it eliminates most of the latency associated with disk access.

---

# Redis vs Java HashMap

Conceptually, Redis behaves similarly to a Java HashMap.

```java
HashMap<String, Object>
```

```java
map.put("seat:A1", "LOCKED");

map.get("seat:A1");
```

Both provide nearly constant-time lookup.

The difference is that a Java HashMap exists only inside one JVM, whereas Redis runs as an independent server.

```text
Application 1
        \
         \
          Redis Server
         /
        /
Application 2
```

Multiple application instances can communicate with the same Redis server, making it suitable for distributed systems.

---

# Redis is More Than a Cache

Redis is commonly used for:

* Caching
* Session Management
* Distributed Locking
* Rate Limiting
* Leaderboards
* Pub/Sub Messaging
* Queues
* Real-Time Analytics

In this project, Redis will be used to implement **distributed seat locking**.

---

# Time To Live (TTL)

One of Redis' most powerful features is the ability to automatically expire data.

Example:

```text
seat:show101:A1

↓

LOCKED

↓

Expires after 5 minutes

↓

Automatically removed
```

Unlike a relational database, Redis can automatically delete temporary data without requiring scheduled jobs or manual cleanup.

This makes it ideal for temporary seat reservations.

---

# Atomic Operations

One major challenge in concurrent systems is preventing race conditions.

A normal implementation performs:

```text
Check

↓

Create
```

as two separate operations.

This creates a race window where another request can execute in between.

Redis provides atomic commands that combine these operations into a single indivisible action.

Example:

```redis
SET seat:show101:A1 LOCKED NX EX 300
```

Meaning:

* Create the key only if it does not already exist.
* Automatically expire the key after 300 seconds.

The command executes atomically, eliminating race conditions during seat reservation.

---

# Why "SET NX"?

Suppose two users attempt to reserve the same seat simultaneously.

```text
Ashish

↓

SET seat:A1 LOCKED NX

↓

Success
```

```text
Rahul

↓

SET seat:A1 LOCKED NX

↓

Fails
```

Because Redis processes commands atomically, only one request succeeds.

This property makes Redis suitable for implementing distributed locks.

---

# Redis and Database Responsibilities

A key architectural decision is separating temporary state from permanent data.

```text
MySQL

Permanent Business Data

Bookings

Payments

Shows

Users
```

```text
Redis

Temporary State

Seat Locks

Cache

Session Data
```

Redis does **not replace** MySQL.

Instead, Redis complements the database by handling temporary, high-speed operations while MySQL remains the source of truth.

---

# Key Learnings

* Redis is an in-memory Key-Value data store.
* Storing data in RAM makes Redis significantly faster than disk-based databases.
* Redis is conceptually similar to a distributed HashMap accessible by multiple application servers.
* Redis supports automatic expiration (TTL), making it ideal for temporary reservations.
* Atomic commands eliminate race conditions without requiring complex synchronization.
* `SET NX EX` is the foundation of distributed locking in Redis.
* Redis should be viewed as temporary storage, while MySQL remains responsible for permanent business data.

---

# Final Mental Model

```text
                    MySQL

Permanent Data

ACID Transactions

Source of Truth

Disk Based

---------------------------------------

                    Redis

Temporary Data

Distributed Locks

Caching

TTL

RAM Based
```

Redis and MySQL are complementary technologies.

The database stores **what must never be lost**, while Redis stores **what only needs to exist temporarily**.

---

# Conclusion

Redis was introduced into the Ticket Booking System not simply because it is fast, but because it provides a highly efficient mechanism for managing temporary seat reservations.

By storing locks in Redis instead of the primary database:

* Database transactions remain short.
* Temporary seat reservations expire automatically.
* Race conditions are eliminated using atomic operations.
* User experience is significantly improved.

This understanding forms the foundation for implementing distributed seat locking in the next phase of the project.

############################################################

# Chapter 3 - Preventing Double Booking using Redis Distributed Locks

## Problem Statement

Although the booking flow was functionally correct, it still suffered from one major issue.

When two users attempted to book the same seat at nearly the same time, both requests could execute concurrently.

```
User A                          User B

Checks A1 Available

                                Checks A1 Available

Creates Booking

                                Creates Booking
```

Since both requests read the database before either transaction committed, both users believed the seat was available.

This resulted in the classic **Double Booking Problem**.

---

## Why not use synchronized?

The first solution that comes to mind is Java's synchronized keyword.

```java
synchronized(bookSeat()){
    ...
}
```

This solution was rejected because:

- It only works inside a single JVM.
- It blocks every booking request, even for different seats.
- It completely fails once the application is deployed on multiple servers.

It is therefore not suitable for a distributed system.

---

## Why not use Database Locks?

The next possible solution was pessimistic database locking.

Although it prevents concurrent updates, it introduces another problem.

A booking request involves multiple business operations and may later include payment processing.

Keeping database rows locked for several seconds would:

- Reduce database throughput.
- Increase waiting time for other transactions.
- Make the database responsible for application coordination.

The database should store data, not manage long-running distributed locks.

---

## Introducing Redis

Instead of locking the database, a temporary lock is created inside Redis before the booking process begins.

Redis internally performs:

```redis
SET key value NX EX 300
```

where

- **NX** → Create the key only if it does not already exist.
- **EX** → Automatically expire the lock after five minutes.

This allows only one request to acquire the lock.

Every other request immediately fails without touching the database.

---

## Lock Ownership

Initially, the lock value stored inside Redis was simply

```
Seat:A1

↓

LOCKED
```

This created another issue.

Since no ownership information existed, any request could delete the lock.

To solve this, every successful lock generates a unique UUID.

```java
UUID.randomUUID().toString();
```

Redis now stores

```
Seat:A1

↓

550e8400-e29b-41d4-a716-446655440000
```

The UUID represents the ownership of that particular lock.

Only the owner of the lock is allowed to release it.

---

## Safe Unlocking

A naive unlock implementation would perform

```
GET

↓

COMPARE

↓

DELETE
```

These are three separate operations.

Between GET and DELETE another request could acquire the same lock, causing the new owner's lock to be deleted accidentally.

This race condition was solved using a Redis Lua Script.

```lua
local current = redis.call("GET", KEYS[1])

if current == ARGV[1] then
    redis.call("DEL", KEYS[1])
    return 1
end

return 0
```

Redis executes the entire script atomically, ensuring that no other client can modify the lock during execution.

---

## Integrating Redis into Booking Flow

Redis locking was integrated before any database operation.

The booking flow became

```
Validate Booking
        ↓
Acquire Redis Locks
        ↓
Create Booking
        ↓
Create BookedSeat
        ↓
Update ShowSeat
        ↓
Release Redis Locks
```

Every requested seat is locked before proceeding.

If acquiring even one lock fails,

```
Acquire Lock A1 ✓

Acquire Lock A2 ✓

Acquire Lock A3 ✗

↓

Release A1

↓

Release A2

↓

Booking Failed
```

This guarantees that either **all seats are locked or none are locked**.

---

## Why finally?

Redis is completely independent from Spring's database transaction.

`@Transactional` rolls back only database changes.

It does **not** release Redis locks.

For this reason every booking follows the pattern

```java
try{

    acquireLocks();

    bookingLogic();

}
finally{

    releaseLocks();

}
```

Even if the database transaction fails, Redis locks are still released.

---

## Why TTL?

Locks are normally released immediately after booking.

However, if the application crashes before reaching the unlock step, the lock would remain forever.

TTL acts as a safety mechanism.

If the application dies unexpectedly, Redis automatically removes the lock after five minutes, preventing seats from remaining locked permanently.

---

## Final Architecture

```
Validate Booking
        ↓
Acquire Redis Distributed Locks
        ↓
Spring Database Transaction
        ↓
Create Booking
        ↓
Create BookedSeat
        ↓
Update ShowSeat
        ↓
Commit / Rollback Database
        ↓
Release Redis Locks
```

---

## What I Learned

- Identified the double booking problem in concurrent booking systems.
- Compared synchronized blocks, pessimistic database locking and Redis distributed locking.
- Implemented distributed locks using `SET NX EX`.
- Introduced UUID based lock ownership.
- Implemented atomic unlocking using Redis Lua Scripts.
- Wrapped the booking flow with lock acquisition and release.
- Used `try-finally` to guarantee lock cleanup.
- Used TTL as a safety mechanism in case of application failure.
- Separated Redis coordination from database transactions.