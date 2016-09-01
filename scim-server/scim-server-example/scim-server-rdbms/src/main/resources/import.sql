
insert into Person (person_id, first_name, last_name, middle_name, active) values (1, 'Bilbo', 'Baggins', 'Bongo', true);
insert into Person (person_id, first_name, last_name, middle_name, active) values (2, 'Frodo', 'Baggins', 'Bongo', true);
insert into Person (person_id, first_name, last_name, middle_name, active) values (3, 'Gandalf', 'Wizard', 'The', true);

  
insert into Address (address_id, street_address, city, state, country, zip_code, person_id) values (1, '133 MacDuff Cir', 'Bellefonte', 'PA', 'US', '22144', 1);
insert into Address (address_id, street_address, city, state, country, zip_code, person_id) values (2, '409B Kitts', 'China Lake', 'CA', 'US', '93555', 1);

insert into Address (address_id, street_address, city, state, country, zip_code, person_id) values (3, 'Underhill', 'Hobbiton', 'WA', 'US', '12121', 2);
insert into Address (address_id, street_address, city, state, country, zip_code, person_id) values (4, 'Overhill', 'Middle Earth', 'AJ', 'NZ', 'AE1023', 3);

insert into Phone (phone_id, phone_type, international_prefix, number, extension, person_id) values (1, 'home', '44', 'tel=+44 1423 3482734;ext=23', '444', 1);
insert into Phone (phone_id, phone_type, international_prefix, number, extension, person_id) values (2, 'work', '1', 'tel=+1 814 238 5069', null, 1);


insert into Phone (phone_id, phone_type, international_prefix, number, extension, person_id) values (3, 'fax', '1', 'tel=+1 814 238 5069', null, 2);
insert into Phone (phone_id, phone_type, international_prefix, number, extension, person_id) values (4, 'cell', '23', 'tel=+23 782938 2345', null, 3);

