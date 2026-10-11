package com.campus.safety.acoustic;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AcousticTelemetryRepository extends MongoRepository<AcousticTelemetry, String> {

    long countBySustainedViolationTrue();

    List<AcousticTelemetry> findAllByOrderByRecordedAtDesc(Pageable pageable);
}
