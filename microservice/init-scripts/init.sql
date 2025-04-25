-- Create databases with UTF-8 support and proper collation
CREATE DATABASE IF NOT EXISTS access_control_db 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS user_management_db 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS biometrics_db 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS model_management_db 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS training_data_db 
    CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

-- Create a dedicated user for these databases
CREATE USER IF NOT EXISTS 'app_user'@'%' IDENTIFIED BY 'StrongPassword123!';

-- Grant all privileges to the new user on all created databases
GRANT ALL PRIVILEGES ON access_control_db.* TO 'app_user'@'%';
GRANT ALL PRIVILEGES ON user_management_db.* TO 'app_user'@'%';
GRANT ALL PRIVILEGES ON biometrics_db.* TO 'app_user'@'%';
GRANT ALL PRIVILEGES ON model_management_db.* TO 'app_user'@'%';
GRANT ALL PRIVILEGES ON training_data_db.* TO 'app_user'@'%';

-- Flush privileges to ensure immediate application
FLUSH PRIVILEGES;