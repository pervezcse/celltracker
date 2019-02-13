package com.rokin.celltracker.repository;

import com.rokin.celltracker.domain.Circle;
import com.rokin.celltracker.domain.Client;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CircleRepository extends PagingAndSortingRepository<Circle, String> {
  Page<Circle> findAll(Pageable pageabel);
  
  Optional<Circle> findByCode(String code);
  
  Optional<Circle> findByIdAndOwner(String id, Client owner);
  
  
}
