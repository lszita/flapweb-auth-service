DROP TABLE users;
CREATE TABLE users
(
    id identity,
    username varchar(20) NOT NULL,
    password varchar(60) NOT NULL,
    email_address varchar(30)  NOT NULL,
    active boolean NOT NULL DEFAULT false,
    creation_date date DEFAULT CURRENT_TIMESTAMP(),
    CONSTRAINT id_pkc PRIMARY KEY (id),
    CONSTRAINT email_address_uc UNIQUE (email_address),
    CONSTRAINT username_uc UNIQUE (username)
);