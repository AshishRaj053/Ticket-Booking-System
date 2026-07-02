package com.Ashish.Booking.Sytem.TheatreManagement;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Repository
public interface TheatreRepo extends JpaRepository<Theatre, UUID> {

}
