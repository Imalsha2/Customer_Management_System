-- Sample Master Data for Customer Management System
-- MariaDB/MySQL DML Script

-- Insert Countries
INSERT INTO countries (name, code) VALUES
('Sri Lanka', 'LK'),
('India', 'IN'),
('United States', 'US'),
('United Kingdom', 'UK'),
('Australia', 'AU'),
('Canada', 'CA'),
('Singapore', 'SG'),
('Malaysia', 'MY'),
('United Arab Emirates', 'AE'),
('Qatar', 'QA');

-- Insert Cities for Sri Lanka
INSERT INTO cities (name, country_id, zip_code) VALUES
('Colombo', (SELECT id FROM countries WHERE code = 'LK'), '00100'),
('Gampaha', (SELECT id FROM countries WHERE code = 'LK'), '11000'),
('Kandy', (SELECT id FROM countries WHERE code = 'LK'), '20000'),
('Galle', (SELECT id FROM countries WHERE code = 'LK'), '80000'),
('Jaffna', (SELECT id FROM countries WHERE code = 'LK'), '40000'),
('Negombo', (SELECT id FROM countries WHERE code = 'LK'), '11500'),
('Matara', (SELECT id FROM countries WHERE code = 'LK'), '81000'),
('Anuradhapura', (SELECT id FROM countries WHERE code = 'LK'), '50000'),
('Batticaloa', (SELECT id FROM countries WHERE code = 'LK'), '30000'),
('Kurunegala', (SELECT id FROM countries WHERE code = 'LK'), '60000');

-- Insert Cities for India
INSERT INTO cities (name, country_id, zip_code) VALUES
('Mumbai', (SELECT id FROM countries WHERE code = 'IN'), '400001'),
('Delhi', (SELECT id FROM countries WHERE code = 'IN'), '110001'),
('Bangalore', (SELECT id FROM countries WHERE code = 'IN'), '560001'),
('Chennai', (SELECT id FROM countries WHERE code = 'IN'), '600001'),
('Hyderabad', (SELECT id FROM countries WHERE code = 'IN'), '500001');

-- Insert Cities for United States
INSERT INTO cities (name, country_id, zip_code) VALUES
('New York', (SELECT id FROM countries WHERE code = 'US'), '10001'),
('Los Angeles', (SELECT id FROM countries WHERE code = 'US'), '90001'),
('Chicago', (SELECT id FROM countries WHERE code = 'US'), '60601'),
('Houston', (SELECT id FROM countries WHERE code = 'US'), '77001'),
('San Francisco', (SELECT id FROM countries WHERE code = 'US'), '94101');

-- Insert Cities for United Kingdom
INSERT INTO cities (name, country_id, zip_code) VALUES
('London', (SELECT id FROM countries WHERE code = 'UK'), 'EC1A'),
('Manchester', (SELECT id FROM countries WHERE code = 'UK'), 'M1'),
('Birmingham', (SELECT id FROM countries WHERE code = 'UK'), 'B1'),
('Liverpool', (SELECT id FROM countries WHERE code = 'UK'), 'L1'),
('Edinburgh', (SELECT id FROM countries WHERE code = 'UK'), 'EH1');

-- Insert Cities for Australia
INSERT INTO cities (name, country_id, zip_code) VALUES
('Sydney', (SELECT id FROM countries WHERE code = 'AU'), '2000'),
('Melbourne', (SELECT id FROM countries WHERE code = 'AU'), '3000'),
('Brisbane', (SELECT id FROM countries WHERE code = 'AU'), '4000'),
('Perth', (SELECT id FROM countries WHERE code = 'AU'), '6000'),
('Adelaide', (SELECT id FROM countries WHERE code = 'AU'), '5000');

-- Insert Sample Customers
INSERT INTO customers (first_name, last_name, date_of_birth, nic, email, gender) VALUES
('Nimal', 'Perera', '1985-05-15', '850515123V', 'nimal.perera@email.com', 'MALE'),
('Saman', 'Silva', '1990-08-20', '900820456V', 'saman.silva@email.com', 'MALE'),
('Kamani', 'Fernando', '1988-12-10', '881210789V', 'kamani.fernando@email.com', 'FEMALE'),
('Ruwan', 'Jayawardena', '1982-03-25', '820325234V', 'ruwan.j@email.com', 'MALE'),
('Dilini', 'Rajapaksha', '1995-07-08', '950708567V', 'dilini.r@email.com', 'FEMALE');

-- Insert Addresses for Customers
INSERT INTO addresses (customer_id, address_line1, address_line2, city_id, address_type, is_primary) VALUES
(1, 'No. 45, Galle Road', 'Colombo 03', (SELECT id FROM cities WHERE name = 'Colombo'), 'HOME', TRUE),
(1, 'No. 120, Kandy Road', 'Peradeniya', (SELECT id FROM cities WHERE name = 'Kandy'), 'WORK', FALSE),
(2, 'No. 78, Main Street', 'Gampaha', (SELECT id FROM cities WHERE name = 'Gampaha'), 'HOME', TRUE),
(3, 'No. 234, Hospital Road', 'Negombo', (SELECT id FROM cities WHERE name = 'Negombo'), 'HOME', TRUE),
(4, 'No. 56, Temple Street', 'Kandy', (SELECT id FROM cities WHERE name = 'Kandy'), 'HOME', TRUE),
(5, 'No. 89, Beach Road', 'Galle', (SELECT id FROM cities WHERE name = 'Galle'), 'HOME', TRUE);

-- Insert Phone Numbers for Customers
INSERT INTO phone_numbers (customer_id, phone_number, phone_type, is_primary) VALUES
(1, '+94771234567', 'MOBILE', TRUE),
(1, '+94112345678', 'HOME', FALSE),
(2, '+94777654321', 'MOBILE', TRUE),
(3, '+94769876543', 'MOBILE', TRUE),
(3, '+94312233445', 'WORK', FALSE),
(4, '+94712345678', 'MOBILE', TRUE),
(5, '+94767890123', 'MOBILE', TRUE);

-- Insert Family Relationships
INSERT INTO customer_family_members (customer_id, family_member_id, relationship) VALUES
(1, 3, 'SPOUSE'),
(3, 1, 'SPOUSE'),
(2, 4, 'SIBLING'),
(4, 2, 'SIBLING');
