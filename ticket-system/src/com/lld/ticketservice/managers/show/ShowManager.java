package com.lld.ticketservice.managers.show;

import com.lld.ticketservice.domain.show.Show;

import java.util.List;
import java.util.Map;

public interface ShowManager {
    void addShow(Show s);

    void removeShow(int showId);

    Show getShow(int showId);

    List<Show> getShowsByEvent(int eventId);

    Map<Integer, Show> getShowMap();
}
