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
import fr.univlorraine.publikfeed.model.app.repository.RoleManuelRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Transactional
@Data
@Slf4j
@SuppressWarnings("serial")
public class RoleManuelService implements Serializable {

	/* Injections */
	@Resource
	private transient RoleManuelRepository roleManuelRepository;
	



	public RoleManuel saveRole(final RoleManuel role) {
		return roleManuelRepository.save(role);
	}
	
	public Optional<RoleManuel> findRole(final String id) {
		return roleManuelRepository.findById(id);
	}

	public List<RoleManuel> findActive() {
		return roleManuelRepository.findByDatSupNull();
	}

	public List<RoleManuel> findInactiveAndInPublik() {
		return roleManuelRepository.findByDatSupNotNullAndUuidNotNullAndDatSupPublikNull();
	}

	public List<RoleManuel> findAll() {
		return roleManuelRepository.findAll();
	}
	
	public List<RoleManuel> findAllOrderByDateMaj() {
		return roleManuelRepository.findAllByOrderByDatMajDesc();
	}
	
	

	public RoleManuel updateLibelle(RoleManuel r, String value) {
		Optional<RoleManuel> role = findRole(r.getId());
		if(role.isPresent()) {
			role.get().setLibelle(value);
			r = saveRole(role.get());
		}
		return r;
	}

	public RoleManuel updateFiltreAndLogins(RoleManuel r, String filtre, String logins, String loginsDefaut, Boolean actif) {
		Optional<RoleManuel> role = findRole(r.getId());
		if(role.isPresent()) {
			role.get().setFiltre(StringUtils.hasText(filtre)? filtre : null);
			role.get().setLogins(StringUtils.hasText(logins)? logins : null);
			role.get().setLoginsDefaut(StringUtils.hasText(loginsDefaut)? loginsDefaut : null);
			role.get().setDatMaj(LocalDateTime.now());
			if(actif) {
				role.get().setDatSup(null);
			}
			if(!actif && role.get().getDatSup()==null) {
				role.get().setDatSup(LocalDateTime.now());
			}
			r = saveRole(role.get());
		}
		return r;
	}


	
	public List<RoleManuel> findFor(String search) {
		return roleManuelRepository.findByLibelleContainingIgnoreCaseOrIdContainingIgnoreCase(search, search);
	}


	
	
}
