package com.fijimf.deepfij.model.dto;

import com.fijimf.deepfij.model.schedule.Conference;
import com.fijimf.deepfij.model.scraping.standings.ConferenceStanding;

import java.util.List;

public record ConferenceDTO(Long id, String name, String shortName, String logoUrl,
                            List<TeamPage.StandingDTO> standings) {
    public static ConferenceDTO fromConference(Conference conference, List<TeamPage.StandingDTO> standings) {
        return new ConferenceDTO(conference.getId(), conference.getName(), conference.getShortName(), conference.getLogoUrl(), standings);
    }
}
