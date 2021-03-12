package fr.univlorraine.publikfeed.model.app.services;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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


	
	
}
