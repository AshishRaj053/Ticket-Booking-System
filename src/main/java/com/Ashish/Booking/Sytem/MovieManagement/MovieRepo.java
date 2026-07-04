package com.Ashish.Booking.Sytem.MovieManagement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MovieRepo extends JpaRepository<Movie, UUID>, JpaSpecificationExecutor<Movie> {

}
