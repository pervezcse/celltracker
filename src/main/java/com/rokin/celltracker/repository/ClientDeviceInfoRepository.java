package com.rokin.celltracker.repository;

import com.rokin.celltracker.domain.Client;
import com.rokin.celltracker.domain.ClientDeviceInfo;
import java.util.List;
import org.springframework.data.repository.PagingAndSortingRepository;


public interface ClientDeviceInfoRepository
    extends PagingAndSortingRepository<ClientDeviceInfo, Long> {
  List<ClientDeviceInfo> findByClientAndTimestampGreaterThanEqualAndTimestampLessThanEqual(
      Client client, Long fromTime, Long toTime);  
}
