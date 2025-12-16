-- Fix the settings table to have auto-increment ID
USE settings_service_db;

-- Drop and recreate the table with proper auto-increment
DROP TABLE IF EXISTS settings;

CREATE TABLE settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key VARCHAR(255) NOT NULL UNIQUE,
    setting_value TEXT,
    description VARCHAR(500)
);

-- Insert default settings
INSERT INTO settings (setting_key, setting_value, description) VALUES
('siteName', 'RevTicket', 'Site name'),
('siteEmail', 'support@revticket.com', 'Site email'),
('sitePhone', '+91 1234567890', 'Site phone'),
('currency', 'INR', 'Currency'),
('timezone', 'Asia/Kolkata', 'Timezone'),
('bookingCancellationHours', '2', 'Booking cancellation hours'),
('convenienceFeePercent', '5', 'Convenience fee percent'),
('gstPercent', '18', 'GST percent'),
('maxSeatsPerBooking', '10', 'Max seats per booking'),
('enableNotifications', 'true', 'Enable notifications'),
('enableEmailNotifications', 'true', 'Enable email notifications'),
('enableSMSNotifications', 'false', 'Enable SMS notifications'),
('maintenanceMode', 'false', 'Maintenance mode');