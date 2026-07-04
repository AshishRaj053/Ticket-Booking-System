package com.Ashish.Booking.Sytem.MovieManagement.search;

import com.Ashish.Booking.Sytem.MovieManagement.Genre;
import com.Ashish.Booking.Sytem.MovieManagement.Movie;
import jakarta.persistence.Column;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class MovieSpecification {

    private static Specification<Movie> hasName(String name){
        return (root,query,cb)->
            cb.like(root.get("name"),"%" + name + "%");
    }
    private static Specification<Movie> hasGenre(Genre genre) {
        return (root, query, cb) ->
                cb.equal(root.get("genre"), genre);
    }
    private static Specification<Movie> hasLanguage(String language) {
        return (root, query, cb) ->
                cb.equal(root.get("language"), language);
    }

    private static Specification<Movie> hasMaxDuration(Integer duration) {
        return (root, query, cb) ->
                cb.lessThanOrEqualTo(root.get("duration"), duration);
    }
    private static Specification<Movie> releasedAfter(LocalDate date) {
        return (root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("releaseDate"), date);
    }

    public static Specification<Movie> search(
            MovieSearchCriteria criteria) {

        Specification<Movie> specification =
                Specification.allOf();

        // build specification
        if(criteria.getName()!=null){
          specification = specification.and(hasName(criteria.getName()));
        }
        if(criteria.getGenre()!=null){
            specification = specification.and(hasGenre(criteria.getGenre()));
        }
        if(criteria.getLanguage()!=null){
            specification = specification.and(hasLanguage(criteria.getLanguage()));
        }
        if(criteria.getMaxDuration()!=null){
            specification = specification.and(hasMaxDuration(criteria.getMaxDuration()));
        }
        if(criteria.getReleasedAfter()!=null){
            specification = specification.and(releasedAfter(criteria.getReleasedAfter()));
        }
        return specification;
    }
}
