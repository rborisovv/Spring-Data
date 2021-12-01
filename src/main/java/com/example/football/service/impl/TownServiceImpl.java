package com.example.football.service.impl;

import com.example.common.Paths;
import com.example.football.models.dto.townDto.TownSeedDto;
import com.example.football.models.entity.Town;
import com.example.football.repository.TownRepository;
import com.example.football.service.TownService;
import com.example.football.util.FileUtil;
import com.example.football.util.ValidationUtil;
import com.google.gson.Gson;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;


@Service
public class TownServiceImpl implements TownService {
    private static final String TOWN_FILE_NAME = "towns.json";

    private final TownRepository townRepository;
    private final FileUtil fileUtil;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;
    private final Gson gson;

    public TownServiceImpl(TownRepository townRepository, FileUtil fileUtil, ValidationUtil validationUtil,
                           ModelMapper modelMapper, Gson gson) {
        this.townRepository = townRepository;
        this.fileUtil = fileUtil;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
        this.gson = gson;
    }

    @Override
    public boolean areImported() {
        return townRepository.count() > 0;
    }

    @Override
    public String readTownsFileContent() throws IOException {
        return fileUtil.readFile(Paths.JSON_FILE_CONTENT_NAME + TOWN_FILE_NAME);
    }

    @Override
    public String importTowns() throws IOException {
        TownSeedDto[] townSeedDtos = gson
                .fromJson(readTownsFileContent(), TownSeedDto[].class);

        StringBuilder result = new StringBuilder();
        Arrays.stream(townSeedDtos)
                .filter(townSeedDto -> {
                    boolean isValid = validationUtil.validate(townSeedDto)
                            && !townRepository.existsByName(townSeedDto.getName());

                    result.append(isValid ? String.format("Successfully imported Town %s - %d",
                            townSeedDto.getName(), townSeedDto.getPopulation())
                            : "Invalid Town").append(System.lineSeparator());

                    return isValid;

                })
                .map(townSeedDto -> modelMapper.map(townSeedDto, Town.class))
                .forEach(townRepository::save);

        return result.toString().trim();
    }

    @Override
    public Town findTownByName(String name) {
        return townRepository.findTownByName(name).orElse(null);
    }
}