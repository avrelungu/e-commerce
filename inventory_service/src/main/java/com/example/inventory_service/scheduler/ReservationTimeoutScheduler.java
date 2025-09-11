package com.example.inventory_service.scheduler;

import com.example.inventory_service.model.StockReservation;
import com.example.inventory_service.repository.StockReservationRepository;
import com.example.inventory_service.service.StockReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
@Slf4j
public class ReservationTimeoutScheduler {
    
    private final StockReservationRepository reservationRepository;
    private final StockReservationService reservationService;
    
    public ReservationTimeoutScheduler(
            StockReservationRepository reservationRepository,
            StockReservationService reservationService) {
        this.reservationRepository = reservationRepository;
        this.reservationService = reservationService;
    }
    
    @Scheduled(fixedDelay = 900000)
    @Transactional
    public void releaseExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        
        List<StockReservation> expiredReservations = reservationRepository
            .findByStatusAndExpiresAtBefore("RESERVED", now);
        
        if (!expiredReservations.isEmpty()) {
            log.info("Found {} expired reservations", expiredReservations.size());
            
            for (StockReservation reservation : expiredReservations) {
                try {
                    reservationService.releaseReservation(reservation.getOrderId());
                    log.info("Released expired reservation: {} for order: {}", 
                        reservation.getId(), reservation.getOrderId());
                } catch (Exception e) {
                    log.error("Failed to release reservation: {} for order: {}", 
                        reservation.getId(), reservation.getOrderId(), e);
                }
            }
        } else {
            log.debug("No expired reservations found at: {}", now);
        }
    }
}