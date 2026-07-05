package com.Ashish.Booking.Sytem.common;
import lombok.Data;

import java.util.List;

@Data
public class PageResponseDto<T> {

    private List<T> content;

    private Integer page;

    private Integer size;

    private Long totalElements;

    private Integer totalPages;

    private Boolean last;

}
