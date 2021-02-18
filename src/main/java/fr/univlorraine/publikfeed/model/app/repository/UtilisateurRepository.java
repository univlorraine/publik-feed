package fr.univlorraine.publikfeed.model.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import fr.univlorraine.publikfeed.model.app.entity.Utilisateur;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, String> {

	/* nativeQuery permet de ne pas modifier lastModified. */
	@Modifying
	@Transactional
	@Query(nativeQuery = true, value = "update utilisateur set last_login = now() where username = :username")
	void updateLastLogin(String username);

}
