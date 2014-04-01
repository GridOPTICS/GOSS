CREATE TABLE securitypolicy  ( 
    policyId            int(11) AUTO_INCREMENT NOT NULL,
    dataType            varchar(30) NOT NULL,
    source              varchar(15) NOT NULL,
    age                 int(11) NOT NULL,
    accessLevel         varchar(30) NOT NULL,
    roles               varchar(255) NOT NULL,
    operationsAllowed   varchar(255) NULL,
    expiration          datetime NULL,
    PRIMARY KEY(policyId)
);
