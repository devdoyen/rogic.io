package com.devdoyen.nemologic.config;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public class NemologicRuntimeHints implements RuntimeHintsRegistrar {
    @Override
    public void registerHints(@NonNull RuntimeHints hints, @Nullable ClassLoader classLoader) {
        // Register static resources for puzzles
        hints.resources().registerPattern("puzzles/stages.json");
        // Register database migration scripts for Flyway
        hints.resources().registerPattern("db/migration/*.sql");
    }
}
