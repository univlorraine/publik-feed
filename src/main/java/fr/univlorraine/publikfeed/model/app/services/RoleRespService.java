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
package fr.univlorraine.publikfeed.model.app.services;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fr.univlorraine.publikfeed.model.app.entity.RoleManuel;
import fr.univlorraine.publikfeed.model.app.entity.RoleResp;
import fr.univlorraine.publikfeed.model.app.entity.UserHis;
import fr.univlorraine.publikfeed.model.app.repository.RoleRespRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Transactional
@Data
@Slf4j
@SuppressWarnings("serial")
public class RoleRespService implements Serializable {

	/* Injections */
	@Resource
	private transient RoleRespRepository roleRespRepository;
	



	public RoleResp saveRole(final RoleResp role) {
		return roleRespRepository.save(role);
	}
	
	public Optional<RoleResp> findRole(final String id) {
		return roleRespRepository.findById(id);
	}

	public List<RoleResp> findActive() {
		return roleRespRepository.findByDatSupNull();
	}

	public List<RoleResp> findInactiveAndInPublik() {
		return roleRespRepository.findByDatSupNotNullAndUuidNotNullAndDatSupPublikNull();
	}

	public List<RoleResp> findAll() {
		return roleRespRepository.findAll();
	}

	public List<RoleResp> findFor(String search) {
		return roleRespRepository.findByLibelleContainingIgnoreCaseOrCodStrContainingIgnoreCase(search, search);
	}
	
	public RoleResp updateLoginsDefaut(RoleResp r, String loginsDefaut) {
		Optional<RoleResp> role = findRole(r.getCodStr());
		if(role.isPresent()) {
			role.get().setLoginsDefaut(StringUtils.hasText(loginsDefaut)? loginsDefaut : null);
			role.get().setDatMaj(LocalDateTime.now());
			r = saveRole(role.get());
		}
		return r;
	}


	
	
}
