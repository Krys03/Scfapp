package com.supplychain.scfapp.repository;

import com.supplychain.scfapp.model.Invoice;
import com.supplychain.scfapp.model.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    // Trouver toutes les factures par statut
    List<Invoice> findByStatus(InvoiceStatus status);
}
