package io.factorialsystems.auth.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor @Embeddable
public class Address {
    @Column(name = "street", length = 255) private String street;
    @Column(name = "city", length = 100) private String city;
    @Column(name = "lga", length = 100) private String lga;
    @Column(name = "state", length = 100) private String state;
    @Column(name = "country", length = 100) private String country;
    @Column(name = "postal_code", length = 20) private String postalCode;
}
