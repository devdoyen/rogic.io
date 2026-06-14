package com.devdoyen.nemologic.config;

import com.devdoyen.nemologic.repository.VisitorLogRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class VisitorMetricsConfig implements MeterBinder {

    private final VisitorLogRepository visitorLogRepository;

    public VisitorMetricsConfig(VisitorLogRepository visitorLogRepository) {
        this.visitorLogRepository = visitorLogRepository;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("visitor.total.visits", visitorLogRepository, VisitorLogRepository::count)
                .description("Total number of visits recorded in database")
                .register(registry);

        Gauge.builder("visitor.unique.visitors", visitorLogRepository, VisitorLogRepository::countUniqueVisitors)
                .description("Total number of unique visitors (UUIDs) recorded in database")
                .register(registry);

        Gauge.builder("visitor.daily.unique.visitors", visitorLogRepository, repo -> repo.countUniqueVisitorsByDate(LocalDate.now()))
                .description("Number of unique visitors today")
                .register(registry);
    }
}
