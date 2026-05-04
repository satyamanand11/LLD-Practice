package com.lld.bms.repo;

import com.lld.bms.domain.Show;

import java.util.List;

public interface ShowRepository extends Repository<Show, String> {
    List<Show> findByScreenId(String screenId);
    List<Show> findByMovieId(String movieId);
}
