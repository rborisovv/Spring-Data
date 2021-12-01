package com.example.football.repository;

import com.example.football.models.entity.Stat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

//ToDo:
public interface StatRepository extends JpaRepository<Stat,Integer> {
    @Query("select (count(s) > 0) from Stat s where s.passing = ?1 and s.shooting = ?2 and s.endurance = ?3")
    boolean existsByPassingAndShootingAndEndurance(Float passing, Float shooting, Float endurance);

    Optional<Stat> findStatById(Integer id);
}