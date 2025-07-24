
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

