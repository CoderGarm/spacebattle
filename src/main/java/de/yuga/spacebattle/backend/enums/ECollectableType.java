package de.yuga.spacebattle.backend.enums;

public enum ECollectableType {

    /**
     * Everything which is flagged by this means a collectable production.
     */
    COLLECTABLE,

    /**
     * Everything which is flagged by this is decayable and will not be collected and stacked tick by tick.
     */
    FORFEITABLE,

    /**
     * Not very surprisingly this is a mathematical concept of rising until the limit is reached.<br>
     * The closer the limit is, the less will be added.
     */
    VIABLE,
    ;

}
