package com.rokin.celltracker.repository;

import com.rokin.celltracker.domain.Circle;
import com.rokin.celltracker.domain.Client;
import com.rokin.celltracker.domain.ClientCircle;
import com.rokin.celltracker.domain.ClientCirclePk;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ClientCircleRepository
    extends PagingAndSortingRepository<ClientCircle, ClientCirclePk> {
  Page<ClientCircle> findAll(Pageable pageabel);

  Optional<ClientCircle> findByClientAndCircleAndIsInCircle(Client client, Circle circle,
      Boolean isInCircle);

  List<ClientCircle> findByCircle(Circle circle);
  
  List<ClientCircle> findByClient(Client client);
}
