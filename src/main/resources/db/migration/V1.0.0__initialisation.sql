CREATE TABLE utilisateur (
  username varchar(10) NOT NULL COMMENT "Nom d'utilisateur",
  version smallint NOT NULL COMMENT "Version de l'entité",
  display_name varchar(64) COMMENT "Nom affiché",
  last_login datetime(6) COMMENT "Date et heure de derniere connexion",
  created_date datetime(6) COMMENT "Date et heure de création",
  created_by varchar(10) COMMENT "Utilisateur ayant effectué la création",
  last_modified_date datetime(6) COMMENT "Date et heure de la dernière modification",
  last_modified_by varchar(10) COMMENT "Utilisateur ayant effectué la dernière modification",
  PRIMARY KEY (username)
) ENGINE = INNODB COMMENT "Utilisateurs";
ALTER TABLE utilisateur ADD CONSTRAINT fk_created_by FOREIGN KEY (created_by) REFERENCES utilisateur (username);
ALTER TABLE utilisateur ADD CONSTRAINT fk_last_modified_by FOREIGN KEY (last_modified_by) REFERENCES utilisateur (username);


CREATE TABLE process_his (
	cod_process varchar(100) NOT NULL,
	dat_deb timestamp NOT NULL DEFAULT current_timestamp(),
  dat_fin timestamp NULL,
  nb_obj_traite DECIMAL,
  nb_obj_total DECIMAL,
  nb_obj_erreur DECIMAL,
CONSTRAINT process_his_pkey PRIMARY KEY (cod_process, dat_deb)
);

CREATE TABLE user_his (
	login varchar(20) NOT NULL,
	uuid varchar(50),
	data JSON NOT NULL,
	dat_maj timestamp NOT NULL,
	dat_sup timestamp NULL,
	CONSTRAINT user_his_pkey PRIMARY KEY (login)
);

CREATE TABLE user_err_his (
  id int(11) NOT NULL AUTO_INCREMENT,
  login varchar(20) NOT NULL,
  dat_err timestamp NOT NULL DEFAULT current_timestamp(),
  trace text DEFAULT NULL,
  CONSTRAINT user_err_his_pkey PRIMARY KEY (id)
);

CREATE TABLE role_auto (
	id varchar(100) NOT NULL,
	uuid varchar(50),
	slug varchar(50) NULL,
	ou varchar(50) NULL,
	hash text DEFAULT NULL,
	dat_maj timestamp NULL,
	dat_sup timestamp NULL,
	CONSTRAINT role_auto_pkey PRIMARY KEY (id)
);

CREATE TABLE user_role (
	role_id varchar(100) NOT NULL,
	login varchar(20) NOT NULL,
	dat_maj timestamp NULL,
	dat_sup timestamp NULL,
	CONSTRAINT user_role_pkey PRIMARY KEY (role_id, login)
);

 CREATE TABLE role_manuel (
 	id varchar(100) NOT NULL,
 	libelle text NULL,
 	logins text NULL,
 	filtre varchar(100) NULL,
 	uuid varchar(50),
 	slug varchar(50) NULL,
 	ou varchar(50) NULL,
 	hash text DEFAULT NULL,
 	dat_maj timestamp NULL,
 	dat_sup timestamp NULL,
 	dat_cre_publik timestamp NULL,
 	dat_maj_publik timestamp NULL,
 	dat_sup_publik timestamp NULL,
 	CONSTRAINT role_manuel_pkey PRIMARY KEY (id)
 );

 -- INSERT INTO `role_manuel` (`id`, `libelle`, `logins`, `filtre`, `uuid`, `slug`, `ou`, `hash`, `dat_maj`, `dat_maj_publik`, `dat_sup`) VALUES ('SOME_DEV', 'test de role manuel', 'dubois1', '(supannEntiteAffectation=*G1NA-)', NULL, NULL, NULL, NULL, '2021-02-23 17:57:20', NULL, NULL); 


