package com.alimmit.golf.course;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface CourseRepository extends JpaRepository<CourseEntity, UUID> {}
