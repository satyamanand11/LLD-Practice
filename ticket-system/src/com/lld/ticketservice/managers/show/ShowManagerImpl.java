package com.lld.ticketservice.managers.show;

import com.lld.ticketservice.domain.show.Show;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ShowManagerImpl implements ShowManager {
    private final Map<Integer, Show> byId = new ConcurrentHashMap<>();

    public void addShow(Show s) {
        byId.put(s.getShowId(), s);
    }

    public void removeShow(int showId) {
        byId.remove(showId);
    }

    public Show getShow(int id) {
        return byId.get(id);
    }

    public List<Show> getShowsByEvent(int eventId) {
        return byId.values().stream().filter(s -> s.getEventId() == eventId).collect(Collectors.toList());
    }

    public Map<Integer, Show> getShowMap() {
        return byId;
    }
}
