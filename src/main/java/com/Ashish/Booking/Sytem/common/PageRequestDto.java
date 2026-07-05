package com.Ashish.Booking.Sytem.common;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Data
public class PageRequestDto {

    private Integer page = 0;

    private Integer size = 20;

    private String sortBy = "id";

    private String direction = "ASC";

    public Pageable toPageable() {

        Sort.Direction sortDirection =
                Sort.Direction.fromString(direction);

        return PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, sortBy)
        );

    }

    public Pageable toPageableWithoutSort() {
        return PageRequest.of(page, size);
    }

}