package com.driver.services.impl;

import com.driver.model.TripBooking;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;
import com.driver.model.TripStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		customerRepository2.deleteById(customerId);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		List<Driver> driverList = driverRepository2.findAll();
		Driver bookedDriver = null;

		for(Driver driver : driverList){
			if(driver.getCab().getAvailable()){
				bookedDriver = driver;
				break;
			}
		}
		if(bookedDriver == null){
			throw new Exception("No cab available!");
		}
		//find customer by customer to book trip
		Optional<Customer> optionalCustomer = customerRepository2.findById(customerId);
		if(!optionalCustomer.isPresent()){
			throw new Exception("Customer is not found!");
		}

		Customer customer = optionalCustomer.get();

		//now book trip
		TripBooking tripBooking = new TripBooking();
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setStatus(TripStatus.CONFIRMED);
		tripBooking.setBill(0);
		tripBooking.setDriver(bookedDriver);
		tripBooking.setCustomer(customer);

		TripBooking savedTripBooking = tripBookingRepository2.save(tripBooking);

		List<TripBooking> customersBookings = customer.getTripBookingList();
		customersBookings.add(tripBooking);

		List<TripBooking> driversBookig = bookedDriver.getTripBookingList();
		driversBookig.add(tripBooking);

		//save updated driver and customer
		driverRepository2.save(bookedDriver);
		customerRepository2.save(customer);

		return tripBooking;
	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		Optional<TripBooking> optionalTripBooking = tripBookingRepository2.findById(tripId);
		if(!optionalTripBooking.isPresent()){
			return;
		}
		TripBooking tripBooking = optionalTripBooking.get();
		tripBooking.getDriver().getCab().setAvailable(true);
		tripBooking.setBill(0);
		tripBooking.setStatus(TripStatus.CANCELED);

		tripBookingRepository2.save(tripBooking);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		Optional<TripBooking> optionalTripBooking = tripBookingRepository2.findById(tripId);
		if(!optionalTripBooking.isPresent()) return;

		TripBooking tripBooking = optionalTripBooking.get();
		tripBooking.setStatus(TripStatus.COMPLETED);
		tripBooking.getDriver().getCab().setAvailable(true);
		//update bill
		int rate = tripBooking.getDriver().getCab().getPerKmRate();
		int distance = tripBooking.getDistanceInKm();
		int totalAmount = rate * distance;
		tripBooking.setBill(totalAmount);

		tripBookingRepository2.save(tripBooking);
	}
}
