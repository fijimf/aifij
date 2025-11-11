-- PostgreSQL Performance Tuning for Batch Upserts
-- Run these commands as a superuser or with appropriate privileges
-- Adjust values based on your available system resources

-- Memory Settings (adjust based on available RAM)
-- For a system with 8GB RAM, these are reasonable defaults
ALTER SYSTEM SET shared_buffers = '2GB';
ALTER SYSTEM SET effective_cache_size = '6GB';
ALTER SYSTEM SET maintenance_work_mem = '512MB';
ALTER SYSTEM SET work_mem = '16MB';

-- Write Ahead Log (WAL) Settings for better write performance
ALTER SYSTEM SET wal_buffers = '16MB';
ALTER SYSTEM SET checkpoint_completion_target = 0.9;
ALTER SYSTEM SET max_wal_size = '2GB';
ALTER SYSTEM SET min_wal_size = '1GB';

-- Planner Settings
ALTER SYSTEM SET random_page_cost = 1.1;  -- Lower for SSD
ALTER SYSTEM SET effective_io_concurrency = 200;  -- Higher for SSD

-- Connection Settings
ALTER SYSTEM SET max_connections = 100;

-- Synchronous commit - set to 'off' for maximum performance (but slightly less durability)
-- Only do this if you can tolerate potential data loss in case of crash
-- ALTER SYSTEM SET synchronous_commit = 'off';

-- For local development, you might want to disable fsync for even better performance
-- WARNING: NEVER use this in production!
-- ALTER SYSTEM SET fsync = 'off';

-- Apply the changes (requires PostgreSQL restart)
-- Uncomment and run after reviewing settings:
-- SELECT pg_reload_conf();
-- Or restart PostgreSQL:
-- sudo systemctl restart postgresql  (Linux)
-- brew services restart postgresql   (macOS with Homebrew)

-- Check current settings
SELECT name, setting, unit, context
FROM pg_settings
WHERE name IN (
    'shared_buffers',
    'effective_cache_size',
    'maintenance_work_mem',
    'work_mem',
    'wal_buffers',
    'checkpoint_completion_target',
    'max_wal_size',
    'random_page_cost',
    'effective_io_concurrency',
    'synchronous_commit',
    'fsync'
)
ORDER BY name;
