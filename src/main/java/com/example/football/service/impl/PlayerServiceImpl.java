package com.example.football.service.impl;

import com.example.common.Paths;
import com.example.football.models.dto.playerDto.PlayerSeedRootDto;
import com.example.football.models.entity.Player;
import com.example.football.models.entity.Stat;
import com.example.football.models.entity.Team;
import com.example.football.models.entity.Town;
import com.example.football.repository.PlayerRepository;
import com.example.football.service.PlayerService;
import com.example.football.service.StatService;
import com.example.football.service.TeamService;
import com.example.football.service.TownService;
import com.example.football.util.FileUtil;
import com.example.football.util.ValidationUtil;
import com.example.football.util.XmlParser;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PlayerServiceImpl implements PlayerService {
    private static final String PLAYERS_FILE_NAME = "players.xml";

    private final PlayerRepository playerRepository;
    private final FileUtil fileUtil;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;
    private final XmlParser xmlParser;
    private final TownService townService;
    private final TeamService teamService;
    private final StatService statService;

    public PlayerServiceImpl(PlayerRepository playerRepository, FileUtil fileUtil,
                             ValidationUtil validationUtil, ModelMapper modelMapper, XmlParser xmlParser,
                             TownService townService, TeamService teamService, StatService statService) {
        this.playerRepository = playerRepository;
        this.fileUtil = fileUtil;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
        this.xmlParser = xmlParser;
        this.townService = townService;
        this.teamService = teamService;
        this.statService = statService;
    }

    @Override
    public boolean areImported() {
        return playerRepository.count() > 0;
    }

    @Override
    public String readPlayersFileContent() throws IOException {
        return fileUtil.readFile(Paths.XML_FILE_CONTENT_NAME + PLAYERS_FILE_NAME);
    }

    @Override
    public String importPlayers() throws JAXBException, FileNotFoundException {
        PlayerSeedRootDto playerSeedRootDto = xmlParser
                .readFromFile(Paths.XML_FILE_CONTENT_NAME + PLAYERS_FILE_NAME, PlayerSeedRootDto.class);

        StringBuilder result = new StringBuilder();
        playerSeedRootDto
                .getPlayers()
                .stream()
                .filter(playerSeedDto -> {
                    boolean isValid = validationUtil.validate(playerSeedDto)
                            && !playerRepository.existsByEmail(playerSeedDto.getEmail());

                    result.append(isValid ? String.format("Successfully imported Player %s %s - %s",
                                    playerSeedDto.getFirstName(), playerSeedDto.getLastName(),
                                    playerSeedDto.getPosition().name()) : "Invalid Player")
                            .append(System.lineSeparator());

                    return isValid;
                })
                .map(playerSeedDto -> {
                    Player player = modelMapper.map(playerSeedDto, Player.class);
                    Town town = townService.findTownByName(playerSeedDto.getTown().getName());
                    Team team = teamService.findTeamByName(playerSeedDto.getTeam().getName());
                    Stat stat = statService.findStatById(playerSeedDto.getStat().getId());

                    player.setTown(town);
                    player.setTeam(team);
                    player.setStat(stat);

                    return player;
                })
                .forEach(playerRepository::save);
        return result.toString().trim();

    }

    @Override
    public String exportBestPlayers() {
        List<Player> bestPlayers = playerRepository.findBestPlayers(
                LocalDate.parse("01-01-1995", DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                LocalDate.parse("01-01-2003", DateTimeFormatter.ofPattern("dd-MM-yyyy")));

        StringBuilder result = new StringBuilder();
        bestPlayers
                .forEach(player -> result.append(
                                String.format("Player - %s %s%n" +
                                                "\tPosition - %s%n" +
                                                "\tTeam - %s%n" +
                                                "\tStadium - %s",
                                        player.getFirstName(),
                                        player.getLastName(),
                                        player.getPosition().name(),
                                        player.getTeam().getName(),
                                        player.getTeam().getStadiumName()))
                        .append(System.lineSeparator()));
        return result.toString().trim();
    }
}