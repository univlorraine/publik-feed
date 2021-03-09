-- --------------------------------------------------------
--   AJOUT ROLE RESPONSABLE
-- --------------------------------------------------------

 CREATE TABLE role_resp (
 	cod_str varchar(10) NOT NULL,
 	logins text NULL,
 	uuid varchar(50),
 	slug varchar(50) NULL,
 	ou varchar(50) NULL,
 	hash text DEFAULT NULL,
 	dat_maj timestamp NULL,
 	dat_sup timestamp NULL,
 	dat_cre_publik timestamp NULL,
 	dat_maj_publik timestamp NULL,
 	dat_sup_publik timestamp NULL,
 	CONSTRAINT role_resp_pkey PRIMARY KEY (cod_str)
 );