package com.Ashish.Booking.Sytem.RedisManagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LockInfo {

    private boolean locked;

    private String token;

}

