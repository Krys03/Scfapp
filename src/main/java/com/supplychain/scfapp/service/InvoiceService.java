package com.supplychain.scfapp.service;

import com.supplychain.scfapp.model.*;
import com.supplychain.scfapp.repository.InvoiceRepository;
import com.supplychain.scfapp.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, UserRepository userRepository) {
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
    }

    /**
     * Récupère les factures selon le rôle de l'utilisateur connecté
     */
    public List<Invoice> getInvoicesForUser(User user) {
        if (user.getRoles().contains(Role.ADMIN)) {
            return invoiceRepository.findAll();
        } else if (user.getRoles().contains(Role.SUPPLIER)) {
            return invoiceRepository.findBySupplier(user);
        } else if (user.getRoles().contains(Role.BUYER)) {
            return invoiceRepository.findByBuyer(user);
        } else {
            throw new RuntimeException("Rôle inconnu");
        }
    }

    /**
     * Filtre les factures par statut pour l'utilisateur
     */
    public List<Invoice> getInvoicesByStatus(User user, InvoiceStatus status) {
        if (user.getRoles().contains(Role.ADMIN)) {
            return invoiceRepository.findByStatus(status);
        } else if (user.getRoles().contains(Role.SUPPLIER)) {
            return invoiceRepository.findByStatusAndSupplier(status, user);
        } else if (user.getRoles().contains(Role.BUYER)) {
            return invoiceRepository.findByStatusAndBuyer(status, user);
        } else {
            throw new RuntimeException("Rôle inconnu");
        }
    }

    /**
     * Crée une facture (Supplier uniquement)
     */
    public Invoice createInvoice(User supplier, Invoice invoice) {
        if (!supplier.getRoles().contains(Role.SUPPLIER)) {
            throw new RuntimeException("Seul un Supplier peut créer une facture.");
        }
        invoice.setSupplier(supplier);
        invoice.setStatus(InvoiceStatus.UNDER_REVIEW);
        return invoiceRepository.save(invoice);
    }

    /**
     * Met à jour le statut d'une facture avec vérification de rôle
     */
    public Invoice updateInvoiceStatus(User user, Long id, InvoiceStatus newStatus) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Facture introuvable"));

        // Vérification pour Buyer : il ne peut toucher qu'à ses factures
        if (user.getRoles().contains(Role.BUYER) && !user.equals(invoice.getBuyer())) {
            throw new RuntimeException("Vous ne pouvez modifier que vos propres factures.");
        }

        // Vérifie la transition métier
        if (!isTransitionAllowed(invoice.getStatus(), newStatus)) {
            throw new RuntimeException("Transition interdite : " + invoice.getStatus() + " -> " + newStatus);
        }

        // Si Buyer valide, il devient associé à la facture
        if (newStatus == InvoiceStatus.VALIDATED) {
            invoice.setBuyer(user);
        }

        invoice.setStatus(newStatus);
        return invoiceRepository.save(invoice);
    }

    /**
     * Règles de transition entre statuts
     */
    private boolean isTransitionAllowed(InvoiceStatus current, InvoiceStatus next) {
        return switch (current) {
            case UNDER_REVIEW -> next == InvoiceStatus.PENDING_APPROVAL || next == InvoiceStatus.REJECTED;
            case PENDING_APPROVAL -> next == InvoiceStatus.VALIDATED || next == InvoiceStatus.REJECTED;
            case VALIDATED -> next == InvoiceStatus.PAID;
            default -> false;
        };
    }
}
