package com.kronos.repository;

import com.kronos.entity.DayPlan;
import com.kronos.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DayPlanRepository extends JpaRepository<DayPlan, Long> {
    Optional<DayPlan> findByUserAndDate(User user, LocalDate date);
    List<DayPlan> findByUser(User user);
    long countByUser(User user);
    List<DayPlan> findByUserAndDateBetween(User user, LocalDate start, LocalDate end);
}
