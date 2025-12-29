# RevTicket Microservices Architecture

## Overview
RevTicket is a movie ticket booking platform built using microservices architecture with Spring Cloud framework. The system consists of 11 microservices, an API Gateway, service discovery, and polyglot persistence.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              REVTICKET ARCHITECTURE                             │
└─────────────────────────────────────────────────────────────────────────────────┘

┌─────────────────┐
│   Angular UI    │ ←── User Interface (Port 4200)
│   (Frontend)    │     • Movie Browsing
└─────────┬───────┘     • Booking Management
          │ HTTP/HTTPS  • User Dashboard
          ▼
┌─────────────────┐
│   API Gateway   │ ←── Single Entry Point (Port 8080)
│  (Spring Cloud  │     • JWT Authentication
│    Gateway)     │     • Request Routing
└─────────┬───────┘     • Load Balancing
          │             • Rate Limiting
          ▼
┌─────────────────┐
│     Consul      │ ←── Service Discovery (Port 8500)
│ (Service Reg.)  │     • Service Registration
└─────────────────┘     • Health Monitoring
                        • Configuration Management

          │ Service Discovery & Load Balancing
          ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            MICROSERVICES LAYER                                 │
├─────────────────┬─────────────────┬─────────────────┬─────────────────────────┤
│  User Service   │  Movie Service  │ Theater Service │   Showtime Service      │
│   (Port 8081)   │   (Port 8082)   │   (Port 8083)   │     (Port 8084)         │
│                 │                 │                 │                         │
│ • Authentication│ • Movie Catalog │ • Theater Mgmt  │ • Schedule Management   │
│ • User Profiles │ • Movie Details │ • Screen Mgmt   │ • Time Slot Management  │
│ • JWT Generation│ • Genre/Language│ • Location Mgmt │ • Availability Check    │
│ • OAuth2 (Google)│ • Movie Search │ • Seat Layout   │ • Show Scheduling       │
└─────────┬───────┴─────────┬───────┴─────────┬───────┴─────────┬───────────────┘
          │                 │                 │                 │
├─────────────────┬─────────────────┬─────────────────┬─────────────────────────┤
│ Booking Service │ Payment Service │ Review Service  │  Notification Service   │
│   (Port 8085)   │   (Port 8086)   │   (Port 8087)   │     (Port 8089)         │
│                 │                 │                 │                         │
│ • Seat Selection│ • Payment Proc. │ • Movie Reviews │ • Email Notifications   │
│ • Booking Logic │ • Razorpay Integ│ • Rating System │ • SMS Alerts           │
│ • Cancellation  │ • Transaction   │ • User Feedback │ • Booking Confirmations │
│ • Ticket Gen.   │ • Refund Logic  │ • Review Moderation│ • System Alerts      │
└─────────┬───────┴─────────┬───────┴─────────┬───────┴─────────┬───────────────┘
          │                 │                 │                 │
├─────────────────┬─────────────────┬─────────────────┬─────────────────────────┤
│ Search Service  │Settings Service │Dashboard Service│     Additional Services │
│   (Port 8088)   │   (Port 8090)   │   (Port 8091)   │                         │
│                 │                 │                 │                         │
│ • Movie Search  │ • App Config    │ • Admin Panel   │ • API Gateway Routing   │
│ • Filter/Sort   │ • User Prefs    │ • Analytics     │ • Cross-cutting Concerns│
│ • Recommendations│ • System Settings│ • Reports      │ • Monitoring & Logging  │
│ • Advanced Query│ • Feature Flags │ • User Stats    │                         │
└─────────┬───────┴─────────┬───────┴─────────┬───────┴─────────────────────────┘
          │                 │                 │
          ▼                 ▼                 ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            DATABASE LAYER                                      │
├─────────────────────────────────┬───────────────────────────────────────────────┤
│           MySQL                 │              MongoDB                          │
│        (Port 3306)              │            (Port 27017)                       │
│                                 │                                               │
│ Relational Databases:           │ Document Databases:                           │
│ • user_service_db              │ • review_service_db                          │
│ • movie_service_db             │ • notification_service_db                    │
│ • theater_service_db           │                                               │
│ • showtime_service_db          │ Collections:                                  │
│ • booking_service_db           │ • reviews (movie ratings & comments)         │
│ • payment_service_db           │ • notifications (email/sms logs)             │
│ • settings_service_db          │ • user_preferences (flexible schema)         │
│                                 │                                               │
│ ACID Transactions               │ Flexible Schema & Scalability                 │
│ Structured Data                 │ JSON-like Documents                           │
└─────────────────────────────────┴───────────────────────────────────────────────┘
```

## Service Communication Flow

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           COMMUNICATION PATTERNS                               │
└─────────────────────────────────────────────────────────────────────────────────┘

Frontend ──HTTP──► API Gateway ──HTTP──► Microservices
    │                   │                      │
    │                   ▼                      │
    │              JWT Validation               │
    │                   │                      │
    │                   ▼                      ▼
    │              Consul Discovery ──► Service Resolution
    │                   │                      │
    │                   ▼                      │
    └──────────── Load Balancing ──────────────┘
                        │
                        ▼
               RestTemplate + @LoadBalanced
                        │
                        ▼
              Service-to-Service Communication

Example Flow: Book Movie Ticket
1. Frontend → API Gateway → User Service (Authentication)
2. API Gateway → Movie Service (Get Movie Details)
3. API Gateway → Theater Service (Get Available Seats)
4. API Gateway → Booking Service (Create Booking)
5. Booking Service → Payment Service (Process Payment)
6. Booking Service → Notification Service (Send Confirmation)
```

## Security Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                              SECURITY FLOW                                     │
└─────────────────────────────────────────────────────────────────────────────────┘

1. User Login ──► User Service ──► Credential Validation ──► JWT Generation
                      │                                           │
                      ▼                                           ▼
                 Password Hashing                            Token Signing
                 (BCrypt Encoder)                           (HMAC-SHA256)
                      │                                           │
                      ▼                                           ▼
2. JWT Token ──► API Gateway ──► Token Validation ──► Header Enrichment
                      │                                           │
                      ▼                                           ▼
3. Valid Request ──► Microservice ──► Security Context ──► Business Logic
                      │                                           │
                      ▼                                           ▼
4. Response ──► API Gateway ──► Response Filtering ──► Frontend

Security Features:
• JWT-based stateless authentication
• Role-based access control (USER/ADMIN)
• OAuth2 integration (Google)
• Password encryption (BCrypt)
• CORS configuration
• Request/Response filtering
```

## Technology Stack

### Backend Technologies
- **Framework**: Spring Boot 3.2.0
- **Microservices**: Spring Cloud 2023.0.0
- **API Gateway**: Spring Cloud Gateway
- **Service Discovery**: HashiCorp Consul
- **Authentication**: JWT (JJWT 0.12.3)
- **Security**: Spring Security
- **Database**: MySQL 8.0, MongoDB 8.0
- **ORM**: Spring Data JPA, Spring Data MongoDB
- **Build Tool**: Maven
- **Java Version**: 17

### Frontend Technologies
- **Framework**: Angular 17
- **UI Library**: Angular Material
- **HTTP Client**: Angular HttpClient
- **State Management**: Angular Signals
- **Styling**: CSS3, Angular Flex Layout

### Infrastructure
- **Containerization**: Docker & Docker Compose
- **Service Registry**: Consul
- **Load Balancing**: Spring Cloud LoadBalancer
- **Monitoring**: Spring Boot Actuator

## Microservices Details

| Service | Port | Database | Responsibility |
|---------|------|----------|----------------|
| API Gateway | 8080 | - | Request routing, authentication, load balancing |
| User Service | 8081 | MySQL | User management, authentication, profiles |
| Movie Service | 8082 | MySQL | Movie catalog, details, search |
| Theater Service | 8083 | MySQL | Theater management, screens, locations |
| Showtime Service | 8084 | MySQL | Show scheduling, time slots |
| Booking Service | 8085 | MySQL | Ticket booking, seat selection |
| Payment Service | 8086 | MySQL | Payment processing, transactions |
| Review Service | 8087 | MongoDB | Movie reviews, ratings |
| Search Service | 8088 | - | Advanced search, recommendations |
| Notification Service | 8089 | MongoDB | Email/SMS notifications |
| Settings Service | 8090 | MySQL | Application configuration |
| Dashboard Service | 8091 | - | Admin analytics, reports |

## API Endpoints Structure

```
Base URL: http://localhost:8080

Authentication:
POST /api/auth/login
POST /api/auth/signup
POST /api/auth/forgot-password

Movies:
GET /api/movies
GET /api/movies/{id}

Theaters:
GET /api/theaters
GET /api/theaters/{id}

Bookings:
POST /api/bookings
GET /api/bookings/my-bookings
POST /api/bookings/{id}/cancel

Payments:
POST /api/payments
GET /api/payments/{transactionId}/status

Admin:
GET /api/admin/dashboard/overview
GET /api/admin/reports/full
```

## Deployment Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                            DEPLOYMENT STACK                                    │
└─────────────────────────────────────────────────────────────────────────────────┘

Docker Containers:
├── Infrastructure Services
│   ├── consul (Service Discovery)
│   ├── mysql (Relational Database)
│   └── mongodb (Document Database)
├── Application Services
│   ├── api-gateway
│   ├── frontend (Angular + Nginx)
│   └── 11 Microservices
│       ├── user-service
│       ├── movie-service
│       ├── theater-service
│       ├── showtime-service
│       ├── booking-service
│       ├── payment-service
│       ├── review-service
│       ├── search-service
│       ├── notification-service
│       ├── settings-service
│       └── dashboard-service

Orchestration: Docker Compose
Network: Bridge Network (revticket-network)
Volumes: Persistent storage for databases
```

## Key Features

### Business Features
- **User Management**: Registration, login, profile management
- **Movie Catalog**: Browse movies, view details, ratings
- **Theater Management**: Multiple theaters, screens, seat layouts
- **Booking System**: Seat selection, booking confirmation, cancellation
- **Payment Integration**: Razorpay payment gateway
- **Review System**: Movie ratings and reviews
- **Admin Dashboard**: Analytics, reports, user management
- **Notifications**: Email confirmations, SMS alerts

### Technical Features
- **Microservices Architecture**: Independently deployable services
- **Service Discovery**: Automatic service registration and discovery
- **Load Balancing**: Client-side load balancing
- **API Gateway**: Single entry point with routing and security
- **JWT Authentication**: Stateless authentication
- **Polyglot Persistence**: MySQL for relational data, MongoDB for documents
- **Containerization**: Docker-based deployment
- **Health Monitoring**: Service health checks via Actuator

## Scalability Considerations

1. **Horizontal Scaling**: Each microservice can be scaled independently
2. **Database Sharding**: Separate databases per service
3. **Caching**: Can add Redis for session management and caching
4. **Message Queues**: Can integrate Kafka/RabbitMQ for async communication
5. **CDN**: Static assets can be served via CDN
6. **Auto-scaling**: Container orchestration with Kubernetes

## Future Enhancements

- **Circuit Breaker**: Resilience4j for fault tolerance
- **Distributed Tracing**: Zipkin/Jaeger for request tracing
- **Centralized Logging**: ELK stack for log aggregation
- **Configuration Management**: Spring Cloud Config Server
- **Message Broker**: Kafka for event-driven architecture
- **Caching Layer**: Redis for performance optimization
- **Monitoring**: Prometheus + Grafana for metrics
- **Container Orchestration**: Kubernetes for production deployment

---

**Project**: RevTicket Movie Booking Platform  
**Architecture**: Microservices with Spring Cloud  
**Developer**: Harsh Warbhe  
**Technology Stack**: Spring Boot, Angular, MySQL, MongoDB, Docker