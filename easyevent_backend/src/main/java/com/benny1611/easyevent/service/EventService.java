package com.benny1611.easyevent.service;

import com.benny1611.easyevent.dao.EventRepository;
import com.benny1611.easyevent.dao.UserRepository;
import com.benny1611.easyevent.dto.CreateEventRequest;
import com.benny1611.easyevent.dto.CreateEventResponse;
import com.benny1611.easyevent.entity.Event;
import com.benny1611.easyevent.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Service
public class EventService {
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Autowired
    public EventService(EventRepository eventRepository, UserRepository userRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
    }

    public CreateEventResponse createEvent(CreateEventRequest request, String username) {
        String title = request.getTitle();
        Integer numberOfSeats = request.getNumberOfSeats();
        Event event = new Event();
        LocalDate localDate;

        try {
            localDate = LocalDate.parse(request.getDate());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Date string must be a valid date: YYYY-MM-dd");
        }

        Optional<User> userOptional = userRepository.findByEmail(username);
        User user;
        if (userOptional.isPresent()) {
            user = userOptional.get();
        } else {
            throw new IllegalStateException("Could not find user: " + username);
        }

        event.setTitle(title);
        event.setNumberOfSeats(numberOfSeats);
        event.setDate(localDate.toString());
        event.setCreatedBy(user);

        event = eventRepository.save(event);

        CreateEventResponse response = new CreateEventResponse();
        response.setDate(localDate.toString());
        response.setNumberOfSeats(numberOfSeats);
        response.setTitle(title);
        response.setId(event.getId());


        return response;
    }
}
