package com.fijimf.deepfij.model.dto;

import com.fijimf.deepfij.model.schedule.Team;

public record TeamDTO(Long id, String name, String longName, String nickname, String logoUrl, String primaryColor,
                      String secondaryColor, String slug) {
    public static TeamDTO fromTeam(Team team) {
        return new TeamDTO(
                team.getId(),
                team.getName(),
                team.getLongName(),
                team.getNickname(),
                team.getLogoUrl(),
                team.getPrimaryColor(),
                team.getSecondaryColor(),
                team.getSlug());
    }
}
