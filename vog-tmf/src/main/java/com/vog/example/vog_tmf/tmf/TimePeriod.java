package com.vog.example.vog_tmf.tmf;

import java.time.OffsetDateTime;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** TMF630 common type: a validity window. Reused by entities and DTOs. */
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimePeriod {

    private OffsetDateTime startDateTime;

    private OffsetDateTime endDateTime;
}
