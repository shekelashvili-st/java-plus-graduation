package ru.yandex.practicum.stats.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnalyzerRunner implements CommandLineRunner {
    final UserActionProcessor userActionProcessor;
    final EventSimilarityProcessor eventSimilarityProcessor;

    @Override
    public void run(String... args) throws Exception {
        Thread hubEventsThread = new Thread(userActionProcessor);
        hubEventsThread.setName("userActionProcessorThread");
        hubEventsThread.start();

        eventSimilarityProcessor.run();
    }
}
