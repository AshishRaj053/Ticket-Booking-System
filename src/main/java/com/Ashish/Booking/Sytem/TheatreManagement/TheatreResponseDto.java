package com.Ashish.Booking.Sytem.TheatreManagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TheatreResponseDto {
    private UUID id;
    private String name;
    private  String location;
}
