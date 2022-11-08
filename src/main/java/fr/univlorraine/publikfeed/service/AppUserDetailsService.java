/**
 *
 * Copyright (c) 2022 Université de Lorraine, 18/02/2021
 *
 * dn-sied-dev@univ-lorraine.fr
 *
 * Ce logiciel est un programme informatique servant à alimenter Publik depuis des groupes LDAP.
 *
 * Ce logiciel est régi par la licence CeCILL 2.1 soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA
 * sur le site "http://www.cecill.info".
 *
 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.
 *
 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement,
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité.
 *
 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez
 * pris connaissance de la licence CeCILL 2.1, et que vous en avez accepté les
 * termes.
 *
 */
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
