# System-wide announcements

# --- !Ups
create extension pgcrypto;

ALTER TABLE users add column hashedPassword text;

UPDATE users SET hashedPassword = crypt(password, gen_salt('md5'));

ALTER TABLE users DROP COLUMN password;

# --- !Downs
drop extension pgcrypto;