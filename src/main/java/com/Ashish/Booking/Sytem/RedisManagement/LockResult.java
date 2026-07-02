package com.Ashish.Booking.Sytem.RedisManagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LockResult {
    private Boolean acquired;
    private String token;
}
