# Contributing to HMS Platform

Thank you for your interest in contributing to the HMS (Health Management System) Platform! This document provides guidelines and instructions for contributing to the project.

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Commit Messages](#commit-messages)
- [Pull Request Process](#pull-request-process)
- [Testing Guidelines](#testing-guidelines)
- [Documentation](#documentation)

## 🤝 Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive environment for all contributors, regardless of background or experience level.

### Expected Behavior

- Be respectful and considerate
- Provide constructive feedback
- Focus on what is best for the project
- Show empathy towards other contributors

### Unacceptable Behavior

- Harassment or discrimination of any kind
- Trolling or inflammatory comments
- Personal attacks
- Publishing others' private information

## 🚀 Getting Started

### Prerequisites

Before contributing, ensure you have:
- Java 25 or higher
- Maven 3.9 or higher
- Node.js 18.19 or higher (for frontend)
- Docker and Docker Compose
- Git

### Setting Up Development Environment

1. **Fork the repository** on GitHub

2. **Clone your fork**:
   ```bash
   git clone https://github.com/YOUR_USERNAME/hms-backend.git
   cd hms-backend
   ```

3. **Add upstream remote**:
   ```bash
   git remote add upstream https://github.com/factorialsystems/hms-backend.git
   ```

4. **Start infrastructure services**:
   ```bash
   docker-compose up -d postgres redis rabbitmq
   ```

5. **Build and run services**:
   ```bash
   # Auth Server
   cd hms-authorization-server
   ./mvnw spring-boot:run

   # Gateway
   cd hms-gateway
   ./mvnw spring-boot:run

   # Frontend
   cd hms-frontend
   npm install
   npm start
   ```

## 🔄 Development Workflow

### Branch Strategy

We use Git Flow branching model:

- **`master`**: Production-ready code
- **`develop`**: Integration branch for next release
- **`feature/*`**: New features
- **`bugfix/*`**: Bug fixes
- **`hotfix/*`**: Urgent production fixes
- **`release/*`**: Release preparation

### Creating a Feature Branch

1. **Update your local repository**:
   ```bash
   git checkout develop
   git pull upstream develop
   ```

2. **Create a feature branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make your changes** and commit regularly

4. **Keep your branch updated**:
   ```bash
   git fetch upstream
   git rebase upstream/develop
   ```

## 📝 Coding Standards

### Java / Spring Boot

#### General Guidelines

- Follow [Spring Boot Best Practices](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- Use Java 25 features where appropriate (virtual threads, pattern matching, etc.)
- Write clean, self-documenting code
- Keep methods small and focused (max 20-30 lines)
- Use meaningful variable and method names

#### Lombok Usage

**IMPORTANT**: Do NOT use `@Data` annotation as it's not safe

✅ **Good**:
```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private String id;
    private String username;
    private String email;
}
```

❌ **Bad**:
```java
@Data  // Don't use - exposes sensitive data in toString, etc.
public class User {
    private String id;
    private String username;
    private String password;  // Will be exposed in toString!
}
```

#### Service Layer

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(CreateUserRequest request) {
        log.debug("Creating user: {}", request.getUsername());
        // Implementation
    }
}
```

#### Controller Layer

```java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User management APIs")
public class UserController {
    private final UserService userService;

    @PostMapping
    @Operation(summary = "Create a new user")
    public ResponseEntity<UserResponse> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        // Implementation
    }
}
```

#### Exception Handling

Use custom exceptions and global exception handler:

```java
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse(ex.getMessage()));
    }
}
```

### TypeScript / Angular

#### Component Structure

```typescript
@Component({
  selector: 'app-feature',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './feature.component.html',
  styleUrl: './feature.component.scss'
})
export class FeatureComponent implements OnInit {
  // Use modern Angular features
}
```

#### Service Structure

```typescript
@Injectable({
  providedIn: 'root'
})
export class FeatureService {
  constructor(private http: HttpClient) {}

  getData(): Observable<Data[]> {
    return this.http.get<Data[]>('/api/v1/data');
  }
}
```

#### Type Safety

- Always use strict TypeScript types
- Avoid `any` type - use `unknown` or proper interfaces
- Define interfaces for all API responses

```typescript
// Good
interface User {
  id: string;
  username: string;
  email: string;
}

// Bad
let user: any;
```

### Code Formatting

#### Java
- Use 4 spaces for indentation
- Line length: max 120 characters
- Opening braces on same line
- Use IntelliJ IDEA default formatter

#### TypeScript
- Use 2 spaces for indentation
- Line length: max 120 characters
- Use Prettier for formatting
- Run `npm run lint` before committing

### Security Guidelines

1. **Never commit secrets**:
   - No API keys, passwords, or tokens in code
   - Use environment variables or secret management

2. **Input validation**:
   - Always validate user input
   - Use `@Valid` annotation in controllers
   - Sanitize data before processing

3. **SQL injection prevention**:
   - Use JPA/Hibernate parameterized queries
   - Never concatenate SQL strings

4. **XSS prevention**:
   - Angular sanitizes by default - don't bypass
   - Never use `innerHTML` with user content

5. **Authentication**:
   - Always check authentication on protected endpoints
   - Use `@PreAuthorize` for method-level security

## 💬 Commit Messages

Follow [Conventional Commits](https://www.conventionalcommits.org/) specification:

### Format

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- **feat**: New feature
- **fix**: Bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting)
- **refactor**: Code refactoring
- **test**: Adding or updating tests
- **chore**: Maintenance tasks

### Examples

**Good**:
```
feat(auth): add password reset functionality

Implement password reset flow with email verification.
Users can request password reset via email link.

Closes #123
```

**Good**:
```
fix(auth): resolve token refresh race condition

Fixed issue where concurrent requests could cause
multiple token refresh attempts.

Fixes #456
```

**Bad**:
```
fixed stuff
```

**Bad**:
```
Updated code
```

## 🔍 Pull Request Process

### Before Submitting

1. **Update your branch**:
   ```bash
   git fetch upstream
   git rebase upstream/develop
   ```

2. **Run tests**:
   ```bash
   ./mvnw test  # Backend
   npm test     # Frontend
   ```

3. **Check code quality**:
   ```bash
   ./mvnw verify  # Backend
   npm run lint   # Frontend
   ```

4. **Update documentation** if needed

### Submitting Pull Request

1. **Push your branch**:
   ```bash
   git push origin feature/your-feature-name
   ```

2. **Create Pull Request** on GitHub:
   - Use a clear, descriptive title
   - Fill out the PR template completely
   - Link related issues
   - Add screenshots for UI changes

3. **PR Title Format**:
   ```
   [Type] Brief description

   Examples:
   [Feature] Add patient search functionality
   [Fix] Resolve login redirect loop
   [Docs] Update API documentation
   ```

4. **PR Description Template**:
   ```markdown
   ## Description
   Brief description of changes

   ## Related Issues
   Closes #123

   ## Type of Change
   - [ ] Bug fix
   - [ ] New feature
   - [ ] Breaking change
   - [ ] Documentation update

   ## Testing
   - [ ] Unit tests added/updated
   - [ ] Integration tests added/updated
   - [ ] Manual testing performed

   ## Screenshots (if applicable)

   ## Checklist
   - [ ] Code follows style guidelines
   - [ ] Self-review performed
   - [ ] Comments added for complex code
   - [ ] Documentation updated
   - [ ] No new warnings generated
   - [ ] Tests pass locally
   ```

### Code Review Process

1. **At least one approval** required from maintainers
2. **Address review comments**
3. **All CI checks** must pass
4. **Squash commits** before merging (if requested)

## 🧪 Testing Guidelines

### Backend Testing

#### Unit Tests

```java
@SpringBootTest
class UserServiceTest {
    @MockBean
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    void shouldCreateUser() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setUsername("testuser");

        // When
        User user = userService.createUser(request);

        // Then
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
    }
}
```

#### Integration Tests (Testcontainers)

```java
@SpringBootTest
@Testcontainers
class UserControllerIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Test
    void shouldCreateUserViaAPI() {
        // Test implementation
    }
}
```

### Frontend Testing

```typescript
describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [LoginComponent]
    });
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should validate form', () => {
    component.loginForm.setValue({
      username: '',
      password: ''
    });
    expect(component.loginForm.valid).toBeFalsy();
  });
});
```

### Test Coverage

- Aim for **80%+ code coverage**
- Critical paths should have **100% coverage**
- Run coverage reports:
  ```bash
  ./mvnw test jacoco:report  # Backend
  npm run test:coverage       # Frontend
  ```

## 📚 Documentation

### Code Documentation

#### JavaDoc

```java
/**
 * Creates a new user in the system.
 *
 * @param request the user creation request containing username, email, etc.
 * @return the created user with generated ID
 * @throws DuplicateResourceException if username already exists
 */
public User createUser(CreateUserRequest request) {
    // Implementation
}
```

#### TSDoc

```typescript
/**
 * Authenticates a user with the provided credentials.
 *
 * @param username - The user's username
 * @param password - The user's password
 * @returns Observable of login response with tokens
 * @throws AuthenticationException if credentials are invalid
 */
login(username: string, password: string): Observable<LoginResponse> {
  // Implementation
}
```

### API Documentation

Use OpenAPI/Swagger annotations:

```java
@Operation(
    summary = "Create a new user",
    description = "Creates a new user account with the provided details",
    tags = {"User Management"}
)
@ApiResponses({
    @ApiResponse(responseCode = "201", description = "User created successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid request"),
    @ApiResponse(responseCode = "409", description = "Username already exists")
})
```

### README Updates

- Update README.md when adding new features
- Include setup instructions for new dependencies
- Add examples for new APIs
- Keep architecture diagrams current

## 🐛 Reporting Bugs

### Before Reporting

1. Check if bug already reported
2. Try to reproduce in latest version
3. Gather relevant information:
   - Operating system
   - Java/Node version
   - Steps to reproduce
   - Expected vs actual behavior
   - Error logs

### Bug Report Template

```markdown
## Bug Description
Clear description of the bug

## Steps to Reproduce
1. Go to '...'
2. Click on '...'
3. See error

## Expected Behavior
What should happen

## Actual Behavior
What actually happens

## Environment
- OS: [e.g., macOS 14]
- Java: [e.g., 25]
- Browser: [e.g., Chrome 120]

## Logs
```
Relevant error logs
```

## Screenshots
If applicable
```

## 💡 Feature Requests

### Feature Request Template

```markdown
## Feature Description
Clear description of the feature

## Problem Solved
What problem does this solve?

## Proposed Solution
How should it work?

## Alternatives Considered
Other approaches you've thought about

## Additional Context
Any other relevant information
```

## 📞 Getting Help

- **Documentation**: Check [CLAUDE.md](CLAUDE.md) and service READMEs
- **Issues**: Search existing GitHub issues
- **Questions**: Open a discussion on GitHub
- **Email**: support@factorialsystems.io

## 🏆 Recognition

Contributors will be recognized in:
- CONTRIBUTORS.md file
- Release notes
- Project documentation

Thank you for contributing to HMS Platform! 🙏

---

**Last Updated**: 2026-01-24
