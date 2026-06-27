-- Delete old non-logical default stages so that they can be re-seeded from stages.json
DELETE FROM stages WHERE name IN ('Cross Ruby', 'Smile Face', 'Checkerboard 20x20', 'Giant Cross 30x30');

-- Clean up AI/Daily prefixes from existing stage names
UPDATE stages SET name = REPLACE(name, 'AI Puzzle: ', '');
UPDATE stages SET name = REPLACE(name, 'Daily Puzzle: ', '');
UPDATE stages SET name = REPLACE(name, 'AI Puzzle:', '');
UPDATE stages SET name = REPLACE(name, 'Daily Puzzle:', '');
UPDATE stages SET name = 'Puzzle' WHERE name = 'AI Puzzle' OR name = 'Daily Puzzle' OR name = '';
