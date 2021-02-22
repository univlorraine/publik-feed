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
	CONSTRAINT user_his_pkey PRIMARY KEY (login)
);

CREATE TABLE user_err_his (
  id int(11) NOT NULL AUTO_INCREMENT,
  login varchar(20) NOT NULL,
  dat_err timestamp NOT NULL DEFAULT current_timestamp(),
  trace text DEFAULT NULL,
  CONSTRAINT user_err_his_pkey PRIMARY KEY (id)
)