package fr.univlorraine.publikfeed.model.app.services;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fr.univlorraine.publikfeed.model.app.entity.RoleManuel;
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
		return roleManuelRepository.findByDatSupNotNull();
	}


	
	
}
