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

        // Seed default stages if they don't exist
        saveStageIfAbsent("Diamond Emblem", 5, 5, new int[][]{
            {0, 0, 1, 0, 0},
            {0, 1, 1, 1, 0},
            {1, 1, 0, 1, 1},
            {0, 1, 1, 1, 0},
            {0, 0, 1, 0, 0}
        });

        saveStageIfAbsent("Cross Ruby", 5, 5, new int[][]{
            {1, 0, 0, 0, 1},
            {0, 1, 0, 1, 0},
            {0, 0, 1, 0, 0},
            {0, 1, 0, 1, 0},
            {1, 0, 0, 0, 1}
        });

        saveStageIfAbsent("Crystalline Spark", 5, 5, new int[][]{
            {0, 1, 0, 1, 0},
            {1, 0, 1, 0, 1},
            {0, 1, 0, 1, 0},
            {1, 0, 1, 0, 1},
            {0, 1, 0, 1, 0}
        });

        saveStageIfAbsent("Hourglass", 5, 5, new int[][]{
            {1, 1, 1, 1, 1},
            {0, 1, 1, 1, 0},
            {0, 0, 1, 0, 0},
            {0, 1, 1, 1, 0},
            {1, 1, 1, 1, 1}
        });

        saveStageIfAbsent("Smile Face", 10, 10, new int[][]{
            {0, 0, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
            {1, 0, 1, 0, 0, 0, 0, 1, 0, 1},
            {1, 0, 1, 0, 0, 0, 0, 1, 0, 1},
            {1, 0, 0, 0, 0, 0, 0, 0, 0, 1},
            {1, 0, 1, 0, 0, 0, 0, 1, 0, 1},
            {1, 0, 0, 1, 1, 1, 1, 0, 0, 1},
            {0, 1, 0, 0, 0, 0, 0, 0, 1, 0},
            {0, 0, 1, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
        });

        saveStageIfAbsent("Ascending Star", 15, 15, new int[][]{
            {0,0,0,0,0,0,0,1,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,1,1,1,0,0,0,0,0,0},
            {0,0,0,0,0,1,1,1,1,1,0,0,0,0,0},
            {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
            {0,1,1,1,1,1,1,1,1,1,1,1,1,1,0},
            {0,0,1,1,1,1,1,1,1,1,1,1,1,0,0},
            {0,0,0,1,1,1,1,1,1,1,1,1,0,0,0},
            {0,0,0,0,1,1,1,1,1,1,1,0,0,0,0},
            {0,0,0,0,1,1,1,1,1,1,1,0,0,0,0},
            {0,0,0,1,1,1,1,0,1,1,1,1,0,0,0},
            {0,0,1,1,1,1,0,0,0,1,1,1,1,0,0},
            {0,1,1,1,0,0,0,0,0,0,0,1,1,1,0},
            {1,1,1,0,0,0,0,0,0,0,0,0,1,1,1},
            {1,1,0,0,0,0,0,0,0,0,0,0,0,1,1},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}
        });

        int[][] grid20 = new int[20][20];
        for (int i = 0; i < 20; i++) {
            for (int j = 0; j < 20; j++) {
                grid20[i][j] = (i + j) % 2 == 0 ? 1 : 0;
            }
        }
        saveStageIfAbsent("Checkerboard 20x20", 20, 20, grid20);

        int[][] grid30 = new int[30][30];
        for (int i = 0; i < 30; i++) {
            grid30[i][i] = 1;
            grid30[i][29 - i] = 1;
        }
        saveStageIfAbsent("Giant Cross 30x30", 30, 30, grid30);
    }

    private void saveStageIfAbsent(String name, int width, int height, int[][] solutionGrid) {
        if (stageRepository.findByName(name).isEmpty()) {
            stageRepository.save(new Stage(null, name, width, height, solutionGrid));
        }
    }
}
