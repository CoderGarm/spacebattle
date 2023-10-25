package de.yuga.spacebattle.backend.dto.research;

public class EmpireResearchCapability {

    final long empireWideResearchPoints;
    final long empireWideResearchPointsLeftOver;

    public EmpireResearchCapability(final long empireWideResearchPoints, final long empireWideResearchPointsLeftOver) {
        this.empireWideResearchPoints = empireWideResearchPoints;
        this.empireWideResearchPointsLeftOver = empireWideResearchPointsLeftOver;
    }

    public long getEmpireWideResearchPoints() {
        return empireWideResearchPoints;
    }

    public long getEmpireWideResearchPointsLeftOver() {
        return empireWideResearchPointsLeftOver;
    }
}
