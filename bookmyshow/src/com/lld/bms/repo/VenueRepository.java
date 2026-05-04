package com.lld.bms.repo;

import com.lld.bms.domain.Venue;

import java.util.List;

public interface VenueRepository extends Repository<Venue, String> {
    List<Venue> findByCityId(String cityId);
}
