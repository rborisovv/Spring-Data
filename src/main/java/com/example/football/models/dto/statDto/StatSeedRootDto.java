package com.example.football.models.dto.statDto;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "stats")
@XmlAccessorType(XmlAccessType.FIELD)
public class StatSeedRootDto {
    @XmlElement(name = "stat")
    private List<StatSeedDto> stats;

    public List<StatSeedDto> getStats() {
        return stats;
    }

    public void setStats(List<StatSeedDto> stats) {
        this.stats = stats;
    }
}