-- drop: Tables

DROP TABLE IF EXISTS customerfeedback;
DROP TABLE IF EXISTS rmaudit;
DROP TABLE IF EXISTS otp;
DROP TABLE IF EXISTS applicationdocuments;
DROP TABLE IF EXISTS rmmapping;
DROP TABLE IF EXISTS journeytemplate;
DROP TABLE IF EXISTS rmuser;
DROP TABLE IF EXISTS applications;
commit;

-- Table: applications
CREATE TABLE applications (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  appid VARCHAR(100) UNIQUE NOT NULL,
  journeytype VARCHAR(50) NOT NULL,
  status ENUM('inprogress', 'submitted','rejected') DEFAULT 'inprogress',
  rmid VARCHAR(50),
  salutation VARCHAR(10),
  firstname VARCHAR(100),
  lastname VARCHAR(100),
  countryCode VARCHAR(10),
  mobilenumber VARCHAR(20),
  email VARCHAR(150),
  address TEXT,
  postalcode VARCHAR(20),
  formdata LONGTEXT,
  currentpage INT,
  createddate DATETIME DEFAULT CURRENT_TIMESTAMP,
  updateddate DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Table: applicationdocuments
CREATE TABLE applicationdocuments (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  applicationid BIGINT NOT NULL,
  pagenumber INT NOT NULL,
  fieldname VARCHAR(100) NOT NULL,
  filename VARCHAR(255) NOT NULL,
  filetype VARCHAR(100),
  filecontent LONGBLOB NOT NULL,
  createddate DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (applicationid) REFERENCES applications(id)
);

-- Table: rmmapping
CREATE TABLE rmmapping (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  rmid VARCHAR(50) NOT NULL,
  rmname VARCHAR(100),
  pincode VARCHAR(20) NOT NULL,
  journeytype VARCHAR(50) NOT NULL
);

-- Table: journeytemplate
CREATE TABLE journeytemplate (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  journeytype VARCHAR(50) NOT NULL,
  version VARCHAR(10) DEFAULT 'v1',
  templatedata TEXT NOT NULL,  -- Changed from JSON to TEXT
  createddate DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE rmuser (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  rmid VARCHAR(50) UNIQUE, -- Unique RM identifier
  name VARCHAR(100) NOT NULL,
  password VARCHAR(100) NOT NULL,
  role ENUM('RM', 'ADMIN') NOT NULL,
  createddate DATETIME DEFAULT CURRENT_TIMESTAMP
);


CREATE TABLE rmaudit (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  rmid VARCHAR(100),
  applicationid BIGINT,
  message TEXT,
  createddate DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (applicationid) REFERENCES applications(id)
);

CREATE TABLE customerfeedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    applicationid BIGINT NOT NULL,
    feedback TEXT,
    createddate DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (applicationid) REFERENCES applications(id)
);


CREATE TABLE otp (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    recipient VARCHAR(100) NOT NULL,
    otp VARCHAR(10) NOT NULL,
    mode VARCHAR(10) NOT NULL, -- "sms" or "email"
    used BOOLEAN DEFAULT FALSE,
    createdate DATETIME DEFAULT CURRENT_TIMESTAMP
);


INSERT INTO applications (id, appid, journeytype, status, rmid, salutation, firstname, lastname, countryCode, mobilenumber, email, address, postalcode, formdata, currentpage, createddate, updateddate)
VALUES (
  1, 'LOA-1001', 'Loan', 'inprogress', 'anwar', 'Mr', 'Anwar', 'Sk', '+91','9876543210',
  'anwar@example.com', '123 MG Road, Bangalore', '560001',
  '{"loanAmount": 500000, "tenure": 36}', 1, '2025-07-15 10:00:00', '2025-07-15 10:00:00'
);

INSERT INTO applications (id, appid, journeytype, status, rmid, salutation, firstname, lastname, countryCode, mobilenumber, email, address, postalcode, formdata, currentpage, createddate, updateddate)
VALUES (
  2, 'LOA-1003', 'Loan', 'inprogress', 'rahul', 'Mr', 'test', 'Tm', '+91','9876543210',
  'test@example.com', '123 MG Road, Bangalore', '560001',
  '{"loanAmount": 700000, "tenure": 36}', 1, '2025-07-20 10:00:00', '2025-07-20 10:00:00'
);


-- OTP for mobile/Email
INSERT INTO otp (recipient, otp, mode, used)
VALUES ('user1@example.com', '123456', 'email', false);

INSERT INTO otp (recipient, otp, mode, used)
VALUES ('9876543210', '654321', 'sms', false);


INSERT INTO customerfeedback (applicationid, feedback) VALUES
(1, 'Very smooth onboarding process, user-friendly interface.'),
(2, 'Took longer than expected to complete the form.'),
(2, 'Excellent support from Relationship Manager.'),
(1, 'Overall a good experience. Easy and clear instructions.');

INSERT INTO rmuser (rmid,name, password, role) VALUES
('anwar','Anwar sk', 'password', 'RM'),
('bharat','Bharat', 'password', 'RM'),
('rahul','Rahul kumar', 'password', 'RM'),
('nikhil','Nikhil', 'password', 'RM'),
('arpit','Arpit', 'password', 'RM'),
('admin','Admin', 'password', 'ADMIN'),
('abhedya','Abhedya', 'password', 'RM');




-- Sample data: applicationdocuments
INSERT INTO applicationdocuments (id, applicationid, pagenumber, fieldname, filename, filetype, filecontent)
VALUES
  (1, 1, 2, 'panProof', 'pan.jpg', 'image/jpeg', '66616B6562696E6172796461746131'),
  (2, 2, 2, 'incomeDoc', 'income.pdf', 'application/pdf', '66616B6562696E6172796461746132');

-- Sample data: rmmapping
INSERT INTO rmmapping (rmid, rmname, pincode, journeytype)
VALUES
  ('rahul', 'Rahul kumar', '500090', 'Insurance'),
  ('nikhil', 'nikhil ', '500090', 'AccountOpening'),
  ('anwar', 'Anwar sk', '500090', 'Loan'),
  ('arpit', 'arpit', '500091', 'Loan'),
  ('bharat', 'Bharat', '500091', 'Insurance'),
  ('Abhedya', 'Abhedya Ayush ', '500091', 'AccountOpening');

-- Sample data: journeytemplate for Loan
INSERT INTO journeytemplate (journeytype, version, templatedata)
VALUES (
  'Loan',
  'v1',
  '{
    "pages": [
      {
        "pageIndex": 1,
        "title": "Loan Details",
        "fields": [
          {
            "label": "Loan Amount",
            "name": "loanAmount",
            "type": "number",
            "required": true,
            "validation": { "min": 10000, "max": 1000000 }
          },
          {
            "label": "Loan Purpose",
            "name": "loanPurpose",
            "type": "select",
            "options": ["Home", "Car", "Education", "Business"],
            "required": true
          }
        ]
      },
      {
        "pageIndex": 2,
        "title": "Employment Info",
        "fields": [
          {
            "label": "Employment Type",
            "name": "employmentType",
            "type": "radio",
            "options": ["Salaried", "Self-Employed"],
            "required": true
          },
          {
            "label": "Monthly Income",
            "name": "monthlyIncome",
            "type": "number",
            "required": true,
            "validation": { "min": 1000 }
          },
          {
            "label": "Income Proof Document",
            "name": "incomeProof",
            "type": "file",
            "required": true,
            "accept": [".pdf", ".jpg", ".jpeg"]
          }
        ]
      },
      {
        "pageIndex": 3,
        "title": "Identification Upload",
        "fields": [
          {
            "label": "PAN Number",
            "name": "pan",
            "type": "text",
            "required": true,
            "validation": { "regex": "^[A-Z]{5}[0-9]{4}[A-Z]$" }
          },
          {
            "label": "PAN Document Upload",
            "name": "panDocument",
            "type": "file",
            "required": true,
            "accept": [".pdf", ".jpg"]
          }
        ]
      }
    ]
  }'
);
