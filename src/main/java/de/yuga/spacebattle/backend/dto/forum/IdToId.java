package de.yuga.spacebattle.backend.dto.forum;

public class IdToId {

    private final int idSelector;

    private final int idPayload;

    public IdToId(final int idSelector, final int idPayload) {
        this.idSelector = idSelector;
        this.idPayload = idPayload;
    }

    public int getIdSelector() {
        return idSelector;
    }

    public int getIdPayload() {
        return idPayload;
    }
}
