package com.driver.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.driver.model.TripBooking;

@Repository
public interface TripBookingRepository extends JpaRepository<TripBooking, Integer>{

}
