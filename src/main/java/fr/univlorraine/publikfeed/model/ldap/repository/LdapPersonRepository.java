package fr.univlorraine.publikfeed.model.ldap.repository;

import org.springframework.data.ldap.repository.LdapRepository;

import fr.univlorraine.publikfeed.model.ldap.entity.LdapPerson;

public interface LdapPersonRepository extends LdapRepository<LdapPerson> {

	LdapPerson findByUid(String uid);

}
