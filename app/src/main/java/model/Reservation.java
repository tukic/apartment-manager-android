package model;


import java.math.BigDecimal;
import java.time.LocalDate;

import enumerations.ReservationStatus;


/**
 * Reservation generated by hbm2java
 */
public class Reservation implements java.io.Serializable {

	private Long reservationId;
	private Tourists tourists;
	private Apartment apartment;
	private LocalDate checkInDate;
	private LocalDate checkOutDate;
	private BigDecimal pricePerNight;
	private BigDecimal totalPrice;
	private ReservationStatus confirmed;
	private BigDecimal advancedPayment;
	private String advPayCurrency;

	public Reservation() {}
	
	public Reservation(int reservationId2, LocalDate checkInDate2, LocalDate checkOutDate2, double pricePerNight2, double totalPrice2, double advancedPayment2, String advPayCurrency2, ReservationStatus confirmed) {
	}

	public Reservation(Apartment apartment, LocalDate checkInDate, LocalDate checkOutDate, BigDecimal pricePerNight,
			BigDecimal totalPrice, String advPayCurrency) {
		this.apartment = apartment;
		this.checkInDate = checkInDate;
		this.checkOutDate = checkOutDate;
		this.pricePerNight = pricePerNight;
		this.totalPrice = totalPrice;
		this.advPayCurrency = advPayCurrency;
	}

	public Reservation(Long reservationId, Tourists tourists, Apartment apartment, LocalDate checkInDate, LocalDate checkOutDate,
			BigDecimal pricePerNight, BigDecimal totalPrice, ReservationStatus confirmed, BigDecimal advancedPayment,
			String advPayCurrency) {
		this.reservationId = reservationId;
		this.tourists = tourists;
		this.apartment = apartment;
		this.checkInDate = checkInDate;
		this.checkOutDate = checkOutDate;
		this.pricePerNight = pricePerNight;
		this.totalPrice = totalPrice;
		this.confirmed = confirmed;
		this.advancedPayment = advancedPayment;
		this.advPayCurrency = advPayCurrency;
	}

	public Long getReservationId() {
		return this.reservationId;
	}

	public void setReservationId(Long reservationId) {
		this.reservationId = reservationId;
	}

	public Tourists getTourists() {
		return this.tourists;
	}

	public void setTourists(Tourists tourists) {
		this.tourists = tourists;
	}

	public Apartment reservationRepositoryAll() {
		return this.apartment;
	}

	public void setApartment(Apartment apartment) {
		this.apartment = apartment;
	}

	public LocalDate getCheckInDate() {
		return this.checkInDate;
	}

	public void setCheckInDate(LocalDate checkInDate) {
		this.checkInDate = checkInDate;
	}

	public LocalDate getCheckOutDate() {
		return this.checkOutDate;
	}

	public void setCheckOutDate(LocalDate checkOutDate) {
		this.checkOutDate = checkOutDate;
	}

	public BigDecimal getPricePerNight() {
		return this.pricePerNight;
	}

	public void setPricePerNight(BigDecimal pricePerNight) {
		this.pricePerNight = pricePerNight;
	}

	public BigDecimal getTotalPrice() {
		return this.totalPrice;
	}

	public void setTotalPrice(BigDecimal totalPrice) {
		this.totalPrice = totalPrice;
	}

	public ReservationStatus getConfirmed() {
		return this.confirmed;
	}

	public void setConfirmed(ReservationStatus confirmed) {
		this.confirmed = confirmed;
	}

	public BigDecimal getAdvancedPayment() {
		return this.advancedPayment;
	}

	public void setAdvancedPayment(BigDecimal advancedPayment) {
		this.advancedPayment = advancedPayment;
	}

	public String getAdvPayCurrency() {
		return this.advPayCurrency;
	}

	public void setAdvPayCurrency(String advPayCurrency) {
		this.advPayCurrency = advPayCurrency;
	}
	
	public Apartment getApartment() {
		return apartment;
	}

	@Override
	public String toString() {
		return "Reservation{" +
				"reservationId=" + reservationId +
				", tourists=" + tourists +
				", apartment=" + apartment +
				", checkInDate=" + checkInDate +
				", checkOutDate=" + checkOutDate +
				", pricePerNight=" + pricePerNight +
				", totalPrice=" + totalPrice +
				", confirmed=" + confirmed +
				", advancedPayment=" + advancedPayment +
				", advPayCurrency='" + advPayCurrency + '\'' +
				'}';
	}
}