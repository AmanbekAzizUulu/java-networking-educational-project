package com.dandaev.edu.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "appointments")
public class Appointment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "patient_id", nullable = false)
	private Patient patient;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "doctor_id", nullable = false)
	private Doctor doctor;

	@Column(name = "appointment_date", nullable = false)
	private LocalDateTime appointmentDate;

	@Column(name = "appointment_duration_minutes")
	private Integer durationMinutes = 30; // по умолчанию 30 минут

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private AppointmentStatus status = AppointmentStatus.SCHEDULED;

	@Column(name = "complaints", columnDefinition = "TEXT")
	private String complaints;

	@Column(name = "diagnosis", columnDefinition = "TEXT")
	private String diagnosis;

	@Column(name = "prescription", columnDefinition = "TEXT")
	private String prescription;

	@Column(name = "recommendations", columnDefinition = "TEXT")
	private String recommendations;

	@Column(name = "notes", columnDefinition = "TEXT")
	private String notes;

	@Column(name = "price", precision = 10)
	private BigDecimal price;

	@Column(name = "is_paid")
	private Boolean isPaid = false;

	@Column(name = "payment_method", length = 50)
	private String paymentMethod;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	// Конструкторы
	public Appointment() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	public Appointment(Patient patient, Doctor doctor, LocalDateTime appointmentDate) {
		this();
		this.patient = patient;
		this.doctor = doctor;
		this.appointmentDate = appointmentDate;
	}

	public Appointment(Patient patient, Doctor doctor, LocalDateTime appointmentDate,
			Integer durationMinutes, BigDecimal price) {
		this(patient, doctor, appointmentDate);
		this.durationMinutes = durationMinutes;
		this.price = price;
	}

	// Enum для статусов приема
	public enum AppointmentStatus {
		SCHEDULED, // Запланирован
		CONFIRMED, // Подтвержден
		IN_PROGRESS, // В процессе
		COMPLETED, // Завершен
		CANCELLED, // Отменен
		NO_SHOW, // Пациент не явился
		RESCHEDULED // Перенесен
	}

	// Геттеры и сеттеры
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Patient getPatient() {
		return patient;
	}

	public void setPatient(Patient patient) {
		this.patient = patient;
		this.updatedAt = LocalDateTime.now();
	}

	public Doctor getDoctor() {
		return doctor;
	}

	public void setDoctor(Doctor doctor) {
		this.doctor = doctor;
		this.updatedAt = LocalDateTime.now();
	}

	public LocalDateTime getAppointmentDate() {
		return appointmentDate;
	}

	public void setAppointmentDate(LocalDateTime appointmentDate) {
		this.appointmentDate = appointmentDate;
		this.updatedAt = LocalDateTime.now();
	}

	public Integer getDurationMinutes() {
		return durationMinutes;
	}

	public void setDurationMinutes(Integer durationMinutes) {
		this.durationMinutes = durationMinutes;
		this.updatedAt = LocalDateTime.now();
	}

	public LocalDateTime getEndDateTime() {
		return appointmentDate.plusMinutes(durationMinutes != null ? durationMinutes : 30);
	}

	public AppointmentStatus getStatus() {
		return status;
	}

	public void setStatus(AppointmentStatus status) {
		this.status = status;
		this.updatedAt = LocalDateTime.now();
	}

	public String getComplaints() {
		return complaints;
	}

	public void setComplaints(String complaints) {
		this.complaints = complaints;
		this.updatedAt = LocalDateTime.now();
	}

	public String getDiagnosis() {
		return diagnosis;
	}

	public void setDiagnosis(String diagnosis) {
		this.diagnosis = diagnosis;
		this.updatedAt = LocalDateTime.now();
	}

	public String getPrescription() {
		return prescription;
	}

	public void setPrescription(String prescription) {
		this.prescription = prescription;
		this.updatedAt = LocalDateTime.now();
	}

	public String getRecommendations() {
		return recommendations;
	}

	public void setRecommendations(String recommendations) {
		this.recommendations = recommendations;
		this.updatedAt = LocalDateTime.now();
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
		this.updatedAt = LocalDateTime.now();
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
		this.updatedAt = LocalDateTime.now();
	}

	public Boolean getIsPaid() {
		return isPaid;
	}

	public void setIsPaid(Boolean isPaid) {
		this.isPaid = isPaid;
		this.updatedAt = LocalDateTime.now();
	}

	public String getPaymentMethod() {
		return paymentMethod;
	}

	public void setPaymentMethod(String paymentMethod) {
		this.paymentMethod = paymentMethod;
		this.updatedAt = LocalDateTime.now();
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	// Бизнес-методы
	public boolean canBeCancelled() {
		return status == AppointmentStatus.SCHEDULED || status == AppointmentStatus.CONFIRMED;
	}

	public boolean isCompleted() {
		return status == AppointmentStatus.COMPLETED;
	}

	public boolean isUpcoming() {
		LocalDateTime now = LocalDateTime.now();
		return (status == AppointmentStatus.SCHEDULED || status == AppointmentStatus.CONFIRMED)
				&& appointmentDate.isAfter(now);
	}

	public boolean isPastDue() {
		return appointmentDate.isBefore(LocalDateTime.now()) &&
				(status == AppointmentStatus.SCHEDULED || status == AppointmentStatus.CONFIRMED);
	}

	// Методы для изменения статуса
	public void confirm() {
		if (this.status == AppointmentStatus.SCHEDULED) {
			this.status = AppointmentStatus.CONFIRMED;
			this.updatedAt = LocalDateTime.now();
		}
	}

	public void complete() {
		if (this.status == AppointmentStatus.IN_PROGRESS || this.status == AppointmentStatus.CONFIRMED) {
			this.status = AppointmentStatus.COMPLETED;
			this.updatedAt = LocalDateTime.now();
		}
	}

	public void cancel() {
		if (canBeCancelled()) {
			this.status = AppointmentStatus.CANCELLED;
			this.updatedAt = LocalDateTime.now();
		}
	}

	public void markAsNoShow() {
		if (this.status == AppointmentStatus.SCHEDULED || this.status == AppointmentStatus.CONFIRMED) {
			this.status = AppointmentStatus.NO_SHOW;
			this.updatedAt = LocalDateTime.now();
		}
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	@Override
	public String toString() {
		return "Appointment{" +
				"id=" + id +
				", patient=" + (patient != null ? patient.getFullName() : "null") +
				", doctor=" + (doctor != null ? doctor.getFullName() : "null") +
				", appointmentDate=" + appointmentDate +
				", status=" + status +
				", isPaid=" + isPaid +
				'}';
	}

	// equals и hashCode по id
	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof Appointment))
			return false;
		Appointment that = (Appointment) o;
		return id != null && id.equals(that.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}
