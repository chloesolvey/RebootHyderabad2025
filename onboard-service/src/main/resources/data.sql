
INSERT INTO applications (appid, journeytype, status, rmid, salutation, firstname, lastname, countryCode, mobilenumber, email, address, postalcode, formdata, currentpage, createddate, updateddate)
VALUES (
           'LOA-1001', 'Loan', 'inprogress', 'anwar', 'Mr', 'Anwar', 'Sk', '+91','9876543210',
           'anwar@example.com', '123 MG Road, Bangalore', '560001',
           '{"loanAmount": 500000, "tenure": 36}', 1, '2025-07-20 10:00:00', '2025-07-15 10:00:00'
       );

INSERT INTO applications (appid, journeytype, status, rmid, salutation, firstname, lastname, countryCode, mobilenumber, email, address, postalcode, formdata, currentpage, createddate, updateddate)
VALUES (
           'LOA-1003', 'Loan', 'inprogress', 'rahul', 'Mr', 'test', 'Tm', '+91','9876543210',
           'test@example.com', '123 MG Road, Bangalore', '560001',
           '{"loanAmount": 700000, "tenure": 36}', 1, '2025-07-25 10:00:00', '2025-07-20 10:00:00'
       );


-- OTP for mobile/Email
INSERT INTO otp (recipient, otp, mode, used)
VALUES ('user1@example.com', '123456', 'email', false);

INSERT INTO otp (recipient, otp, mode, used)
VALUES ('9876543210', '654321', 'sms', false);


/*INSERT INTO customerfeedback (applicationid, feedback) VALUES
                                                           (1, 'Very smooth onboarding process, user-friendly interface.'),
                                                           (2, 'Took longer than expected to complete the form.'),
                                                           (2, 'Excellent support from Relationship Manager.'),
                                                           (1, 'Overall a good experience. Easy and clear instructions.');
*/
INSERT INTO rmuser (rmid,name, password, role) VALUES
                                                   ('anwar','Anwar sk', 'password', 'RM'),
                                                   ('bharat','Bharat', 'password', 'RM'),
                                                   ('rahul','Rahul kumar', 'password', 'RM'),
                                                   ('nikhil','Nikhil', 'password', 'RM'),
                                                   ('arpit','Arpit', 'password', 'RM'),
                                                   ('admin','Admin', 'password', 'ADMIN'),
                                                   ('abhedya','Abhedya', 'password', 'RM');




-- Sample data: applicationdocuments
/*INSERT INTO applicationdocuments (id, applicationid, pagenumber, fieldname, filename, filetype, filecontent)
VALUES
    (1, 1, 2, 'panProof', 'pan.jpg', 'image/jpeg', '66616B6562696E6172796461746131'),
    (2, 2, 2, 'incomeDoc', 'income.pdf', 'application/pdf', '66616B6562696E6172796461746132');
*/
-- Sample data: rmmapping
INSERT INTO rmmapping (rmid, rmname, pincode, journeytype)
VALUES
    ('rahul', 'Rahul kumar', '500090', 'Insurance'),
    ('nikhil', 'nikhil ', '500090', 'AccountOpening'),
    ('anwar', 'Anwar sk', '500090', 'Loan'),
    ('arpit', 'arpit', '500091', 'Loan'),
    ('bharat', 'Bharat', '500091', 'Insurance'),
    ('Abhedya', 'Abhedya Ayush ', '500091', 'AccountOpening');

-- Sample data: journeytemplate for Loan and PCA
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
INSERT INTO journeytemplate (journeytype, version, templatedata)
VALUES (
           'PCA',
           'v1',
           '{
             "pages": [
               {
                 "pageIndex": 1,
                 "title": "Personal Details",
                 "fields": [
                   {
                     "label": "Do you have a partner or spouse?",
                     "name": "havePartnerSpouse",
                     "type": "select",
                     "required": true,
                     "validation": {},
                     "options": ["Yes", "No"],
                     "accept": [""]
                   },
                   {
                     "label": "Do you own or rent your home?",
                     "name": "ownRentHome",
                     "type": "select",
                     "required": true,
                     "validation": {},
                     "options": ["I own- no mortgage", "I own - with a mortgage", "I rent privately"],
                     "accept": [""]
                   },
                   {
                     "label": "What your monthly mortgage payment (your share only)?",
            "name": "mortgagePayment",
            "type": "text",
            "required": true,
            "validation": { "regex": "^[0-9]+$" },
            "options": [""],
            "accept": [""]
          },
          {
            "label": "what do you pay each month for child-care, tuition fees or maintenance (your share only)?",
            "name": "maintenance",
            "type": "text",
            "required": true,
            "validation": { "regex": "^[0-9]+$" },
            "options": [""],
            "accept": [""]
          }
        ]
      },
      {
        "pageIndex": 2,
        "title": "Employment Info",
        "fields": [
          {
            "label": "What do you do for a living?",
            "name": "workForLiving",
            "type": "select",
            "required": true,
            "validation": {},
            "options": ["I work full time", "I work part time"],
            "accept": [""]
          },
          {
            "label": "What kind of work do you do?",
            "name": "workType",
            "type": "select",
            "required": true,
            "validation": {},
            "options": ["Manager", "professional"],
            "accept": [""]
          },
          {
            "label": "Who do you work for?",
            "name": "employerName",
            "type": "text",
            "required": true,
            "validation": {},
            "options": [""],
            "accept": [""]
          },
          {
            "label": "what''s your monthly income after deductions",
            "name": "incomeAfterDeductions",
            "type": "text",
            "required": true,
            "validation": { "regex": "^[0-9]+$" },
            "options": [""],
            "accept": [""]
          },
          {
            "label": "what''s your total amount of savings?",
            "name": "totalSavings",
            "type": "text",
            "required": true,
            "validation": { "regex": "^[0-9]+$" },
            "options": [""],
            "accept": [""]
          }
        ]
      },
      {
        "pageIndex": 3,
        "title": "Identification Upload",
        "fields": [
          {
            "label": "Passport",
            "name": "passport",
            "type": "text",
            "required": true,
            "validation": { "regex": "^[A-Z]{1}[0-9]{7}[A-Z]{1}$" }
          },
          {
            "label": "Passport Upload",
            "name": "passportDocument",
            "type": "file",
            "required": true,
            "accept": [".pdf", ".jpg"]
          }
        ]
      }
    ]
  }'
);