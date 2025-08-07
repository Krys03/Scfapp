package com.supplychain.scfapp.model;

import jakarta.persistence.*;

@Entity
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String supplierName;
    private Double amount;
    private String dueDate;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status = InvoiceStatus.UNDER_REVIEW;

    // 🔗 Relations
    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private User supplier;

    @ManyToOne
    @JoinColumn(name = "buyer_id")
    private User buyer;

    // === GETTERS & SETTERS ===

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public InvoiceStatus getStatus() { return status; }
    public void setStatus(InvoiceStatus status) { this.status = status; }

    public User getSupplier() { return supplier; }
    public void setSupplier(User supplier) { this.supplier = supplier; }

    public User getBuyer() { return buyer; }
    public void setBuyer(User buyer) { this.buyer = buyer; }
}
