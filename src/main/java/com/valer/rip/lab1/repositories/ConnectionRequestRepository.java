package com.valer.rip.lab1.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.valer.rip.lab1.models.ConnectionRequest;
import com.valer.rip.lab1.models.User;

@Repository
public interface ConnectionRequestRepository extends JpaRepository<ConnectionRequest, Integer> {
    Optional<ConnectionRequest> findFirstByClientAndStatus(User client, String status);

    @Query("SELECT cr FROM ConnectionRequest cr WHERE cr.status NOT IN ('DELETED', 'DRAFT', 'REJECTED')")
    List<ConnectionRequest> findAllExceptDeletedAndDraftAndRejected();

    @Query("SELECT cr FROM ConnectionRequest cr WHERE cr.client = :client AND cr.status NOT IN ('DELETED', 'DRAFT', 'REJECTED')")
    List<ConnectionRequest> findByClient(@Param("client") User client);

}
