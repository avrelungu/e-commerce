package com.example.payment_service.controller;

import com.example.payment_service.model.Refund;
import com.example.payment_service.service.RefundService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@Slf4j
public class RefundController {

    private final RefundService refundService;

    public RefundController(RefundService refundService) {
        this.refundService = refundService;
    }

    @PostMapping("/{paymentId}/refund")
    public ResponseEntity<RefundResponse> processRefund(
            @PathVariable UUID paymentId,
            @RequestBody RefundRequest request) {
        
        log.info("Processing refund for payment: {}, amount: {}", paymentId, request.getRefundAmount());
        
        try {
            Refund refund = refundService.initiateRefund(
                    paymentId, 
                    request.getRefundAmount(), 
                    request.getReason()
            );
            
            RefundResponse response = RefundResponse.builder()
                    .refundId(refund.getId())
                    .status(refund.getStatus().toString())
                    .refundAmount(refund.getRefundAmount())
                    .currency(refund.getCurrency())
                    .message("Refund initiated successfully")
                    .build();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error processing refund for payment: {}", paymentId, e);
            
            RefundResponse response = RefundResponse.builder()
                    .message("Refund processing failed: " + e.getMessage())
                    .build();
            
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/{paymentId}/refund")
    public ResponseEntity<Refund> getRefund(@PathVariable UUID paymentId) {
        return refundService.findByPaymentId(paymentId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Data
    public static class RefundRequest {
        private BigDecimal refundAmount;
        private String reason;
    }

    @Data
    @lombok.Builder
    public static class RefundResponse {
        private UUID refundId;
        private String status;
        private BigDecimal refundAmount;
        private String currency;
        private String message;
    }
}