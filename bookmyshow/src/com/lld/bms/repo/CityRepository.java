package com.lld.bms.repo;

import com.lld.bms.domain.City;

import java.util.List;

public interface CityRepository extends Repository<City, String> {
    List<City> findAll();
}
