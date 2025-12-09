package com.challxbot.repository;

import com.challxbot.domain.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Integer> { // Short -> Integer
    Optional<Topic> findByName(String name);
}