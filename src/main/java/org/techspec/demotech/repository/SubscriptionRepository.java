package org.techspec.demotech.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.techspec.demotech.entity.Subscription;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByUserId(Long userId);

    Optional<Subscription> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndServiceName(Long userId, String serviceName);

    @Query("SELECT s.serviceName, COUNT(s) as count FROM Subscription s " +
            "GROUP BY s.serviceName ORDER BY count DESC")
    List<Object[]> findTopServicesBySubscriptionCount();
}
