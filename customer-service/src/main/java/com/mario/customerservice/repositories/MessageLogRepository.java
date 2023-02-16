package com.mario.customerservice.repositories;

import com.mario.customerservice.domain.entity.MessageLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MessageLogRepository extends JpaRepository<MessageLog, UUID> {

}
