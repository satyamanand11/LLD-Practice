package com.lld.bms.repo;

import com.lld.bms.domain.Screen;

import java.util.List;

public interface ScreenRepository extends Repository<Screen, String> {
    List<Screen> findByVenueId(String venueId);
}
