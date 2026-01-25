package io.factorialsystems.auth.service;

import io.factorialsystems.auth.model.dto.request.CreateUserRequest;
import io.factorialsystems.auth.model.dto.response.UserResponse;
import io.factorialsystems.auth.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordService passwordService;
    private final AuthEventPublisher eventPublisher;
    private final CommunicationsPublisher communicationsPublisher;

    public UserResponse createUser(UUID tenantId, CreateUserRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    public UserResponse getUser(UUID userId) { throw new UnsupportedOperationException("Not implemented yet"); }
    public Page<UserResponse> listUsers(UUID tenantId, String status, String search, Pageable pageable) { throw new UnsupportedOperationException("Not implemented yet"); }
    public UserResponse updateUser(UUID userId, CreateUserRequest request) { throw new UnsupportedOperationException("Not implemented yet"); }
    public void deactivateUser(UUID userId) { throw new UnsupportedOperationException("Not implemented yet"); }
}
