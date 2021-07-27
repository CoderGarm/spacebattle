package de.yuga.spacebattle.backend.enums;

/**
 * The way of damage projection.
 */
public enum EWarheadType {

    /**
     * If the damage will be dealt by nuke-powered laser beams.
     */
    LASER,

    /**
     * If the damage will be dealt by an explosion.
     */
    EXPLOSION,

    /**
     * If the damage will be dealt only by a direct hit.
     */
    KINETIC,

    /**
     * If the warhead is empty like in a counter missile.
     */
    COUNTER_MISSILE;

}
