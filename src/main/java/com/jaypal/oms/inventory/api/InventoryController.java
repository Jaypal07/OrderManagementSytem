package com.jaypal.oms.inventory.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    private final InventoryApi inventoryApi;

    public InventoryController(InventoryApi inventoryApi) {
        this.inventoryApi = inventoryApi;
    }

    // Usually we wouldn't expose raw reserve/release via REST for external users,
    // but for admin/testing it's useful.
    // For a monolith, inter-module communication is via Java API (InventoryApi),
    // not REST.
    // So this controller is primarily for debug/admin/external system sync.

    @PostMapping("/reserve")
    public ResponseEntity<?> reserveStock(@RequestBody ReserveRequest request) {
        try {
            inventoryApi.reserveStock(request.orderId(), request.skuQuantities());
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/release")
    public ResponseEntity<?> releaseStock(@RequestBody ReleaseRequest request) {
        inventoryApi.releaseStock(request.orderId());
        return ResponseEntity.ok().build();
    }

    // Simple DTOs
    public record ReserveRequest(UUID orderId, Map<String, Integer> skuQuantities) {
    }

    public record ReleaseRequest(UUID orderId) {
    }
}
