package com.Ashish.Booking.Sytem.ShowSeatManagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShowSeatResponseDto {
   private String label;

   private SeatStatus status;
}
