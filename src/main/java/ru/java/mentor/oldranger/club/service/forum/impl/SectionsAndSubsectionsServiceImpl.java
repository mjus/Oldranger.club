package ru.java.mentor.oldranger.club.service.forum.impl;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.java.mentor.oldranger.club.dao.ForumRepository.SectionRepository;
import ru.java.mentor.oldranger.club.dao.ForumRepository.SubsectionRepository;
import ru.java.mentor.oldranger.club.dto.SectionsAndSubsectionsDto;
import ru.java.mentor.oldranger.club.model.forum.Section;
import ru.java.mentor.oldranger.club.model.forum.Subsection;
import ru.java.mentor.oldranger.club.service.forum.SectionsAndSubsectionsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SectionsAndSubsectionsServiceImpl implements SectionsAndSubsectionsService {

    private static final Logger LOG = LoggerFactory.getLogger(SectionsAndSubsectionsServiceImpl.class);
    private SectionRepository sectionRepository;
    private SubsectionRepository subsectionRepository;

    @Override
    public List<SectionsAndSubsectionsDto> getAllSectionsAndSubsections() {
        LOG.debug("Getting list of section and subsection dtos");
        List<SectionsAndSubsectionsDto> list = null;
        try {
            List<Section> sections = sectionRepository.findAll(Sort.by("position").ascending());
            List<Subsection> subsections = subsectionRepository.findAll(Sort.by("section", "position").ascending());
            list = combineListOfSectionsAndSubsections(sections, subsections);
            LOG.debug("Returned list of {} dtos", list.size());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return list;
    }

    @Override
    public void swapSections(List<Long> sectionsId) {
        LOG.info("Swapping sections {}", sectionsId);
        try {
            for (int i = 0; i < sectionsId.size(); i++) {
                // (i + 1) - это новая позиция секции с id [i]
                Section section = sectionRepository.getOne(sectionsId.get(i));
                section.setPosition(i + 1);
                sectionRepository.save(section);
                LOG.info("Sections swapped");
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public void swapSubsectons(Map<Long, List<String>> sectionsAndSubsectionsIds) {
        LOG.info("Swapping subsections");
        try {
            // клиент посылает это: {"1":["3.7","1.1","1.2"],"3":["3.5","3.6"]}
            // {"section_id":["section_id.subsection_id","section_id.subsection_id" ......
            for (Map.Entry<Long, List<String>> pair : sectionsAndSubsectionsIds.entrySet()) {
                Section section = sectionRepository.getOne(pair.getKey());
                // пройти по всем подсекциям и изменить section
                List<String> listSubsections = pair.getValue();
                for (int i = 0; i < listSubsections.size(); i++) {
                    Long subsectionId = Long.valueOf(listSubsections.get(i).split("\\.")[1]);
                    Subsection subsection = subsectionRepository.getOne(subsectionId);
                    subsection.setSection(section);
                    subsection.setPosition(i + 1);
                    subsectionRepository.save(subsection);
                }
            }
            LOG.info("Subsections swapped");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private List<SectionsAndSubsectionsDto> combineListOfSectionsAndSubsections(List<Section> sections, List<Subsection> subsections) {
        LOG.debug("Combining list of sections and subsections");
        List<SectionsAndSubsectionsDto> dtos = new ArrayList<>();
        for (Section section : sections) {
            List<Subsection> subsectionsList = subsections.stream().filter(subsection -> subsection.getSection().equals(section)).collect(Collectors.toList());
            SectionsAndSubsectionsDto dto = new SectionsAndSubsectionsDto(section, subsectionsList);
            dtos.add(dto);
        }
        LOG.debug("List combined");
        return dtos;
    }
}
