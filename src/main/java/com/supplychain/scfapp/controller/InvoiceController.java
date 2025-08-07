package com.supplychain.scfapp.controller;

import com.supplychain.scfapp.model.Invoice;
import com.supplychain.scfapp.model.InvoiceStatus;
import com.supplychain.scfapp.model.User;
import com.supplychain.scfapp.repository.UserRepository;
import com.supplychain.scfapp.service.InvoiceService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final UserRepository userRepository;

    public InvoiceController(InvoiceService invoiceService, UserRepository userRepository) {
        this.invoiceService = invoiceService;
        this.userRepository = userRepository;
    }

    /**
     * Liste des factures selon le rôle de l'utilisateur connecté.
     * - Admin : toutes les factures
     * - Supplier : uniquement ses factures
     * - Buyer : uniquement ses factures à payer
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPPLIER','BUYER')")
    public List<Invoice> getAll(Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        return invoiceService.getInvoicesForUser(user);
    }

    /**
     * Filtrer les factures par statut (limité par rôle)
     */
    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN','SUPPLIER','BUYER')")
    public List<Invoice> getByStatus(@RequestParam InvoiceStatus value, Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        return invoiceService.getInvoicesByStatus(user, value);
    }

    /**
     * Créer une nouvelle facture (Supplier uniquement)
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPPLIER')")
    public Invoice create(@RequestBody Invoice invoice, Authentication auth) {
        User supplier = userRepository.findByUsername(auth.getName()).orElseThrow();
        return invoiceService.createInvoice(supplier, invoice);
    }

    /**
     * Changer le statut d'une facture (Buyer/Admin uniquement)
     * - Buyer ne peut changer que ses factures
     * - Admin peut changer n'importe quelle facture
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','BUYER')")
    public Invoice updateStatus(@PathVariable Long id, @RequestParam InvoiceStatus status, Authentication auth) {
        User user = userRepository.findByUsername(auth.getName()).orElseThrow();
        return invoiceService.updateInvoiceStatus(user, id, status);
    }
}
