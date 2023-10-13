create table pgsession(
    id text PRIMARY KEY,
    creationTime timestamp NOT NULL,
    lastAccessedTime timestamp NOT NULL,
    maxInactiveInterval int default 86400 
);

create table pgsession_attribute(
    idsession text FOREIGN KEY pgsession(id),
    attr_name VARCHAR(20) UNIQUE,
    attr_value text
);