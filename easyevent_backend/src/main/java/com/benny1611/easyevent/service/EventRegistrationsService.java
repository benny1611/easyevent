package com.benny1611.easyevent.service;

import com.benny1611.easyevent.dao.EventRegistrationRepository;
import com.benny1611.easyevent.dao.EventRepository;
import com.benny1611.easyevent.dao.GuestRepository;
import com.benny1611.easyevent.dao.UserRepository;
import com.benny1611.easyevent.dto.EventRegistrationRequest;
import com.benny1611.easyevent.dto.GetEventRegistrationResponse;
import com.benny1611.easyevent.entity.Event;
import com.benny1611.easyevent.entity.EventRegistration;
import com.benny1611.easyevent.entity.Guest;
import com.benny1611.easyevent.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EventRegistrationsService {

    private final EventRegistrationRepository eventRegistrationRepository;
    private final UserRepository userRepository;
    private final GuestRepository guestRepository;
    private final EventRepository eventRepository;

    @Autowired
    public EventRegistrationsService(EventRegistrationRepository eventRegistrationRepository, UserRepository userRepository, GuestRepository guestRepository, EventRepository eventRepository) {
        this.eventRegistrationRepository = eventRegistrationRepository;
        this.userRepository = userRepository;
        this.guestRepository = guestRepository;
        this.eventRepository = eventRepository;
    }

    public Page<GetEventRegistrationResponse> getRegistrationsForEvent(Long eventId, Pageable pageable) {
        return eventRegistrationRepository.findByEventId(eventId, pageable).map(GetEventRegistrationResponse::fromEntity);
    }

    public void registerToAnEvent(String email, EventRegistrationRequest request) {
        Event event = eventRepository.findById(request.getEventId()).orElseThrow(() -> new IllegalArgumentException("Event " + request.getEventId() + " not found"));

        // Sanity check: First check if the user is a guest or not
        User user = null;
        if (email != null && !"anonymousUser".equals(email)) {
            user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Could not find user: " + email));
        } else {
            Optional<User> userSanityCheckOptional = userRepository.findByEmail(request.getEmail());
            if (userSanityCheckOptional.isPresent()) {
                // User is not logged in, and is trying to register a created account! Should not be possible.
                throw new IllegalStateException("There's already an account associated with the given email, please log in and try again");
            }
        }

        // Check if there's already a registration made for this event for this guest
        Guest guest = null;
        if (user == null) {
            Optional<Guest> guestOptional = guestRepository.findByEmail(request.getEmail());
            if (guestOptional.isPresent()) {
                guest = guestOptional.get();
                Optional<EventRegistration> guestRegistrationOptional = eventRegistrationRepository.findByEventIdAndGuestId(event.getId(), guest.getId());
                if (guestRegistrationOptional.isPresent()) {
                    throw new IllegalStateException("User already registered for this event");
                }
            }
        } else {
            Optional<EventRegistration> userRegistrationOptional = eventRegistrationRepository.findByEventIdAndUserId(event.getId(), user.getId());
            if (userRegistrationOptional.isPresent()) {
                throw new IllegalStateException("User already registered for this event");
            }
        }

        EventRegistration registration = new EventRegistration();
        registration.setEvent(event);

        if (user != null) {
            registration.setUser(user);
            registration.setGuest(null);
        } else {
            if (guest == null) {
                guest = new Guest();
            }
            guest.setName(request.getName());
            guest.setEmail(request.getEmail());
            guestRepository.save(guest);
            registration.setGuest(guest);
            registration.setUser(null);
        }

        eventRegistrationRepository.save(registration);
    }
}
