package fr.univlorraine.publikfeed.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import fr.univlorraine.publikfeed.model.app.repository.UtilisateurRepository;
import fr.univlorraine.publikfeed.model.app.entity.Utilisateur;

@Service
public class AppUserDetailsService implements UserDetailsService {

	public static final String ROLE_USER = "ROLE_USER";
	public static final String ROLE_SUPERADMIN = "ROLE_SUPERADMIN";

	@Autowired
	private transient UtilisateurRepository utilisateurRepository;

	@Value("${app.superadmins:}")
	private transient List<String> superAdmins;

	@Transactional
	@Override
	public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
		Assert.notNull(username, "Le nom d'utilisateur ne doit pas être nul.");

		Utilisateur utilisateur = utilisateurRepository.findById(username)
			/* Si l'utilisateur existe, met à jour la date de dernière connexion */
			.map(existingUtilisateur -> {
				utilisateurRepository.updateLastLogin(username);
				return existingUtilisateur;
			})
			/* Si l'utilisateur est un super admin qui n'existe pas dans l'application, crée un Utilisateur */
			.or(() -> Optional.of(username)
				.filter(superAdmins::contains)
				.map(this::newSuperAdminUtilisateur))
			.orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé."));

		/* Peuple les droits */
		if (superAdmins.contains(username)) {
			utilisateur.getAuthorities().add(new SimpleGrantedAuthority(ROLE_SUPERADMIN));
		}
		utilisateur.getAuthorities().add(new SimpleGrantedAuthority(ROLE_USER));

		return utilisateur;
	}

	private Utilisateur newSuperAdminUtilisateur(final String username) {
		Utilisateur superAdmin = new Utilisateur();
		superAdmin.setUsername(username);
		superAdmin.setLastLogin(LocalDateTime.now());
		return superAdmin;
	}

}
