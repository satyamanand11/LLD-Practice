package com.lld.amazon.locker.dto;

import com.lld.amazon.locker.model.Dimensions;

public record PackageRequest(String packageId, Dimensions dimensions) {}
