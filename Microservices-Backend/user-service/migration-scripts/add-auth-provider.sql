-- Migration script to add auth_provider columns to users table
-- This script is for reference only - Hibernate will auto-create columns with ddl-auto: update
-- However, existing users need to be updated to have LOCAL auth provider

-- Update all existing users to have LOCAL auth provider (they registered with password)
UPDATE users 
SET auth_provider = 'LOCAL' 
WHERE auth_provider IS NULL;

-- Verify the update
SELECT id, email, name, auth_provider, created_at 
FROM users 
ORDER BY created_at DESC;
