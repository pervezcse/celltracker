package com.rokin.celltracker.repository;

import com.rokin.celltracker.domain.Client;
import com.rokin.celltracker.domain.ClientFavoritePlace;
import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ClientFavouritePlaceRepository
    extends PagingAndSortingRepository<ClientFavoritePlace, Long> {

  List<ClientFavoritePlace> findByClient(Client client);
  
}
