package rest;

import org.springframework.context.ApplicationEvent;

public class TripChangeEvent extends ApplicationEvent {
    public TripChangeEvent(Object source) {
        super(source);
    }
}
