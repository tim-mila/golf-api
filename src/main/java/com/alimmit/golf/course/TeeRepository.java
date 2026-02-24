package com.alimmit.golf.course;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface TeeRepository extends JpaRepository<TeeEntity, UUID> {

  List<TeeEntity> findByCourse_Id(UUID courseId);

  void deleteByCourse_Id(UUID courseId);
}
