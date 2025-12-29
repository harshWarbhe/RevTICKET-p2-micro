# RevTicket Project - Interview Preparation Guide

## 📋 Project Overview

**RevTicket** is a microservices-based movie ticket booking platform with 12 microservices, built using Spring Boot and Angular.

---

## 🛠️ Tech Stack & Versions

### Frontend

- **Angular**: 18.0.0
- **TypeScript**: 5.4.0
- **RxJS**: 7.8.0
- **Angular Forms & Router**: 18.0.0
- **WebSocket**: @stomp/stompjs 7.2.1, sockjs-client 1.6.1
- **PDF Generation**: jspdf 3.0.4, html2canvas 1.4.1
- **QR Code**: qrcode 1.5.4

### Backend

- **Spring Boot**: 3.2.0
- **Java**: 17
- **Spring Cloud**: 2023.0.0
- **Spring Data JPA**: 3.2.0
- **Hibernate**: (bundled with Spring Data JPA)
- **JWT**: io.jsonwebtoken 0.12.3
- **Lombok**: 1.18.36

### Databases

- **MySQL**: 8.0 (for transactional data)
- **MongoDB**: 8.0 (for reviews and notifications)

### Infrastructure

- **Service Discovery**: Consul 1.15
- **API Gateway**: Spring Cloud Gateway
- **Containerization**: Docker
- **CI/CD**: Jenkins

---

## 🏗️ Architecture

### Microservices (12 Services)

1. **API Gateway** (Port 8080) - Entry point, routing
2. **User Service** (Port 8081) - Authentication, user management
3. **Movie Service** (Port 8082) - Movie catalog
4. **Theater Service** (Port 8083) - Theater/venue management
5. **Showtime Service** (Port 8084) - Show scheduling
6. **Booking Service** (Port 8085) - Ticket booking
7. **Payment Service** (Port 8086) - Payment processing
8. **Review Service** (Port 8087) - Movie reviews (MongoDB)
9. **Search Service** (Port 8088) - Search functionality
10. **Notification Service** (Port 8089) - Notifications (MongoDB)
11. **Settings Service** (Port 8090) - System settings
12. **Dashboard Service** (Port 8091) - Analytics

---

## 💡 Core Concepts Explained

### Framework Libraries Used

Frontend (Angular 18.0.0)
Core Angular Libraries:

@angular/core - Core Angular framework functionality

@angular/common - Common directives, pipes, and services

@angular/forms - Reactive and template-driven forms

@angular/router - Client-side navigation and routing

@angular/platform-browser - DOM manipulation and browser APIs

## Additional Libraries:

rxjs (7.8.0) - Reactive programming with Observables

@stomp/stompjs (7.2.1) - WebSocket communication for real-time notifications

sockjs-client (1.6.1) - WebSocket fallback support

jspdf (3.0.4) - PDF generation for tickets

html2canvas (1.4.1) - Screenshot functionality for PDF generation

qrcode (1.5.4) - QR code generation for e-tickets

## Backend (Spring Boot 3.2.0)

Core Spring Libraries:

spring-boot-starter-web - REST API development with embedded Tomcat

spring-boot-starter-data-jpa - JPA/Hibernate for database operations

spring-boot-starter-security - Authentication and authorization

spring-boot-starter-validation - Bean validation

## Microservices Libraries:

spring-cloud-starter-consul-discovery - Service discovery with Consul

spring-cloud-starter-gateway - API Gateway routing

spring-cloud-starter-openfeign - Inter-service communication

spring-cloud-starter-circuitbreaker-resilience4j - Fault tolerance

## Database & Security:

mysql-connector-j - MySQL database connectivity

spring-boot-starter-data-mongodb - MongoDB operations

io.jsonwebtoken (0.12.3) - JWT token generation and validation

lombok (1.18.36) - Boilerplate code reduction

### 1. Spring Data JPA / Hibernate

**Spring Data JPA** is a Spring framework module that simplifies database operations by providing repository abstractions.

**Hibernate** is the ORM (Object-Relational Mapping) implementation that Spring Data JPA uses under the hood.

**ORM** maps Java objects to database tables automatically.

**Differences:**

- **JPA**: Specification/Interface (javax.persistence)
- **Spring Data JPA**: Spring's implementation with repository pattern
- **Hibernate**: Actual ORM implementation

**In Your Project:**

```java
// Entity
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String email;
}

// Repository
@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

### 2. MongoDB Implementation

**Why Both MySQL and MongoDB?**

- **MySQL**: Transactional data (users, bookings, payments) - ACID compliance needed
- **MongoDB**: Reviews, notifications - flexible schema, high read/write throughput

**Data Format in MongoDB**: BSON (Binary JSON)

**Creating Collections & Relationships:**

```java
// Entity
@Document(collection = "reviews")
@CompoundIndex(name = "user_movie_idx", def = "{'userId': 1, 'movieId': 1}", unique = true)
public class Review {
    @Id
    private String id;
    private String userId;      // Reference to User
    private String movieId;     // Reference to Movie
    private Integer rating;
    private String comment;
}

// Repository
@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {
    List<Review> findByMovieIdAndApprovedTrueOrderByCreatedAtDesc(String movieId);
    Optional<Review> findByUserIdAndMovieId(String userId, String movieId);
}
```

**Relationships in MongoDB**: Use reference IDs (userId, movieId) instead of foreign keys.

---

## 🔐 JWT Authentication Flow

### Implementation:

1. **User Login** → AuthController receives credentials
2. **Validation** → AuthService validates via UserRepository
3. **Token Generation** → JwtUtil creates JWT token
4. **Response** → Token sent to frontend
5. **Subsequent Requests** → JwtAuthenticationFilter validates token
6. **Authorization** → SecurityConfig allows/denies access

```java
// JWT Token Generation
public String generateToken(String email, String role) {
    return Jwts.builder()
        .subject(email)
        .claim("role", role)
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + 86400000))
        .signWith(getSigningKey())
        .compact();
}

// Filter validates every request
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) {
        String token = extractToken(request);
        if (token != null && jwtUtil.validateToken(token)) {
            // Set authentication in SecurityContext
        }
        filterChain.doFilter(request, response);
    }
}
```

---

## 🔄 Complete CRUD Flow (User to DB)

### Example: User Profile Update

1. **Frontend (Angular)**:

```typescript
// Service
updateProfile(userData: any): Observable<any> {
  return this.http.put(`${API_URL}/api/users/profile`, userData);
}

// Component
onSubmit() {
  this.userService.updateProfile(this.profileForm.value)
    .subscribe(response => console.log('Updated'));
}
```

2. **API Gateway**: Routes request to User Service

3. **User Service Controller**:

```java
@PutMapping("/profile")
public ResponseEntity<UserDto> updateProfile(
    @RequestBody UserDto userDto,
    Authentication authentication) {
    String userId = securityUtil.getCurrentUserId(authentication);
    return ResponseEntity.ok(userService.updateProfile(userId, userDto));
}
```

4. **Service Layer**:

```java
public UserDto updateProfile(String userId, UserDto userDto) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));
    user.setName(userDto.getName());
    user.setPhone(userDto.getPhone());
    User updated = userRepository.save(user);
    return convertToDto(updated);
}
```

5. **Repository Layer**:

```java
public interface UserRepository extends JpaRepository<User, String> {
    // save() method inherited from JpaRepository
}
```

6. **Hibernate/JPA**: Generates SQL and executes

```sql
UPDATE users SET name=?, phone=? WHERE id=?
```

---

## 🎯 Java Concepts

### 1. ArrayList - Remove Duplicates

```java
// Method 1: Using LinkedHashSet
List<String> list = new ArrayList<>(Arrays.asList("A", "B", "A", "C"));
List<String> unique = new ArrayList<>(new LinkedHashSet<>(list));

// Method 2: Using Stream API (Java 8)
List<String> unique = list.stream()
    .distinct()
    .collect(Collectors.toList());
```

### 2. Collections Framework

**Interfaces Familiar:**

- **List**: ArrayList, LinkedList, Vector
- **Set**: HashSet, LinkedHashSet, TreeSet
- **Map**: HashMap, LinkedHashMap, TreeMap
- **Queue**: PriorityQueue, LinkedList

### 3. ArrayList vs Vector

| ArrayList        | Vector        |
| ---------------- | ------------- |
| Not synchronized | Synchronized  |
| Fast             | Slower        |
| Grows by 50%     | Grows by 100% |
| Not thread-safe  | Thread-safe   |

### 4. Map & Set

- **Map**: Key-value pairs (HashMap, TreeMap)
- **Set**: Unique elements (HashSet, TreeSet)

### 5. Java 8 Features Used

- **Lambda Expressions**: `list.forEach(item -> System.out.println(item))`
- **Stream API**: `list.stream().filter().map().collect()`
- **Optional**: `Optional<User> user = userRepository.findById(id)`
- **Method References**: `list.forEach(System.out::println)`

### 6. OOP - Inheritance Scenario

```java
// Can we override static methods? NO
// Static methods are hidden, not overridden

// this and super in static context? NO
// Static context has no instance reference

// Child error affects parent? NO
// Inheritance is one-way dependency
```

---

## 🌐 Angular Implementation

### 1. Form Creation & Validation

```typescript
// Component
export class SignupComponent {
  signupForm: FormGroup;

  constructor(private fb: FormBuilder) {
    this.signupForm = this.fb.group({
      name: ["", [Validators.required, Validators.minLength(3)]],
      email: ["", [Validators.required, Validators.email]],
      password: ["", [Validators.required, Validators.minLength(6)]],
    });
  }

  onSubmit() {
    if (this.signupForm.valid) {
      this.authService.signup(this.signupForm.value).subscribe();
    }
  }
}
```

```html
<!-- Template -->
<form [formGroup]="signupForm" (ngSubmit)="onSubmit()">
  <input formControlName="name" />
  <div
    *ngIf="signupForm.get('name')?.invalid && signupForm.get('name')?.touched"
  >
    Name is required
  </div>
  <button [disabled]="signupForm.invalid">Submit</button>
</form>
```

### 2. Data Binding Types

**Interpolation**: `{{ movie.title }}`
**Property Binding**: `[src]="movie.poster"`
**Event Binding**: `(click)="bookTicket()"`
**Two-Way Binding**: `[(ngModel)]="searchQuery"`

### 3. REST API Return Types

- **ResponseEntity<T>**: Full control over HTTP response
- **@ResponseBody**: Automatic JSON conversion
- **String, Object, List**: Direct return

---

## ⚠️ Exception Handling

### 1. Java Exception Handling

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", "Invalid email or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
```

### 2. Microservices Error Handling

- **Circuit Breaker**: Resilience4j for fault tolerance
- **Fallback Methods**: Return default response on service failure
- **Global Exception Handler**: Centralized error handling
- **API Gateway**: Catches and formats errors

### 3. SQL Exception in Angular

```typescript
// Error Interceptor
@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 500) {
          this.alertService.error("Database error occurred");
        }
        return throwError(() => error);
      })
    );
  }
}
```

---

## 🚀 Service Discovery (Consul)

### Implementation:

1. **Consul Server** runs on port 8500
2. **Each microservice** registers itself with Consul on startup
3. **Service Discovery**: Services find each other via Consul
4. **Health Checks**: Consul monitors service health

```yaml
# application.yml
spring:
  cloud:
    consul:
      host: consul
      port: 8500
      discovery:
        service-name: user-service
        health-check-path: /actuator/health
```

---

## ☁️ AWS Deployment

### Deployment Strategy:

1. **Docker Images**: Build images for each service
2. **ECR**: Push images to AWS Elastic Container Registry
3. **ECS/EKS**: Deploy containers
4. **RDS**: MySQL database
5. **DocumentDB**: MongoDB alternative
6. **Load Balancer**: Distribute traffic
7. **Route 53**: DNS management

---

## 📡 API Endpoints Examples

### User Service:

- `POST /api/auth/signup` - Register user
- `POST /api/auth/login` - Login
- `GET /api/users/profile` - Get profile
- `PUT /api/users/profile` - Update profile

### Movie Service:

- `GET /api/movies` - List movies
- `GET /api/movies/{id}` - Movie details
- `POST /api/movies` - Add movie (Admin)

### Booking Service:

- `POST /api/bookings` - Create booking
- `GET /api/bookings/user` - User bookings
- `GET /api/bookings/{id}` - Booking details

---

## 🔗 Backend to Frontend Integration

1. **Proxy Configuration** (proxy.conf.json):

```json
{
  "/api": {
    "target": "http://localhost:8080",
    "secure": false
  }
}
```

2. **HTTP Service**:

```typescript
@Injectable()
export class MovieService {
  private apiUrl = "/api/movies";

  constructor(private http: HttpClient) {}

  getMovies(): Observable<Movie[]> {
    return this.http.get<Movie[]>(this.apiUrl);
  }
}
```

3. **Token Interceptor**:

```typescript
@Injectable()
export class TokenInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler) {
    const token = localStorage.getItem("token");
    if (token) {
      req = req.clone({
        setHeaders: { Authorization: `Bearer ${token}` },
      });
    }
    return next.handle(req);
  }
}
```

---

## 📊 SQL Relationships in Entities

### One-to-Many Example:

```java
// Theater Entity
@Entity
public class Theater {
    @Id
    private String id;

    @OneToMany(mappedBy = "theater", cascade = CascadeType.ALL)
    private List<Screen> screens;
}

// Screen Entity
@Entity
public class Screen {
    @Id
    private String id;

    @ManyToOne
    @JoinColumn(name = "theater_id")
    private Theater theater;
}
```

### Many-to-Many Example:

```java
@Entity
public class Movie {
    @Id
    private String id;

    @ManyToMany
    @JoinTable(
        name = "movie_genre",
        joinColumns = @JoinColumn(name = "movie_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<Genre> genres;
}
```

---

## 🎬 Project Implementation Layers

### 1. Entity Layer

- JPA entities with annotations
- Database table mapping

### 2. Repository Layer

- Extends JpaRepository/MongoRepository
- Custom query methods

### 3. Service Layer

- Business logic
- Transaction management

### 4. Controller Layer

- REST endpoints
- Request/Response handling

### 5. DTO Layer

- Data transfer objects
- Decouples entities from API

### 6. Security Layer

- JWT authentication
- Authorization filters

### 7. Exception Layer

- Global exception handling
- Custom exceptions

---

## 🔧 Spring Boot Dependencies

**Core Dependencies:**

- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-data-mongodb
- spring-boot-starter-security
- spring-boot-starter-validation
- spring-cloud-starter-consul-discovery
- spring-cloud-starter-gateway
- spring-cloud-starter-openfeign
- mysql-connector-j
- io.jsonwebtoken (JWT)
- lombok

---

## 📝 Key Interview Points

### Project Highlights:

1. **Microservices Architecture** with 12 independent services
2. **Polyglot Persistence**: MySQL + MongoDB
3. **Service Discovery**: Consul for dynamic service registration
4. **API Gateway**: Single entry point with routing
5. **JWT Authentication**: Stateless security
6. **Real-time Notifications**: WebSocket with STOMP
7. **Containerization**: Docker for all services
8. **CI/CD**: Jenkins pipeline
9. **Responsive UI**: Angular 18 with modern features
10. **Payment Integration**: Razorpay

### Why This Architecture?

- **Scalability**: Each service scales independently
- **Maintainability**: Separate codebases, easier to manage
- **Technology Diversity**: Use best tool for each service
- **Fault Isolation**: One service failure doesn't crash entire system
- **Team Autonomy**: Different teams can work on different services

---

## 🎯 Common Coding Questions

### HashMap Example:

```java
// Remove duplicates using HashMap
Map<String, Integer> map = new HashMap<>();
List<String> list = Arrays.asList("A", "B", "A", "C");
for (String item : list) {
    map.put(item, map.getOrDefault(item, 0) + 1);
}
List<String> unique = new ArrayList<>(map.keySet());
```

### Stream API Example:

```java
// Filter and collect
List<Movie> popularMovies = movies.stream()
    .filter(m -> m.getRating() > 4.0)
    .sorted(Comparator.comparing(Movie::getReleaseDate).reversed())
    .limit(10)
    .collect(Collectors.toList());
```

---

## 🎤 How to Explain Your Project

**Start with:**
"RevTicket is a microservices-based movie ticket booking platform. It consists of 12 independent microservices built with Spring Boot 3.2.0 and Java 17, with an Angular 18 frontend. We use MySQL for transactional data and MongoDB for reviews and notifications. The architecture includes Consul for service discovery, Spring Cloud Gateway as API gateway, and JWT for authentication. All services are containerized using Docker and deployed with CI/CD pipeline using Jenkins."

**Then explain each service's responsibility and how they communicate via REST APIs and service discovery.**

---

Good luck with your interview! 🚀
