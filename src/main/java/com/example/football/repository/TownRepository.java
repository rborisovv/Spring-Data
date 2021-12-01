package com.example.football.repository;


import com.example.football.models.entity.Town;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

//ToDo:
public interface TownRepository extends JpaRepository<Town, Integer> {
    @Query("select (count(t) > 0) from Town t where t.name = ?1")
    boolean existsByName(String name);

    Optional<Town> findTownByName(String name);
}
