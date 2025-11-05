package com.dandaev.edu.entities;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "payment_items")
public class PaymentItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id", nullable = false)
	private Payment payment;

	@Column(name = "item_type", nullable = false, length = 50)
	private String itemType; // "CONSULTATION", "MEDICATION", "PROCEDURE", "MATERIALS"

	@Column(name = "description", nullable = false)
	private String description;

	@Column(name = "quantity", nullable = false)
	private Integer quantity = 1;

	@Column(name = "unit_price", nullable = false, precision = 10)
	private BigDecimal unitPrice;

	@Column(name = "total_price", nullable = false, precision = 10)
	private BigDecimal totalPrice;

	// Конструкторы
	public PaymentItem() {
	}

	public PaymentItem(String itemType, String description, Integer quantity, BigDecimal unitPrice) {
		this.itemType = itemType;
		this.description = description;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
		this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
	}

	// Геттеры и сеттеры
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Payment getPayment() {
		return payment;
	}

	public void setPayment(Payment payment) {
		this.payment = payment;
	}

	public String getItemType() {
		return itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
		calculateTotalPrice();
	}

	public BigDecimal getUnitPrice() {
		return unitPrice;
	}

	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
		calculateTotalPrice();
	}

	public BigDecimal getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(BigDecimal totalPrice) {
		this.totalPrice = totalPrice;
	}

	private void calculateTotalPrice() {
		if (unitPrice != null && quantity != null) {
			this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
		}
	}

	@Override
	public String toString() {
		return "PaymentItem{" +
				"id=" + id +
				", itemType='" + itemType + '\'' +
				", description='" + description + '\'' +
				", quantity=" + quantity +
				", totalPrice=" + totalPrice +
				'}';
	}
}
