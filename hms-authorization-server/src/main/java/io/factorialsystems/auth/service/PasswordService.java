package io.factorialsystems.auth.service;

import io.factorialsystems.auth.config.AuthProperties;
import io.factorialsystems.auth.exception.BusinessException;
import io.factorialsystems.auth.model.entity.User;
import io.factorialsystems.auth.model.entity.UserPasswordHistory;
import io.factorialsystems.auth.repository.UserPasswordHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordService {
    private final AuthProperties authProperties;
    private final PasswordEncoder passwordEncoder;
    private final UserPasswordHistoryRepository passwordHistoryRepository;
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_+-=[]{}|;:,.<>?";
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateTemporaryPassword() {
        StringBuilder password = new StringBuilder();
        password.append(UPPERCASE.charAt(RANDOM.nextInt(UPPERCASE.length())));
        password.append(LOWERCASE.charAt(RANDOM.nextInt(LOWERCASE.length())));
        password.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        password.append(SPECIAL.charAt(RANDOM.nextInt(SPECIAL.length())));
        String allChars = UPPERCASE + LOWERCASE + DIGITS + SPECIAL;
        int minLength = Math.max(12, authProperties.getPassword().getMinLength());
        for (int i = password.length(); i < minLength; i++) password.append(allChars.charAt(RANDOM.nextInt(allChars.length())));
        List<Character> chars = new ArrayList<>();
        for (char c : password.toString().toCharArray()) chars.add(c);
        Collections.shuffle(chars, RANDOM);
        StringBuilder shuffled = new StringBuilder();
        for (char c : chars) shuffled.append(c);
        return shuffled.toString();
    }

    public void validatePassword(String password) {
        var config = authProperties.getPassword();
        List<String> errors = new ArrayList<>();
        if (password == null || password.length() < config.getMinLength()) errors.add("Password must be at least " + config.getMinLength() + " characters");
        if (password != null && password.length() > config.getMaxLength()) errors.add("Password must not exceed " + config.getMaxLength() + " characters");
        if (config.isRequireUppercase() && password != null && !password.matches(".*[A-Z].*")) errors.add("Password must contain at least one uppercase letter");
        if (config.isRequireLowercase() && password != null && !password.matches(".*[a-z].*")) errors.add("Password must contain at least one lowercase letter");
        if (config.isRequireDigit() && password != null && !password.matches(".*\\d.*")) errors.add("Password must contain at least one digit");
        if (config.isRequireSpecialChar() && password != null && !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*")) errors.add("Password must contain at least one special character");
        if (!errors.isEmpty()) throw new BusinessException("INVALID_PASSWORD", String.join("; ", errors), HttpStatus.BAD_REQUEST);
    }

    public void validatePasswordHistory(User user, String newPassword) {
        int historyCount = authProperties.getPassword().getHistoryCount();
        List<UserPasswordHistory> history = passwordHistoryRepository.findRecentByUserId(user.getId());
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) throw new BusinessException("PASSWORD_REUSE", "Cannot reuse your current password", HttpStatus.BAD_REQUEST);
        for (int i = 0; i < Math.min(history.size(), historyCount); i++) {
            if (passwordEncoder.matches(newPassword, history.get(i).getPasswordHash())) throw new BusinessException("PASSWORD_REUSE", "Cannot reuse any of your last " + historyCount + " passwords", HttpStatus.BAD_REQUEST);
        }
    }

    @Transactional
    public void savePasswordHistory(User user, String oldPasswordHash) {
        UserPasswordHistory history = UserPasswordHistory.builder().user(user).passwordHash(oldPasswordHash).createdAt(LocalDateTime.now()).build();
        passwordHistoryRepository.save(history);
        int keepCount = authProperties.getPassword().getHistoryCount();
        passwordHistoryRepository.deleteOldPasswordHistory(user.getId(), keepCount);
    }
}
