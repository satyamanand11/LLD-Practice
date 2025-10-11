package com.lld.elevator.factory;


import com.lld.elevator.startegy.BroadcastStrategy;
import com.lld.elevator.startegy.ExclusiveEtaStrategy;
import com.lld.elevator.startegy.SchedulerStrategy;

public class StrategyFactory {
    public static SchedulerStrategy broadcast() { return new BroadcastStrategy(); }
    public static SchedulerStrategy exclusiveEta() { return new ExclusiveEtaStrategy(); }
}
