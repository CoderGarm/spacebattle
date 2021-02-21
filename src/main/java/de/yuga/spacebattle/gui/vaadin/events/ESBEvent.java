package de.yuga.spacebattle.gui.vaadin.events;

public enum ESBEvent {

    TICK, // if a tick was processed
    LOGIN, // user has logged in
    LOGOUT, // user has logged out
    USERCOMPLETE, // creating a new user - all data collected
    USERINCOMPLETE, // creating a new user - NOT all data collected
    CONSTRUCT_BUILDING, // when a constuction job should start
    CONSTRUCTION_JOB_STARTED, // when a constuction job has started

}
