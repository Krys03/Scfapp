package com.supplychain.scfapp.controller;

import com.supplychain.scfapp.model.Invoice;
import com.supplychain.scfapp.model.InvoiceStatus;
import com.supplychain.scfapp.repository.InvoiceRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;

    public InvoiceController(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    // ✅ Liste toutes les factures
    @GetMapping
    public List<Invoice> getAll() {
        return invoiceRepository.findAll();
    }

    // ✅ Filtrage par statut
    @GetMapping("/status")
    public List<Invoice> getByStatus(@RequestParam InvoiceStatus value) {
        return invoiceRepository.findByStatus(value);
    }

    // ✅ Crée une nouvelle facture
    @PostMapping
    public Invoice create(@RequestBody Invoice invoice) {
        return invoiceRepository.save(invoice);
    }

    // ✅ Change le statut de manière sécurisée
    @PutMapping("/{id}/status")
    public Invoice updateStatus(@PathVariable Long id, @RequestParam InvoiceStatus status) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invoice not found"));

        if (!isTransitionAllowed(invoice.getStatus(), status)) {
            throw new RuntimeException("Transition from " + invoice.getStatus() + " to " + status + " is not allowed.");
        }

        invoice.setStatus(status);
        return invoiceRepository.save(invoice);
    }

    // 🔒 Règles de transition métier
    private boolean isTransitionAllowed(InvoiceStatus current, InvoiceStatus next) {
        return switch (current) {
            case UNDER_REVIEW -> next == InvoiceStatus.PENDING_APPROVAL || next == InvoiceStatus.REJECTED;
            case PENDING_APPROVAL -> next == InvoiceStatus.VALIDATED || next == InvoiceStatus.REJECTED;
            case VALIDATED -> next == InvoiceStatus.PAID;
            default -> false; // REJECTED, PAID : pas de transition
        };
    }
}
