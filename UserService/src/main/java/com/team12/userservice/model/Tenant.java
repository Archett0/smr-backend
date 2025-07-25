package com.team12.userservice.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@DiscriminatorValue("TENANT")
public class Tenant extends BaseUser {
    private boolean priceAlertEnabled;
}
