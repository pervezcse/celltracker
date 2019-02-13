package com.rokin.celltracker.repository;

import com.rokin.celltracker.domain.Client;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ClientRepository extends PagingAndSortingRepository<Client, Long> {
  Page<Client> findAll(Pageable pageabel);

  Optional<Client> findByEmailOrCellNo(String email, String cellNo);

  Optional<Client> findByEmail(String email);
  
  Optional<Client> findByCellNo(String cellNo);
}
