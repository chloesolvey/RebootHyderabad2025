package com.reboot.zerotocloud.controller;

import com.reboot.zerotocloud.model.BillingInfo;
import com.reboot.zerotocloud.service.BillingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/gcp/billing")
public class BillingController {

    @Autowired
    private BillingService billingService;

    @GetMapping("/summary")
    public ResponseEntity<BillingInfo> getBillingSummary() {
        System.out.println("📡 Called /summary endpoint");
        try {
            BillingInfo info = billingService.fetchBillingData();
            System.out.println("✅ Billing fetched: " + info);
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            System.out.println("Exception caught:");
            e.printStackTrace();  // Don't miss this line!
            return ResponseEntity.internalServerError().build();
        }
    }

}
