/*CREATE DATABASE back2u;*/

USE back2u;

/*CREATE TABLE accountdetails(user_id int auto_increment primary key ,
								name varchar(100), 
								student_id varchar(20), 
                                email varchar(50),
                                phone varchar(10), 
                                password varchar(50));*/


/*CREATE TABLE items (
    item_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    report_type ENUM('LOST','FOUND'),
    item_name VARCHAR(100),
    category VARCHAR(50),
    description VARCHAR(500),
    date_occurred DATE,
    time_occurred VARCHAR(50),
    location VARCHAR(200),
    status VARCHAR(30) DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    unique_details VARCHAR(500),

    CONSTRAINT fk_items_user
        FOREIGN KEY (user_id)
        REFERENCES accountdetails(user_id)
);*/



/*CREATE TABLE admin_reports (
    report_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    issue_type VARCHAR(100) NOT NULL,
    subject VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) DEFAULT 'OPEN',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES accountdetails(user_id)
);*/


/*CREATE TABLE IF NOT EXISTS item_matches (
    match_id INT AUTO_INCREMENT PRIMARY KEY,
    lost_item_id INT NOT NULL,
    found_item_id INT NOT NULL,
    match_score INT NOT NULL,
    owner_response ENUM(
        'PENDING', 'CONFIRMED', 'REJECTED'
    ) DEFAULT 'PENDING',
    finder_response ENUM(
        'PENDING', 'CONFIRMED', 'REJECTED'
    ) DEFAULT 'PENDING',
    match_status ENUM(
        'PENDING', 'VERIFICATION', 'REJECTED', 'COMPLETED'
    ) DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    UNIQUE KEY unique_item_pair (lost_item_id, found_item_id),

    FOREIGN KEY (lost_item_id)
        REFERENCES items(item_id),

    FOREIGN KEY (found_item_id)
        REFERENCES items(item_id)
);*/

/*CREATE TABLE handover (
    handover_id INT AUTO_INCREMENT PRIMARY KEY,
    match_id INT NOT NULL UNIQUE,

    meeting_location VARCHAR(200) NOT NULL,
    meeting_date DATE NOT NULL,
    meeting_time VARCHAR(20) NOT NULL,
    instructions VARCHAR(500),

    pin VARCHAR(6) NOT NULL,
    status ENUM('PENDING', 'COMPLETED')
        DEFAULT 'PENDING',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    returned_at DATETIME NULL,

    CONSTRAINT fk_handover_match
        FOREIGN KEY (match_id)
        REFERENCES item_matches(match_id)
);*/


/*select * from accountdetails;
select * from items;
select * from item_matches;
select * from admin_reports;
select * from handover;*/

/*ALTER TABLE items
ADD COLUMN trust_score INT NOT NULL DEFAULT 0,
ADD COLUMN ad_review_status VARCHAR(20)
    NOT NULL DEFAULT 'PENDING_REVIEW';*/
    

/*CREATE TABLE advertisement_ratings (
    rating_id INT AUTO_INCREMENT PRIMARY KEY,
    item_id INT NOT NULL,
    user_id INT NOT NULL,
    rating INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_ad_rating
        CHECK (rating BETWEEN 1 AND 5),

    CONSTRAINT uq_item_user_rating
        UNIQUE (item_id, user_id),

    CONSTRAINT fk_rating_item
        FOREIGN KEY (item_id) REFERENCES items(item_id)
        ON DELETE CASCADE
);*/

/*CREATE TABLE advertisement_reports (
    report_id INT AUTO_INCREMENT PRIMARY KEY,
    item_id INT NOT NULL,
    reporter_user_id INT NOT NULL,
    reason VARCHAR(100) NOT NULL,
    details VARCHAR(1000),
    report_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_ad_report_item
        FOREIGN KEY (item_id) REFERENCES items(item_id)
        ON DELETE CASCADE
);*/

select * from advertisement_ratings;
select * from advertisement_reports;
select * from handover;
select * from admin_reports;
select * from item_matches;
select * from items;
select * from accountdetails;

USE back2u;

/*ALTER TABLE items
ADD COLUMN initial_trust_score INT NOT NULL DEFAULT 0;*/

/*DESCRIBE items;

UPDATE items
SET initial_trust_score = trust_score
WHERE item_id > 0;

ALTER TABLE advertisement_reports
ADD CONSTRAINT fk_ad_reporter_user
FOREIGN KEY (reporter_user_id)
REFERENCES accountdetails(user_id);

SELECT
    item_id,
    item_name,
    initial_trust_score,
    trust_score,
    ad_review_status
FROM items;*/

SELECT item_id, user_id, report_type, item_name,
       status, ad_review_status
FROM items
ORDER BY item_id DESC;

SELECT match_id, lost_item_id, found_item_id,
       match_score, match_status
FROM item_matches
ORDER BY match_id DESC;
