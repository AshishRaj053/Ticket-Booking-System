
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

# Chapter 4: Event-Driven Architecture with Apache Kafka

## Overview

After implementing distributed locking with Redis, the booking system successfully prevents multiple users from reserving the same seat simultaneously. However, a real-world movie booking platform performs several independent tasks after a booking is created, such as processing payments, sending notifications, updating analytics, generating recommendations, or rewarding loyalty points. Executing all of these operations synchronously inside the Booking Service tightly couples the application and increases the response time of the booking API.

To solve this problem, the project adopts an **Event-Driven Architecture** using **Apache Kafka**. Instead of directly invoking downstream services, the Booking Service publishes business events to Kafka. Interested services subscribe to these events and execute their responsibilities independently. This makes the system asynchronous, loosely coupled, scalable, and much closer to a production-grade distributed system.

---

## Why Kafka?

Initially, the Booking Service directly communicated with the Payment Service.

```
Booking Service
       │
       ▼
Payment Service
```

Although simple, this approach has several drawbacks:

- Booking Service becomes dependent on Payment Service.
- Every new downstream feature requires modifications inside Booking Service.
- User response time increases because every operation executes synchronously.
- Tight coupling makes future microservice migration difficult.

To remove these dependencies, Kafka is introduced as the communication backbone.

```
Booking Service
       │
       ▼
BookingCreatedEvent
       │
   Kafka Topic
       │
       ▼
Payment Service
```

The Booking Service now only publishes an event and has no knowledge of which services consume it.

---

## Architecture

The booking workflow is divided into independent event-driven components.

```
                     Booking Service
                            │
                            ▼
                 BookingCreatedEvent
                            │
                     booking-created Topic
                            │
               ┌────────────┴────────────┐
               ▼                         ▼
       Payment Consumer          (Future Consumers)
               │
               ▼
        Payment Service
               │
               ▼
      PaymentCompletedEvent
               │
                payment-completed Topic
               │
        ┌──────┴──────────────┐
        ▼                     ▼
 Booking Consumer     Notification Consumer
        │                     │
        ▼                     ▼
 Booking Service      Notification Service
```

Each service performs one responsibility and communicates only through business events.

---

## Booking Workflow

The complete booking flow is divided into synchronous and asynchronous stages.

### Stage 1 : Booking Creation

```
User Books Seats
        │
        ▼
Booking Service
        │
        ▼
Acquire Redis Locks
        │
        ▼
Validate Booking
        │
        ▼
Save Booking (Status = PENDING)
        │
        ▼
Save Booked Seats
        │
        ▼
Publish BookingCreatedEvent
        │
        ▼
Release Redis Locks
        │
        ▼
Return Response
```

The booking request finishes immediately after publishing the event.

---

### Stage 2 : Payment Processing

```
BookingCreatedEvent
        │
        ▼
Payment Consumer
        │
        ▼
Payment Service
        │
        ▼
Process Payment
        │
        ▼
Publish PaymentCompletedEvent
```

Payment processing happens asynchronously without blocking the booking request.

---

### Stage 3 : Booking Confirmation & Notification

```
PaymentCompletedEvent
        │
        ├──────────────► Booking Consumer
        │                      │
        │                      ▼
        │             Confirm Booking
        │
        ▼
Notification Consumer
        │
        ▼
Send Notification
```

Both services independently react to the same business event.

---

## Business Events

Two business events are introduced.

### BookingCreatedEvent

Represents the creation of a booking request.

Current booking status:

```
PENDING
```

Fields:

- bookingId
- showId
- userId

This event **does not** indicate successful payment.

---

### PaymentCompletedEvent

Represents successful payment.

Fields:

- bookingId
- showId
- userId

Only after this event:

- Booking is confirmed.
- Notification is sent.

Although both events currently contain identical fields, they represent different business facts and are intentionally kept as separate classes so they can evolve independently in the future.

---

## Kafka Topics

Two dedicated topics are used.

### booking-created

Purpose:

- Notify Payment Service that a booking request has been created.

---

### payment-completed

Purpose:

- Notify downstream services that payment has been completed successfully.

Each topic represents exactly one business event.

---

## Consumer Groups

Different services subscribe to the same event using different consumer groups.

### payment-group

Consumes:

- BookingCreatedEvent

Responsibility:

- Process payment.

---

### booking-group

Consumes:

- PaymentCompletedEvent

Responsibility:

- Confirm booking.

---

### notification-group

Consumes:

- PaymentCompletedEvent

Responsibility:

- Send booking confirmation notification.

Using different consumer groups ensures every service receives its own copy of the event.

---

## Producer Design

Separate producer classes are created for each business event.

### BookingEventProducer

Responsible only for publishing:

```
BookingCreatedEvent
```

---

### PaymentEventProducer

Responsible only for publishing:

```
PaymentCompletedEvent
```

Each producer has a single responsibility and hides Kafka implementation details from the business services.

---

## Consumer Design

Consumers contain no business logic.

They simply receive events and delegate processing to the corresponding service.

```
PaymentConsumer
        │
        ▼
PaymentService
```

```
BookingConsumer
        │
        ▼
BookingService.confirmBooking()
```

```
NotificationConsumer
        │
        ▼
NotificationService
```

This keeps the architecture clean and follows the Single Responsibility Principle.

---

## Why Notification Listens to PaymentCompletedEvent

Initially, Notification was designed to consume `BookingCreatedEvent`.

However, booking creation does not guarantee successful payment.

```
Booking Created
        │
Payment Failed
```

Sending a confirmation notification in this situation would be incorrect.

Instead, Notification listens to:

```
PaymentCompletedEvent
```

Now the notification is sent only after payment succeeds.

```
Payment Successful
        │
        ▼
PaymentCompletedEvent
        │
        ▼
Notification Sent
```

This better reflects the actual business workflow.

---

## Booking Confirmation Through Kafka

Initially, Payment Service directly invoked:

```
bookingService.confirmBooking()
```

This tightly coupled the Payment module with the Booking module.

The implementation was improved by introducing another business event.

```
Payment Service
        │
        ▼
PaymentCompletedEvent
        │
        ▼
Booking Consumer
        │
        ▼
Booking Service
```

Now Payment Service has no knowledge of Booking Service.

This makes the architecture much closer to a real microservice design.

---

## Event Publishing Strategy

Business events are published **only after** the corresponding business operation completes successfully.

Example:

```
Payment Started
        │
        ▼
Payment Successful
        │
        ▼
Publish PaymentCompletedEvent
```

Publishing events only after successful execution ensures downstream services react only to completed business actions.

---

## Design Decisions

The following architectural decisions were intentionally taken during implementation:

- Spring Boot remains the source of truth for all business data.
- Kafka is used only for communication between independent services.
- Every business event has its own dedicated Kafka topic.
- Separate producer classes are created for different event types.
- Consumer classes contain no business logic.
- Business logic always remains inside service classes.
- Booking status changes from **PENDING** to **CONFIRMED** only after receiving **PaymentCompletedEvent**.
- Notification is triggered only after successful payment.
- Services communicate through business events instead of direct method calls.
- Event classes represent business facts rather than database entities.

---

## Advantages of Event-Driven Architecture

Implementing Kafka provides several architectural benefits:

- Loose coupling between modules.
- Asynchronous processing.
- Faster API response time.
- Independent scalability.
- Easier feature addition.
- Better separation of responsibilities.
- Microservice-ready architecture.

Adding a new service requires no modification to the Booking Service.

Example:

```
BookingCreatedEvent
        │
        ├────────► Payment Service
        ├────────► Analytics Service
        ├────────► Recommendation Service
        ├────────► Audit Service
        └────────► AI Service
```

Each service independently reacts to the same event.

---

## Future Improvements

A production-grade implementation would additionally include:

- Transactional Outbox Pattern
- Retry Topics
- Dead Letter Queue (DLQ)
- Kafka Transactions
- Schema Registry (Avro / Protobuf)
- Distributed Tracing
- Monitoring & Alerting

These features are intentionally excluded to keep the project focused on demonstrating clean event-driven architecture without introducing unnecessary infrastructure complexity.

---

## Key Learnings

This chapter demonstrates the implementation of:

- Apache Kafka
- Event-Driven Architecture
- Producers & Consumers
- Topics & Consumer Groups
- Asynchronous Communication
- Business Event Modeling
- Loose Coupling
- Single Responsibility Principle
- Production-inspired Distributed System Design

The Booking System now follows an event-driven architecture where independent services communicate through business events instead of direct service calls, providing a scalable foundation for future enhancements such as analytics, AI services, recommendation systems, and microservice decomposition.


# Dynamic Movie Search using JPA Specifications

## Why was this required?

Initially, the application supported fetching movies only by predefined APIs such as:

- Get Movie by Id
- Get All Movies
- Upcoming Movies
- Now Showing Movies
- Trending Movies

However, in a real-world movie booking platform like BookMyShow, users expect to search movies using multiple filters simultaneously.

Examples:

- English Action movies
- Comedy movies under 2 hours
- Movies released after 2024
- Action movies in Hindi released after 2023

Creating a separate API or repository method for every combination would quickly become impossible to maintain.

For example:

```
findByGenre()
findByLanguage()
findByGenreAndLanguage()
findByGenreAndLanguageAndDuration()
findByGenreAndLanguageAndDurationAndReleaseDate()
...
```

The number of methods grows exponentially as new search filters are introduced.

To solve this problem, the project uses **Spring Data JPA Specifications**, allowing dynamic SQL query generation at runtime.

---

## Why POST instead of GET?

Searching movies requires multiple optional parameters.

Instead of exposing a long URL such as:

```
GET /movies?genre=ACTION&language=English&duration=120&releasedAfter=2024-01-01
```

the project uses:

```
POST /api/movies/search
```

Request Body

```json
{
    "genre": "ACTION",
    "language": "English",
    "maxDuration": 120,
    "releasedAfter": "2024-01-01"
}
```

Advantages:

- Cleaner API
- Easier to extend
- Supports any number of future search filters
- Avoids very long URLs

---

## Search Criteria DTO

A dedicated DTO was introduced to hold all optional filters.

Current supported filters:

- Movie Name
- Genre
- Language
- Maximum Duration
- Released After

Every field is optional.

Only the provided fields participate in query generation.

---

## Specification Pattern

Instead of building one large query manually, every filter is implemented as an independent Specification.

Example:

- hasName()
- hasGenre()
- hasLanguage()
- hasMaxDuration()
- releasedAfter()

Each Specification is responsible for generating only one SQL predicate.

The final query is dynamically composed by combining all applicable Specifications.

This follows the Single Responsibility Principle and keeps every search condition reusable.

---

## Dynamic Query Generation

The search process follows:

```
Search Request

↓

MovieSearchCriteria

↓

MovieSpecification

↓

Build Required Specifications

↓

Combine using AND

↓

MovieRepository.findAll(specification)

↓

Matching Movies
```

Only filters provided by the client are included in the SQL query.

Examples:

Input

```json
{
    "genre": "ACTION"
}
```

Generated SQL (conceptually)

```
WHERE genre = 'ACTION'
```

Input

```json
{
    "genre": "ACTION",
    "language": "English"
}
```

Generated SQL

```
WHERE genre = 'ACTION'
AND language = 'English'
```

Input

```json
{
    "genre": "ACTION",
    "language": "English",
    "maxDuration": 120
}
```

Generated SQL

```
WHERE genre = 'ACTION'
AND language = 'English'
AND duration <= 120
```

The query adapts automatically based on the request.

---

## Benefits

- No repository method explosion
- Easily extensible
- Reusable Specifications
- Cleaner business logic
- Dynamic SQL generation
- Supports any combination of search filters

---

## Design Decisions

- Used Specifications instead of writing custom JPQL.
- Every search condition was isolated into its own Specification.
- Combined Specifications only when the corresponding filter is present.
- Kept the repository completely generic by extending JpaSpecificationExecutor.
- Search endpoint accepts a request body to simplify future extensions.

---

## Future Scope

Additional filters can be introduced without changing existing logic.

Examples:

- IMDb Rating
- Actor
- Director
- Production House
- Age Rating
- OTT Availability
- Subtitle Language
- City-wise Availability

Only a new Specification needs to be added, making the search engine scalable without modifying existing code.

# Redis Caching

## Why Redis?

Movie booking platforms receive significantly more read requests than write requests.

Examples:

- Fetch Movie Details
- View Upcoming Movies
- View Trending Movies
- Browse Shows
- Theatre Listings

If every request directly queries MySQL, the database becomes the bottleneck under heavy traffic.

Redis was introduced to reduce database load and improve response time by serving frequently accessed data directly from memory.

---

## Redis Use Cases in this Project

Redis is used for two independent purposes.

### 1. Distributed Locking

Implemented during seat booking to prevent concurrent users from booking the same seat simultaneously.

### 2. Distributed Caching

Implemented to cache frequently accessed movie and show data, reducing repetitive database queries.

---

## Caching Strategy

The project follows the **Cache-Aside Pattern**, one of the most commonly used caching strategies.

Workflow:

```
Client Request

↓

Check Redis

↓

Cache Hit ?

↓

YES ----------------→ Return Cached Response

↓

NO

↓

Query MySQL

↓

Store Result in Redis

↓

Return Response
```

The application only queries MySQL when the requested data is absent from Redis.

---

## Cache Configuration

Spring Cache abstraction was integrated with Redis.

Features:

- Redis Cache Manager
- Centralized Cache Configuration
- Time-To-Live (TTL)
- JSON Serialization
- Annotation-based Caching

Caching is enabled globally using:

```
@EnableCaching
```

A dedicated RedisCacheManager configures:

- Redis Connection
- Cache Serialization
- Default TTL
- Cache Behaviour

---

## Cache TTL

All cached entries expire automatically after:

```
10 Minutes
```

Benefits:

- Prevents stale data from living forever
- Controls Redis memory usage
- Automatically refreshes inactive cache entries

---

## Cache Names

Instead of hardcoding cache names throughout the project, all cache names are centralized in:

```
RedisCacheNames
```

Current caches:

```
movies
upcoming-movies
now-showing-movies
trending-movies
shows
movie-shows
theatre-shows
```

This improves maintainability and prevents naming inconsistencies.

---

## Cached APIs

### Movie Details

```
GET /api/movies/{id}
```

Cache Key

```
movies::<movieId>
```

---

### Upcoming Movies

```
POST /api/movies/upcoming
```

Cache Key

```
upcoming-movies::<page-size-sort>
```

---

### Now Showing Movies

```
POST /api/movies/now-showing
```

Cache Key

```
now-showing-movies::<page-size-sort>
```

---

### Trending Movies

```
POST /api/movies/trending
```

Cache Key

```
trending-movies::<page-size>
```

---

### Shows By Movie

```
GET /api/movies/{movieId}/shows
```

Cache Key

```
movie-shows::<movieId>
```

---

### Shows By Theatre

```
GET /api/theatres/{theatreId}/shows
```

Cache Key

```
theatre-shows::<theatreId>
```

---

### Show Details

```
GET /api/shows/{showId}
```

Cache Key

```
shows::<showId>
```

---

## Cache Keys

Cache keys are generated using request parameters that affect the response.

Examples:

```
upcoming-movies::0-20-name-ASC

trending-movies::0-20

movies::<movieId>
```

Only parameters influencing query results are included in cache keys.

For example, Trending Movies ignores sorting, so sorting fields are intentionally excluded from its cache key.

---

## Cache Eviction

Caching alone is insufficient.

Whenever movie data changes, stale cache entries must be removed.

The project uses:

```
@CacheEvict
```

and

```
@Caching
```

to invalidate outdated cache entries.

### Movie Creation

Evicts:

- Upcoming Movies
- Now Showing Movies
- Trending Movies

Reason:

A newly created movie may immediately appear in these discovery APIs.

---

### Movie Update

Evicts:

- Movie Details
- Upcoming Movies
- Trending Movies
- Now Showing Movies
- Movie Shows
- Theatre Shows
- Show Details

Reason:

Updating movie information can affect multiple cached responses across the application.

---

### Movie Deletion

Uses the same eviction strategy as Movie Update.

Removing a movie invalidates all cached data related to that movie.

---

## APIs Not Cached

Dynamic Movie Search intentionally does **not** use caching.

Reason:

The search API supports arbitrary combinations of filters.

Examples:

- Genre
- Language
- Duration
- Release Date
- Movie Name

Caching every possible combination would generate an enormous number of cache entries while providing very low cache hit ratios.

This is a deliberate architectural decision to balance Redis memory usage and performance.

---

## Benefits Achieved

- Reduced database load
- Faster API response times
- Lower latency
- Improved scalability
- Automatic cache invalidation
- Clean separation between read and write operations
- Production-style cache management

---

## Design Decisions

- Implemented using Spring Cache abstraction.
- Centralized cache names in RedisCacheNames.
- Configured a global RedisCacheManager.
- Used annotation-based caching for simplicity.
- Applied cache eviction only to write operations.
- Cached only read-heavy APIs with high reuse.
- Avoided caching highly dynamic APIs such as seat availability and movie search.

---

## Future Improvements

Potential enhancements include:

- Cache statistics and monitoring
- Different TTL values per cache
- Distributed cache invalidation across microservices
- Redis Cluster support
- Cache warming for frequently accessed data
- Multi-level caching (Application Cache + Redis)