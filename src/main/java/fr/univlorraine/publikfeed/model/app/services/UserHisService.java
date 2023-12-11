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
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fr.univlorraine.publikfeed.model.app.entity.UserHis;
import fr.univlorraine.publikfeed.model.app.repository.UserHisRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Transactional
@Data
@Slf4j
@SuppressWarnings("serial")
public class UserHisService implements Serializable {

	/* Injections */
	@Resource
	private transient UserHisRepository userHisRepository;


	public UserHis save(final UserHis user) {
		return userHisRepository.save(user);
	}
	
	public Optional<UserHis> find(final String login) {
		if(login != null) {
			return userHisRepository.findById(login);
		}
		return null;
	}

	public String getUuidFromLogin(String login) {
		if(login != null) {
			Optional<UserHis> u = find(login);
			if(u.isPresent() && u.get().getUuid()!=null) {
				return u.get().getUuid();
			}
		}
		return null;
	}

	public List<UserHis> findAll() {
		return userHisRepository.findAllByOrderByLogin();
	}

	public List<UserHis> findAllWithLimit() {
		Pageable limit = PageRequest.of(0,200);
		Page<UserHis> page = userHisRepository.findAll(limit);
		return page.toList();
		
	}

	public List<UserHis> findFor(String search) {
		return userHisRepository.findByLoginContainingIgnoreCaseOrDataContainingIgnoreCase(search, search);
	}

	public List<UserHis> findAllActiv() {
		return userHisRepository.findAllByDatSupNull();
	}
	
	
}
