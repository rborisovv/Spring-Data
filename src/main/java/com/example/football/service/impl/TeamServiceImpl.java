package com.example.football.service.impl;

import com.example.common.Paths;
import com.example.football.models.dto.teamDto.TeamSeedDto;
import com.example.football.models.entity.Team;
import com.example.football.models.entity.Town;
import com.example.football.repository.TeamRepository;
import com.example.football.service.TeamService;
import com.example.football.service.TownService;
import com.example.football.util.FileUtil;
import com.example.football.util.ValidationUtil;
import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;

@Service
public class TeamServiceImpl implements TeamService {
    private static final String TEAM_FILE_NAME = "teams.json";

    private final TeamRepository teamRepository;
    private final FileUtil fileUtil;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;
    private final Gson gson;
    private final TownService townService;

    public TeamServiceImpl(TeamRepository teamRepository, FileUtil fileUtil,
                           ValidationUtil validationUtil, ModelMapper modelMapper, Gson gson, TownService townService) {
        this.teamRepository = teamRepository;
        this.fileUtil = fileUtil;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
        this.gson = gson;
        this.townService = townService;
    }


    @Override
    public boolean areImported() {
        return teamRepository.count() > 0;
    }

    @Override
    public String readTeamsFileContent() throws IOException {
        return fileUtil.readFile(Paths.JSON_FILE_CONTENT_NAME + TEAM_FILE_NAME);
    }

    @Override
    public String importTeams() throws IOException {
        TeamSeedDto[] teamSeedDtos = gson.fromJson(readTeamsFileContent(), TeamSeedDto[].class);

        StringBuilder result = new StringBuilder();
        Arrays.stream(teamSeedDtos)
                .filter(teamSeedDto -> {
                    boolean isValid = validationUtil.validate(teamSeedDto)
                            && !teamRepository.existsByName(teamSeedDto.getName());

                    result.append(isValid ? String.format("Successfully imported Team %s - %d",
                            teamSeedDto.getName(), teamSeedDto.getFanBase())
                            : "Invalid Team").append(System.lineSeparator());

                    return isValid;
                })
                .map(teamSeedDto -> {
                    Team team = modelMapper.map(teamSeedDto, Team.class);
                    Town town = townService.findTownByName(teamSeedDto.getTownName());
                    team.setTown(town);

                    return team;
                })
                .forEach(teamRepository::save);

        return result.toString().trim();
    }

    @Override
    public Team findTeamByName(String name) {
        return teamRepository.findTeamByName(name).orElse(null);
    }
}