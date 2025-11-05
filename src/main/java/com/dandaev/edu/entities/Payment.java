package com.dandaev.edu.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "payments")
public class Payment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "appointment_id", nullable = false)
	private Appointment appointment;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	@Column(name = "amount", nullable = false, precision = 10)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(name = "payment_method", nullable = false, length = 20)
	private PaymentMethod paymentMethod;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private PaymentStatus status = PaymentStatus.PENDING;

	@Column(name = "payment_date")
	private LocalDateTime paymentDate;

	@Column(name = "transaction_id", unique = true, length = 100)
	private String transactionId;

	@Column(name = "description")
	private String description;

	@OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<PaymentItem> paymentItems = new ArrayList<>();

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	// Конструкторы
	public Payment() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	public Payment(Appointment appointment, BigDecimal amount, PaymentMethod paymentMethod) {
		this();
		this.appointment = appointment;
		this.patient = appointment.getPatient();
		this.amount = amount;
		this.paymentMethod = paymentMethod;
	}

	// Enum для методов оплаты
	public enum PaymentMethod {
		CASH, // Наличные
		CARD, // Банковская карта
		BANK_TRANSFER, // Банковский перевод
		INSURANCE, // Страховка
		ONLINE, // Онлайн оплата
		CREDIT // В кредит
	}

	// Enum для статусов оплаты
	public enum PaymentStatus {
		PENDING, // Ожидает оплаты
		COMPLETED, // Оплачено
		FAILED, // Ошибка оплаты
		REFUNDED, // Возврат
		PARTIAL, // Частичная оплата
		CANCELLED // Отменено
	}

	// Геттеры и сеттеры
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Appointment getAppointment() {
		return appointment;
	}

	public void setAppointment(Appointment appointment) {
		this.appointment = appointment;
		this.updatedAt = LocalDateTime.now();
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
		this.updatedAt = LocalDateTime.now();
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
		this.updatedAt = LocalDateTime.now();
	}

	public PaymentMethod getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(PaymentMethod paymentMethod) {
		this.paymentMethod = paymentMethod;
		this.updatedAt = LocalDateTime.now();
	}

	public PaymentStatus getStatus() {
		return status;
	}

	public void setStatus(PaymentStatus status) {
		this.status = status;
		this.updatedAt = LocalDateTime.now();
	}

	public LocalDateTime getPaymentDate() {
		return paymentDate;
	}

	public void setPaymentDate(LocalDateTime paymentDate) {
		this.paymentDate = paymentDate;
		this.updatedAt = LocalDateTime.now();
	}

	public String getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(String transactionId) {
		this.transactionId = transactionId;
		this.updatedAt = LocalDateTime.now();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
		this.updatedAt = LocalDateTime.now();
	}

	public List<PaymentItem> getPaymentItems() {
		return paymentItems;
	}

	public void setPaymentItems(List<PaymentItem> paymentItems) {
		this.paymentItems = paymentItems;
		this.updatedAt = LocalDateTime.now();
	}

	public void addPaymentItem(PaymentItem paymentItem) {
		paymentItems.add(paymentItem);
		paymentItem.setPayment(this);
		this.updatedAt = LocalDateTime.now();
	}

	public void removePaymentItem(PaymentItem paymentItem) {
		paymentItems.remove(paymentItem);
		paymentItem.setPayment(null);
		this.updatedAt = LocalDateTime.now();
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	// Бизнес-методы
	public void markAsCompleted(String transactionId) {
		this.status = PaymentStatus.COMPLETED;
		this.paymentDate = LocalDateTime.now();
		this.transactionId = transactionId;
		this.updatedAt = LocalDateTime.now();

		// Обновляем статус оплаты в приеме
		if (appointment != null) {
			appointment.setIsPaid(true);
		}
	}

	public void markAsFailed() {
		this.status = PaymentStatus.FAILED;
		this.updatedAt = LocalDateTime.now();
	}

	public void refund() {
		this.status = PaymentStatus.REFUNDED;
		this.updatedAt = LocalDateTime.now();

		// Обновляем статус оплаты в приеме
		if (appointment != null) {
			appointment.setIsPaid(false);
		}
	}

	public boolean isRefundable() {
		return status == PaymentStatus.COMPLETED &&
				paymentDate != null &&
				paymentDate.isAfter(LocalDateTime.now().minusDays(30)); // Возврат в течение 30 дней
	}

	public BigDecimal calculateTotalFromItems() {
		return paymentItems.stream()
				.map(PaymentItem::getTotalPrice)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	@Override
	public String toString() {
		return "Payment{" +
				"id=" + id +
				", amount=" + amount +
				", status=" + status +
				", paymentMethod=" + paymentMethod +
				'}';
	}
}
