package ru.java.mentor.oldranger.club.service.forum.impl;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.java.mentor.oldranger.club.dao.ForumRepository.TopicRepository;
import ru.java.mentor.oldranger.club.dto.TopicAndNewMessagesCountDto;
import ru.java.mentor.oldranger.club.model.forum.Subsection;
import ru.java.mentor.oldranger.club.model.forum.Topic;
import ru.java.mentor.oldranger.club.model.forum.TopicVisitAndSubscription;
import ru.java.mentor.oldranger.club.model.user.User;
import ru.java.mentor.oldranger.club.model.user.UserStatistic;
import ru.java.mentor.oldranger.club.projection.IdAndNumberProjection;
import ru.java.mentor.oldranger.club.service.forum.TopicService;
import ru.java.mentor.oldranger.club.service.forum.TopicVisitAndSubscriptionService;
import ru.java.mentor.oldranger.club.service.user.UserStatisticService;
import ru.java.mentor.oldranger.club.service.utils.SecurityUtilsService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TopicServiceImpl implements TopicService {

    private static final Logger LOG = LoggerFactory.getLogger(TopicServiceImpl.class);
    private TopicRepository topicRepository;
    private UserStatisticService userStatisticService;
    private SecurityUtilsService securityUtilsService;
    private TopicVisitAndSubscriptionService topicVisitAndSubscriptionService;

    @Override
    public void createTopic(Topic topic) {
        LOG.info("Saving topic {}", topic);
        try {
            UserStatistic userStatistic = userStatisticService.getUserStaticByUser(topic.getTopicStarter());
            long topicCount = userStatistic.getTopicStartCount();
            userStatistic.setTopicStartCount(++topicCount);
            userStatisticService.saveUserStatic(userStatistic);
            topicRepository.save(topic);
            LOG.info("Topic saved");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public void editTopicByName(Topic topic) {
        LOG.info("Saving topic {}", topic);
        try {
            topicRepository.save(topic);
            LOG.info("Topic saved");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public void deleteTopicById(Long id) {
        LOG.info("Deleting topic with id = {}", id);
        try {
            topicRepository.deleteById(id);
            LOG.info("Topic deleted");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }

    @Override
    public Topic findById(Long id) {
        LOG.debug("Getting topic by id = {}", id);
        return topicRepository.findById(id).orElse(null);
    }

    @Override
    public List<Topic> findAll() {
        LOG.debug("Getting all topics");
        List<Topic> topics = null;
        try {
            topics = topicRepository.findAll();
            LOG.debug("Returned list of {} topics", topics.size());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return topics;
    }

    @Override
    public List<Topic> getActualTopicsLimitAnyBySection(Integer limitTopicsBySection) {
        LOG.debug("Getting actual topics with limit = {}", limitTopicsBySection);
        List<Topic> topics = null;
        try {
            topics = topicRepository.getActualTopicsLimitAnyBySection(limitTopicsBySection);
            LOG.debug("Returned list of {} topics", topics.size());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return topics;
    }

    @Override
    public List<Topic> getActualTopicsLimitAnyBySectionForAnon(int expecting_topics_limit_less_or_equals) {
        LOG.debug("Getting actual topics for anon with limit = {}", expecting_topics_limit_less_or_equals);
        List<Topic> topics = null;
        try {
            topics = topicRepository.getActualTopicsLimitAnyBySectionForAnon(expecting_topics_limit_less_or_equals);
            LOG.debug("Returned list of {} topics", topics.size());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return topics;
    }

    @Override
    public List<Topic> getActualTopicsLimit10BySection() {
        LOG.debug("Getting actual topics with limit = 10");
        List<Topic> topics = null;
        try {
            topics = topicRepository.getActualTopicsLimitAnyBySection(10);
            LOG.debug("Returned list of {} topics", topics.size());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return topics;
    }

    @Override
    public List<Topic> getActualTopicsLimit10BySectionForAnon() {
        LOG.debug("Getting actual topics for anon with limit = 10");
        List<Topic> topics = null;
        try {
            topics = topicRepository.getActualTopicsLimitAnyBySectionForAnon(10);
            LOG.debug("Returned list of {} topics", topics.size());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return topics;
    }

    @Override
    public Page<Topic> getPageableBySubsection(Subsection subsection, Pageable pageable) {
        LOG.debug("Getting page {} of topics for subsection with id = {}", pageable.getPageNumber(), subsection.getId());
        Page<Topic> page = null;
        try {
            if (securityUtilsService.isLoggedUserIsUser()) {
                page = getPageableBySubsectionForUser(securityUtilsService.getLoggedUser(), subsection, pageable);
            } else {
                page = getPageableBySubsectionForAnon(subsection, pageable);
            }
            LOG.debug("Page returned");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return page;
    }

    public Page<Topic> getPageableBySubsectionForAnon(Subsection subsection, Pageable pageable) {
        LOG.debug("Getting page {} of topics for subsection for anon with id = {}", pageable.getPageNumber(), subsection.getId());
        Page<Topic> page = null;
        try {
            page = topicRepository.findBySubsectionAndIsHideToAnonIsFalseOrderByLastMessageTimeDesc(subsection, pageable);
            LOG.debug("Page returned");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return page;
    }

    @Override
    public Page<Topic> findAllTopicsStartedByUser(User user, Pageable pageable) {
        LOG.debug("Getting page {} of topics started by user with id = {}", pageable.getPageNumber(), user.getId());
        Page<Topic> page = null;
        try {
            page = topicRepository.findAllBytopicStarter(user, pageable);
            LOG.debug("Page returned");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return page;
    }

    @Override
    public Page<Topic> getPageableBySubsectionForUser(User user, Subsection subsection, Pageable pageable) {
        LOG.debug("Getting page {} of topics by subsection id = {} and user id = {}", pageable.getPageNumber(), subsection.getId(), user.getId());
        PageImpl<Topic> page = null;
        try {
            int pageNumber = pageable.getPageNumber();
            int pageSize = pageable.getPageSize();
            int offset = pageNumber * pageSize;
            page = new PageImpl<>(
                    topicRepository.getSliceListBySubsectionForUserOrderByLastMessageTimeDescAndSubscriptionsWithNewMessagesFirst(user.getId(), subsection.getId(), offset, pageSize),
                    pageable,
                    topicRepository.countForGetSliceListBySubsectionForUserOrderByLastMessageTimeDescAndSubscriptionsWithNewMessagesFirst(user.getId(), subsection.getId())
            );
            LOG.debug("Page returned");
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return page;
    }

    @Override
    public List<IdAndNumberProjection> getMessagesCountForTopics(List<Topic> topics) {
        LOG.debug("Getting messages count for topics");
        List<IdAndNumberProjection> projections = null;
        try {
            List<Long> list = topics.stream().map(Topic::getId).collect(Collectors.toList());
            projections = topicRepository.getPairsTopicIdAndTotalMessagesCount(list);
            LOG.debug("Returned list of {} projections", projections.size());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return projections;
    }

    @Override
    public List<IdAndNumberProjection> getNewMessagesCountForTopicsAndUser(List<Topic> topics, User user) {
        LOG.debug("Getting new messages count for user with id = {}", user.getId());
        List<IdAndNumberProjection> projections = null;
        try {
            List<Long> list = topics.stream().map(Topic::getId).collect(Collectors.toList());
            projections = topicRepository.getPairsTopicIdAndNewMessagesCountForUserId(list, user.getId());
            LOG.debug("Returned list of {} projections", projections.size());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return projections;
    }

    @Override
    public List<TopicAndNewMessagesCountDto> getTopicsDto(List<Topic> topics) {
        LOG.debug("Getting list of topic dtos");
        boolean logged = false;
        List<IdAndNumberProjection> newMessagesCountForTopicsAndUser = null;
        List<TopicVisitAndSubscription> topicVisitAndSubscriptionForUser = null;
        List<TopicAndNewMessagesCountDto> dtos = new ArrayList<>();
        try {
            if (securityUtilsService.isLoggedUserIsUser()) {
                logged = true;
                User loggedUser = securityUtilsService.getLoggedUser();
                newMessagesCountForTopicsAndUser = getNewMessagesCountForTopicsAndUser(topics, loggedUser);
                topicVisitAndSubscriptionForUser = topicVisitAndSubscriptionService.getTopicVisitAndSubscriptionForUser(loggedUser);
            }
            List<IdAndNumberProjection> messagesCountForTopics = getMessagesCountForTopics(topics);
            for (Topic topic : topics) {
                TopicAndNewMessagesCountDto dto = new TopicAndNewMessagesCountDto();
                dto.setTopic(topic);
                Optional<IdAndNumberProjection> messagesCountForTopic = messagesCountForTopics.stream().filter(t -> t.getId() == topic.getId()).findAny();
                if (messagesCountForTopic.isPresent()) {
                    dto.setTotalMessages(messagesCountForTopic.get().getNumber());
                } else {
                    dto.setTotalMessages(0);
                }
                if (logged) {
                    boolean isSubscribed = topicVisitAndSubscriptionForUser.stream().filter(t -> t.getTopic().getId().equals(topic.getId())).anyMatch(TopicVisitAndSubscription::isSubscribed);
                    dto.setIsSubscribed(isSubscribed);
                    Optional<IdAndNumberProjection> newMessages = newMessagesCountForTopicsAndUser.stream().filter(t -> t.getId() == topic.getId()).findAny();
                    if (newMessages.isPresent()) {
                        dto.setHasNewMessages(true);
                        dto.setNewMessagesCount(newMessages.get().getNumber());
                    } else {
                        dto.setHasNewMessages(false);
                        dto.setNewMessagesCount(0L);
                    }
                } else {
                    dto.setIsSubscribed(null);
                    dto.setHasNewMessages(null);
                    dto.setNewMessagesCount(null);
                }
                dtos.add(dto);
            }
            LOG.debug("Returned list of {} dtos", dtos.size());
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
        return dtos;
    }
}
