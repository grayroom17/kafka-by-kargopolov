package com.appsdeveloperblog.ws.emailnotification.domain.repository;

import com.appsdeveloperblog.ws.emailnotification.domain.entity.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, Long> {

    boolean existsByMessageId(String messageId);

}
