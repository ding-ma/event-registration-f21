package ca.mcgill.ecse321.eventregistration.service;

import ca.mcgill.ecse321.eventregistration.model.Event;
import ca.mcgill.ecse321.eventregistration.model.Person;
import ca.mcgill.ecse321.eventregistration.model.Registration;
import ca.mcgill.ecse321.eventregistration.repository.EventRepository;
import ca.mcgill.ecse321.eventregistration.repository.PersonRepository;
import ca.mcgill.ecse321.eventregistration.repository.RegistrationRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

@Service
//@Component
public class EventRegistrationService {
    
    @Autowired
    private PersonRepository personRepository;
    
    @Autowired
    private EventRepository eventRepository;
    
    @Autowired
    private EventRegistrationValidator eventRegistrationValidator;
    
    
    @Autowired
    private RegistrationRepository registrationRepository;
    
    @Transactional
    public Person createPerson(String name) {
        if (StringUtils.isEmpty(name) || StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("Person name cannot be empty!");
        }
        if (personRepository.existsPersonByName(name)) {
            throw new IllegalArgumentException("Person name taken!");
        }
        
        Person person = new Person();
        person.setName(name);
        personRepository.save(person);
        return person;
    }
    
    @Transactional
    public Person getPerson(String name) {
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException("Person name cannot be empty!");
        }
        Person person = personRepository.findPersonByName(name);
        return person;
    }
    
    @Transactional
    public List<Person> getAllPersons() {
        return toList(personRepository.findAll());
    }
    
    
    @Transactional
    public Event getEvent(String name) {
        if (name == null || name.trim().length() == 0) {
            throw new IllegalArgumentException("Event name cannot be empty!");
        }
        Event event = eventRepository.findEventByName(name);
        return event;
    }
    
    
    @Transactional
    public Event createEvent(String name, Date date, Time startTime, Time endTime) {
        // Input validation
        eventRegistrationValidator.validateCreateEvent(name, date, startTime, endTime);
        
        Event event = new Event();
        event.setName(name);
        event.setDate(date);
        event.setStartTime(startTime);
        event.setEndTime(endTime);
        eventRepository.save(event);
        return event;
    }
    
    @Transactional
    public Registration register(Person person, Event event) {
        String error = "";
        if (person == null) {
            error = error + "Person needs to be selected for registration! ";
        } else if (!personRepository.existsPersonByName(person.getName())) {
            error = error + "Person does not exist! ";
        }
        if (event == null) {
            error = error + "Event needs to be selected for registration!";
        } else if (!eventRepository.existsEventByName(event.getName())) {
            error = error + "Event does not exist!";
        }
        if (registrationRepository.existsByPersonAndEvent(person, event)) {
            error = error + "Person is already registered to this event!";
        }
        error = error.trim();
        
        if (error.length() > 0) {
            throw new IllegalArgumentException(error);
        }
        
        Registration registration = new Registration();
        registration.setId(person.getName().hashCode() * event.getName().hashCode());
        registration.setPerson(person);
        registration.setEvent(event);
        
        registrationRepository.save(registration);
        
        return registration;
    }
    
    @Transactional
    public List<Event> getEventsAttendedByPerson(Person person) {
        if (person == null) {
            throw new IllegalArgumentException("Person cannot be null!");
        }
        List<Event> eventsAttendedByPerson = new ArrayList<>();
        for (Registration r : registrationRepository.findByPerson(person)) {
            eventsAttendedByPerson.add(r.getEvent());
        }
        return eventsAttendedByPerson;
    }
    
    @Transactional
    public Iterable<Event> getAllEvents() {
        return eventRepository.findAll();
    }
    
    
    private <T> List<T> toList(Iterable<T> iterable) {
        List<T> resultList = new ArrayList<T>();
        for (T t : iterable) {
            resultList.add(t);
        }
        return resultList;
    }
    
}
