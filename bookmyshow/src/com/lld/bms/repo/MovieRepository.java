package com.lld.bms.repo;

import com.lld.bms.domain.Movie;

import java.util.List;

public interface MovieRepository extends Repository<Movie, String> {
    List<Movie> findByTitleContaining(String titleFragment);
    List<Movie> findAll();
}
