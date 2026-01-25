package io.factorialsystems.auth.model.enums;
import lombok.Getter;
@Getter
public enum SubscriptionPlan {
    FREE_TRIAL(100, 5000, 30), BASIC(500, 50000, 365), PROFESSIONAL(2000, 500000, 365), ENTERPRISE(10000, -1, 365);
    private final int requestsPerMinute; private final int requestsPerDay; private final int trialDays;
    SubscriptionPlan(int rpm, int rpd, int td) { this.requestsPerMinute = rpm; this.requestsPerDay = rpd; this.trialDays = td; }
    public boolean isUnlimitedDaily() { return requestsPerDay == -1; }
}
