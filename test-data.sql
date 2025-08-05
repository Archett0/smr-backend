-- Test data for SMR (Smart Matching Rental) system
-- Created for testing SearchService and other microservices

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS smr;
USE smr;

-- Clear existing data (optional - uncomment if needed)
-- DELETE FROM notification;
-- DELETE FROM property;
-- DELETE FROM base_user;

-- Insert test users (Agents and Tenants)
-- Note: oidc_sub should match Auth0 user IDs in production
INSERT INTO base_user (
    oidc_sub, username, email, phone_number, registered_at, last_login_at, 
    enabled, picture, role, user_type, verified, price_alert_enabled
) VALUES
    -- Agents
    ('auth0|agent001', 'john_agent', 'john.agent@smr.com', '+1-555-0101', 
     '2024-01-15 10:00:00', '2024-02-01 09:30:00', true, 
     'https://via.placeholder.com/150x150?text=JA', 'AGENT', 'AGENT', true, null),
    
    ('auth0|agent002', 'sarah_realtor', 'sarah.realtor@smr.com', '+1-555-0102', 
     '2024-01-10 14:30:00', '2024-02-01 11:15:00', true, 
     'https://via.placeholder.com/150x150?text=SR', 'AGENT', 'AGENT', true, null),
    
    ('auth0|agent003', 'mike_properties', 'mike.properties@smr.com', '+1-555-0103', 
     '2024-01-20 16:45:00', '2024-02-01 08:45:00', true, 
     'https://via.placeholder.com/150x150?text=MP', 'AGENT', 'AGENT', false, null),
    
    -- Tenants  
    ('auth0|683554001b464400d0f44b31f', 'alice_tenant', 'alice.tenant@email.com', '+1-555-0201', 
     '2024-01-25 12:00:00', '2024-02-01 13:20:00', true, 
     'https://via.placeholder.com/150x150?text=AT', 'TENANT', 'TENANT', null, true),
    
    ('auth0|tenant002', 'bob_renter', 'bob.renter@email.com', '+1-555-0202', 
     '2024-01-28 09:15:00', '2024-02-01 10:45:00', true, 
     'https://via.placeholder.com/150x150?text=BR', 'TENANT', 'TENANT', null, false),
    
    ('auth0|tenant003', 'charlie_student', 'charlie.student@email.com', '+1-555-0203', 
     '2024-01-30 18:30:00', '2024-02-01 12:00:00', true, 
     'https://via.placeholder.com/150x150?text=CS', 'TENANT', 'TENANT', null, true),
    
    -- Admin
    ('auth0|admin001', 'admin_user', 'admin@smr.com', '+1-555-0001', 
     '2024-01-01 00:00:00', '2024-02-01 07:00:00', true, 
     'https://via.placeholder.com/150x150?text=AD', 'ADMIN', 'ADMIN', null, null);

-- Insert test properties (All in Singapore)
INSERT INTO property (
    title, description, price, address, img, latitude, longitude, 
    num_bedrooms, num_bathrooms, available, posted_at, agent_id
) VALUES
    ('Modern CBD Apartment', 
     'Beautiful 2-bedroom apartment in the heart of Singapore CBD with city skyline views, marble floors, and modern amenities. Close to MRT stations and shopping malls.',
     3500.00, 
     '10 Marina Boulevard, Marina Bay, Singapore 018980', 
     'https://via.placeholder.com/800x600?text=Modern+CBD+Apt', 
     1.2800, 103.8540, 
     2, 2, true, 
     '2024-01-15 11:00:00', 
     'auth0|agent001'),
    
    ('Luxury Orchard Penthouse', 
     'Stunning 3-bedroom penthouse with panoramic city views, private infinity pool, granite countertops, and premium finishes throughout. Prime Orchard Road location.',
     8500.00, 
     '350 Orchard Road, Singapore 238868', 
     'https://via.placeholder.com/800x600?text=Luxury+Penthouse', 
     1.3048, 103.8318, 
     3, 3, true, 
     '2024-01-18 14:30:00', 
     'auth0|agent002'),
    
    ('Cozy Studio Near NUS', 
     'Perfect for students! Cozy studio apartment just 10 minutes by bus to NUS campus. Includes all utilities, WiFi, and access to condo facilities.',
     2200.00, 
     '15 Kent Ridge Road, Singapore 119245', 
     'https://via.placeholder.com/800x600?text=Studio+Apt', 
     1.2966, 103.7764, 
     1, 1, true, 
     '2024-01-20 16:45:00', 
     'auth0|agent001'),
    
    ('Family Condo in Punggol', 
     'Spacious 4-bedroom condo with garden view, perfect for families. Located in a family-friendly neighborhood with excellent schools and waterway parks nearby.',
     4200.00, 
     '88 Punggol Point Road, Singapore 828822', 
     'https://via.placeholder.com/800x600?text=Family+Condo', 
     1.4175, 103.9048, 
     4, 3, true, 
     '2024-01-22 10:15:00', 
     'auth0|agent002'),
    
    ('Urban Loft in Tiong Bahru', 
     'Trendy loft apartment with high ceilings and modern kitchen in historic Tiong Bahru district. Walking distance to hipster cafes and heritage sites.',
     3800.00, 
     '56 Tiong Bahru Road, Singapore 160056', 
     'https://via.placeholder.com/800x600?text=Urban+Loft', 
     1.2859, 103.8266, 
     2, 2, true, 
     '2024-01-25 09:30:00', 
     'auth0|agent003'),
    
    ('Sentosa Beachfront Condo', 
     'Wake up to sea views every day! This 2-bedroom beachfront condo on Sentosa Island features floor-to-ceiling windows and private balcony overlooking the beach.',
     5500.00, 
     '26 Sentosa Gateway, Sentosa Island, Singapore 098138', 
     'https://via.placeholder.com/800x600?text=Beach+Condo', 
     1.2494, 103.8303, 
     2, 2, true, 
     '2024-01-28 13:45:00', 
     'auth0|agent001'),
    
    ('Heritage Shophouse Apartment', 
     'Charming 1-bedroom apartment in a restored heritage shophouse in Chinatown. Features original architectural details and modern conveniences.',
     3200.00, 
     '45 Tanjong Pagar Road, Singapore 088463', 
     'https://via.placeholder.com/800x600?text=Shophouse', 
     1.2799, 103.8443, 
     1, 1, false, 
     '2024-01-30 11:20:00', 
     'auth0|agent002'),
    
    ('Garden View Condo in Jurong', 
     'Peaceful 3-bedroom condo surrounded by lush greenery in Jurong West. Perfect for nature lovers with easy access to parks and nature reserves.',
     2800.00, 
     '501 Jurong West Street 51, Singapore 640501', 
     'https://via.placeholder.com/800x600?text=Garden+Condo', 
     1.3496, 103.7197, 
     3, 2, false, 
     '2024-02-01 15:00:00', 
     'auth0|agent003'),

    ('Executive HDB in Tampines', 
     'Spacious 5-room executive HDB flat with panoramic views from high floor. Recently renovated with modern fixtures and near Tampines Mall.',
     3000.00, 
     '201 Tampines Street 21, Singapore 524201', 
     'https://via.placeholder.com/800x600?text=Executive+HDB', 
     1.3521, 103.9445, 
     3, 2, true, 
     '2024-01-16 09:15:00', 
     'auth0|agent001'),

    ('Modern Studio in Bugis', 
     'Stylish studio apartment in the vibrant Bugis district. Walking distance to MRT, shopping streets, and cultural attractions. Perfect for young professionals.',
     2800.00, 
     '22 Victoria Street, Singapore 187978', 
     'https://via.placeholder.com/800x600?text=Bugis+Studio', 
     1.2966, 103.8520, 
     1, 1, true, 
     '2024-01-17 14:20:00', 
     'auth0|agent002'),

    ('Waterfront Condo in Kallang', 
     'Beautiful 2-bedroom waterfront condo with river views and access to kayaking facilities. Modern amenities and near Sports Hub.',
     4100.00, 
     '100 Kallang Riverside, Singapore 329053', 
     'https://via.placeholder.com/800x600?text=Waterfront+Condo', 
     1.3058, 103.8714, 
     2, 2, true, 
     '2024-01-19 11:30:00', 
     'auth0|agent003'),

    ('Cozy 2-Room HDB in Woodlands', 
     'Affordable 2-room HDB flat perfect for singles or couples. Near Causeway Point shopping mall and MRT station. Quiet residential area.',
     1800.00, 
     '680 Woodlands Drive 75, Singapore 730680', 
     'https://via.placeholder.com/800x600?text=Woodlands+HDB', 
     1.4382, 103.8017, 
     2, 1, true, 
     '2024-01-21 16:45:00', 
     'auth0|agent001'),

    ('Luxury Penthouse in Holland Village', 
     'Ultra-luxurious 4-bedroom penthouse in exclusive Holland Village. Private lift access, rooftop terrace, and premium finishes throughout.',
     12000.00, 
     '15 Holland Hill, Singapore 278077', 
     'https://via.placeholder.com/800x600?text=Holland+Penthouse', 
     1.3093, 103.7965, 
     4, 4, true, 
     '2024-01-23 10:00:00', 
     'auth0|agent002'),

    ('Family-Friendly Condo in Serangoon', 
     'Well-maintained 3-bedroom condo in family-oriented Serangoon Gardens. Near good schools, parks, and local amenities. Peaceful environment.',
     3600.00, 
     '33 Serangoon Garden Way, Singapore 555972', 
     'https://via.placeholder.com/800x600?text=Serangoon+Condo', 
     1.3647, 103.8700, 
     3, 2, true, 
     '2024-01-24 13:15:00', 
     'auth0|agent003'),

    ('Trendy Loft in Clarke Quay', 
     'Hip industrial-style loft in the heart of Clarke Quay nightlife district. High ceilings, exposed beams, and river views. Perfect for entertainment.',
     4800.00, 
     '5 River Valley Road, Singapore 179024', 
     'https://via.placeholder.com/800x600?text=Clarke+Loft', 
     1.2884, 103.8459, 
     2, 2, true, 
     '2024-01-26 15:30:00', 
     'auth0|agent001'),

    ('Spacious 4-Room HDB in Clementi', 
     'Well-located 4-room HDB near NTU and Clementi Mall. Perfect for families or students. Recently painted and upgraded with new flooring.',
     2600.00, 
     '450 Clementi Avenue 3, Singapore 120450', 
     'https://via.placeholder.com/800x600?text=Clementi+HDB', 
     1.3162, 103.7649, 
     3, 2, true, 
     '2024-01-27 08:45:00', 
     'auth0|agent002'),

    ('Luxury Waterfront Condo in East Coast', 
     'Premium 3-bedroom condo with direct beach access and sea views. Resort-style living with swimming pools, tennis court, and BBQ areas.',
     6500.00, 
     '85 Marine Parade Road, Singapore 449282', 
     'https://via.placeholder.com/800x600?text=East+Coast+Condo', 
     1.3018, 103.9048, 
     3, 3, true, 
     '2024-01-29 12:00:00', 
     'auth0|agent003'),

    ('Modern Studio near SMU', 
     'Contemporary studio apartment perfect for university students or young professionals. Located near SMU campus and city center attractions.',
     2400.00, 
     '18 Stamford Road, Singapore 178892', 
     'https://via.placeholder.com/800x600?text=SMU+Studio', 
     1.2945, 103.8520, 
     1, 1, true, 
     '2024-01-31 10:30:00', 
     'auth0|agent001'),

    ('Executive Apartment in Novena', 
     'Premium 2-bedroom executive apartment in medical hub district. Near hospitals, shopping centers, and MRT. Ideal for medical professionals.',
     4300.00, 
     '101 Novena Street, Singapore 308225', 
     'https://via.placeholder.com/800x600?text=Novena+Exec', 
     1.3201, 103.8439, 
     2, 2, true, 
     '2024-02-02 14:15:00', 
     'auth0|agent002'),

    ('Terrace House in Katong', 
     'Charming 3-bedroom terrace house in heritage Katong area. Traditional Peranakan architecture with modern renovations. Private courtyard garden.',
     5200.00, 
     '122 Joo Chiat Road, Singapore 427412', 
     'https://via.placeholder.com/800x600?text=Katong+Terrace', 
     1.3067, 103.9018, 
     3, 3, false, 
     '2024-02-03 09:45:00', 
     'auth0|agent003'),

    ('High-Rise Condo in Toa Payoh', 
     'Modern 2-bedroom condo in central Toa Payoh with panoramic city views. Near MRT, hawker centers, and shopping complexes. Great connectivity.',
     3400.00, 
     '200 Lorong 2 Toa Payoh, Singapore 319642', 
     'https://via.placeholder.com/800x600?text=Toa+Payoh+Condo', 
     1.3343, 103.8479, 
     2, 2, true, 
     '2024-02-04 11:20:00', 
     'auth0|agent001'),

    ('Affordable HDB in Yishun', 
     'Budget-friendly 3-room HDB flat in established Yishun town. Near schools, markets, and community facilities. Good for first-time renters.',
     2000.00, 
     '354 Yishun Street 31, Singapore 760354', 
     'https://via.placeholder.com/800x600?text=Yishun+HDB', 
     1.4284, 103.8351, 
     2, 1, true, 
     '2024-02-05 16:00:00', 
     'auth0|agent002');

-- Insert test notifications
INSERT INTO notification (
    tenant_id, agent_id, message, type, isread, created_at
) VALUES
    ('auth0|683554001b464400d0f44b31f', 'auth0|agent001', 
     'New property matching your criteria: Modern CBD Apartment in Marina Bay', 
     'SYSTEM', false, '2024-02-01 10:00:00'),
    
    ('auth0|tenant002', 'auth0|agent002', 
     'Price drop alert: Luxury Orchard Penthouse now SGD $8500/month (was SGD $9000)', 
     'PRICE_ALERT', false, '2024-02-01 11:30:00'),
    
    ('auth0|tenant003', 'auth0|agent001', 
     'Your application for Cozy Studio Near NUS has been received', 
     'SYSTEM', true, '2024-02-01 14:15:00'),
    
    ('auth0|683554001b464400d0f44b31f', 'auth0|agent002', 
     'Viewing scheduled for Family Condo in Punggol on Feb 5th at 2:00 PM', 
     'VIEWING_CONFIRM', false, '2024-02-01 16:45:00'),
    
    ('auth0|tenant002', 'auth0|agent003', 
     'Welcome to SMR Singapore! Complete your profile to get better property matches', 
     'SYSTEM', true, '2024-01-28 12:00:00');

-- Display summary of inserted data
SELECT 'Users inserted:' as summary, COUNT(*) as count FROM base_user
UNION ALL
SELECT 'Properties inserted:', COUNT(*) FROM property
UNION ALL  
SELECT 'Notifications inserted:', COUNT(*) FROM notification;

-- Verify the data structure
SHOW TABLES;

-- Sample queries to verify data
SELECT 'Sample Properties:' as info;
SELECT id, title, price, address, num_bedrooms, num_bathrooms, available 
FROM property LIMIT 3;

SELECT 'Sample Users:' as info;
SELECT id, username, email, role, user_type, enabled 
FROM base_user LIMIT 3;

SELECT 'Sample Notifications:' as info;
SELECT id, tenant_id, message, type, isread 
FROM notification LIMIT 3;