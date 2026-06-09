package com.devdoyen.nemologic.config;

import com.devdoyen.nemologic.model.Stage;
import com.devdoyen.nemologic.model.User;
import com.devdoyen.nemologic.repository.StageRepository;
import com.devdoyen.nemologic.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final StageRepository stageRepository;

    public DataSeeder(UserRepository userRepository, StageRepository stageRepository) {
        this.userRepository = userRepository;
        this.stageRepository = stageRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed default users if database is empty
        if (userRepository.count() == 0) {
            userRepository.save(new User(null, "Player1", 200, 2));
            userRepository.save(new User(null, "Player2", 500, 3));
            userRepository.save(new User(null, "Player3", 1000, 5));
        }

        // Seed default stages if database is empty
        if (stageRepository.count() == 0) {
            stageRepository.save(new Stage(
                null,
                "Diamond Emblem",
                5,
                5,
                new int[][]{
                    {0, 0, 1, 0, 0},
                    {0, 1, 1, 1, 0},
                    {1, 1, 0, 1, 1},
                    {0, 1, 1, 1, 0},
                    {0, 0, 1, 0, 0}
                }
            ));

            stageRepository.save(new Stage(
                null,
                "Cross Ruby",
                5,
                5,
                new int[][]{
                    {1, 0, 0, 0, 1},
                    {0, 1, 0, 1, 0},
                    {0, 0, 1, 0, 0},
                    {0, 1, 0, 1, 0},
                    {1, 0, 0, 0, 1}
                }
            ));

            stageRepository.save(new Stage(
                null,
                "Crystalline Spark",
                5,
                5,
                new int[][]{
                    {0, 1, 0, 1, 0},
                    {1, 0, 1, 0, 1},
                    {0, 1, 0, 1, 0},
                    {1, 0, 1, 0, 1},
                    {0, 1, 0, 1, 0}
                }
            ));

            stageRepository.save(new Stage(
                null,
                "Hourglass",
                5,
                5,
                new int[][]{
                    {1, 1, 1, 1, 1},
                    {0, 1, 1, 1, 0},
                    {0, 0, 1, 0, 0},
                    {0, 1, 1, 1, 0},
                    {1, 1, 1, 1, 1}
                }
            ));
        }

    }
}
