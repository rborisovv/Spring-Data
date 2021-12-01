package com.example.football.service.impl;

import com.example.common.Paths;
import com.example.football.models.dto.statDto.StatSeedRootDto;
import com.example.football.models.entity.Stat;
import com.example.football.repository.StatRepository;
import com.example.football.service.StatService;
import com.example.football.util.FileUtil;
import com.example.football.util.ValidationUtil;
import com.example.football.util.XmlParser;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
public class StatServiceImpl implements StatService {
    private static final String STATS_FILE_NAME = "stats.xml";

    private final StatRepository statRepository;
    private final FileUtil fileUtil;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;
    private final XmlParser xmlParser;

    public StatServiceImpl(StatRepository statRepository, FileUtil fileUtil,
                           ValidationUtil validationUtil, ModelMapper modelMapper, XmlParser xmlParser) {
        this.statRepository = statRepository;
        this.fileUtil = fileUtil;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
        this.xmlParser = xmlParser;
    }

    @Override
    public boolean areImported() {
        return statRepository.count() > 0;
    }

    @Override
    public String readStatsFileContent() throws IOException {
        return fileUtil.readFile(Paths.XML_FILE_CONTENT_NAME + STATS_FILE_NAME);
    }

    @Override
    public String importStats() throws JAXBException, FileNotFoundException {
        StatSeedRootDto statSeedRootDto = xmlParser.readFromFile(Paths.XML_FILE_CONTENT_NAME + STATS_FILE_NAME, StatSeedRootDto.class);

        StringBuilder result = new StringBuilder();
        statSeedRootDto
                .getStats()
                .stream()
                .filter(statSeedDto -> {
                    boolean isValid = validationUtil.validate(statSeedDto)
                            && !statRepository.existsByPassingAndShootingAndEndurance(
                            statSeedDto.getPassing(), statSeedDto.getShooting(), statSeedDto.getEndurance()
                    );

                    result.append(isValid ? String.format("Successfully imported Stat %.2f - %.2f - %.2f",
                            statSeedDto.getPassing(), statSeedDto.getShooting(), statSeedDto.getEndurance())
                            : "Invalid Stat").append(System.lineSeparator());

                    return isValid;
                })
                .map(statSeedDto -> modelMapper.map(statSeedDto, Stat.class))
                .forEach(statRepository::save);

        return result.toString().trim();
    }

    @Override
    public Stat findStatById(Integer id) {
        return statRepository.findStatById(id).orElse(null);
    }
}