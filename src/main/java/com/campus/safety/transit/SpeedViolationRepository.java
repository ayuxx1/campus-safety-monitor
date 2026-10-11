package com.campus.safety.transit;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpeedViolationRepository extends JpaRepository<SpeedViolation, Long> {

    List<SpeedViolation> findAllByOrderByOccurredAtDesc(Pageable pageable);
}
