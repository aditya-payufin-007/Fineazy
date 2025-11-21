package com.hackathon.lending.bot.service;

import com.hackathon.lending.bot.constants.Constants;
import com.hackathon.lending.bot.dto.MessageProcessorRequestDTO;
import com.hackathon.lending.bot.entity.UserDetails;
import com.hackathon.lending.bot.repository.UserDetailsRepository;
import com.hackathon.lending.bot.service.processor.StageMessageProcessor;
import com.hackathon.lending.bot.utility.ApplicationStages;
import com.hackathon.lending.bot.utility.ApplicationStages.StageType;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MessageProcessorService {

    private static final Logger logger = LoggerFactory.getLogger(MessageProcessorService.class);

    private final Map<StageType, StageMessageProcessor> stageProcessorRegistry;
    private final StageMessageProcessor defaultStageProcessor;
    private final UserDetailsRepository userDetailsRepository;

    public MessageProcessorService(List<StageMessageProcessor> stageMessageProcessors,
            UserDetailsRepository userDetailsRepository) {
        Map<StageType, StageMessageProcessor> registry = new EnumMap<>(StageType.class);
        StageMessageProcessor fallback = null;
        this.userDetailsRepository = userDetailsRepository;

        for (StageMessageProcessor processor : stageMessageProcessors) {
            StageType stageType = processor.getStageType();
            if (StageType.UNKNOWN.equals(stageType)) {
                fallback = processor;
                continue;
            }
            registry.put(stageType, processor);
        }

        this.stageProcessorRegistry = Collections.unmodifiableMap(registry);
        this.defaultStageProcessor = fallback != null ? fallback : new StageMessageProcessor() {
            @Override
            public StageType getStageType() {
                return StageType.UNKNOWN;
            }

            @Override
            public String process(MessageProcessorRequestDTO request, ApplicationStages stage) {
                return Constants.SOMEWTHING_WENT_WRONG_MESSAGE_DEFAULT;
            }
        };
    }

    public String processIncomingMessageAndGenerateResponse(MessageProcessorRequestDTO request) {
        try {
            return generateResponseBasedOnIncomingMessage(request);
        } catch (Exception e) {
            logger.error("Exception occurred in processIncomingMessageAndGenerateResponse : ", e);
            return Constants.SOMEWTHING_WENT_WRONG_MESSAGE_DEFAULT;
        }
    }

    private String generateResponseBasedOnIncomingMessage(MessageProcessorRequestDTO request) {
        ApplicationStages currentStage = resolveCurrentStage(request);
        ApplicationStages nextStage = ApplicationStages.nextStage(currentStage);

//        if(currentStage==ApplicationStages.ONBOARDING_IN_PROGRESS){
//            nextStage = ApplicationStages.ONBOARDING_IN_PROGRESS;
//        }

        StageMessageProcessor processor = stageProcessorRegistry.getOrDefault(currentStage.getStageType(),
                defaultStageProcessor);

        String response = processor.process(request, nextStage);
        if (!StringUtils.hasText(response)) {
            response = defaultStageProcessor.process(request, nextStage);
        }
//        updateUserStage(request.getUserDetails(), nextStage);
        return response;
    }

    private ApplicationStages resolveCurrentStage(MessageProcessorRequestDTO request) {
        if (request == null) {
            return ApplicationStages.defaultStage();
        }
        UserDetails userDetails = request.getUserDetails();
        if (userDetails == null) {
            return ApplicationStages.defaultStage();
        }
        return ApplicationStages.fromValue(userDetails.getCurrentStage());
    }

    private void updateUserStage(UserDetails userDetails, ApplicationStages newStage) {
        if (userDetails == null || newStage == null) {
            return;
        }
        userDetails.setCurrentStage(newStage.name());
        userDetailsRepository.save(userDetails);
    }
}
