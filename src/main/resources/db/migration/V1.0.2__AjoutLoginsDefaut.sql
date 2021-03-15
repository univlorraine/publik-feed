-- --------------------------------------------------------
--   AJOUT ROLE RESPONSABLE
-- --------------------------------------------------------

ALTER TABLE role_manuel ADD COLUMN logins_defaut text NULL AFTER logins;

ALTER TABLE role_resp ADD COLUMN logins_defaut text NULL AFTER logins;