package com.supplychain.scfapp.repository;

import com.supplychain.scfapp.model.Invoice;
import com.supplychain.scfapp.model.InvoiceStatus;
import com.supplychain.scfapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    
    // 🔍 Factures selon le statut
    List<Invoice> findByStatus(InvoiceStatus status);

    // 🔍 Factures d’un fournisseur
    List<Invoice> findBySupplier(User supplier);

    // 🔍 Factures d’un acheteur
    List<Invoice> findByBuyer(User buyer);

    // 🔍 Factures selon le statut + fournisseur
    List<Invoice> findByStatusAndSupplier(InvoiceStatus status, User supplier);

    // 🔍 Factures selon le statut + acheteur
    List<Invoice> findByStatusAndBuyer(InvoiceStatus status, User buyer);
}
