package com.github.ylegat.domain;

import com.github.ylegat.domain.event.Event;

import java.util.List;

public interface AccountRepository {

    boolean storeEvents(List<Event> events);

}
