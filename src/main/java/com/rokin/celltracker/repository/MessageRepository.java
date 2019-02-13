package com.rokin.celltracker.repository;

import com.rokin.celltracker.domain.Message;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface MessageRepository extends PagingAndSortingRepository<Message, Long> {

}
