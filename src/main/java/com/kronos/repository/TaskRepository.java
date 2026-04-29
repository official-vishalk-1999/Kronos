package com.kronos.repository;

import com.kronos.entity.Task;
import com.kronos.entity.DayPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByPlan(DayPlan plan);
    List<Task> findByPlanIn(List<DayPlan> plans);
}
