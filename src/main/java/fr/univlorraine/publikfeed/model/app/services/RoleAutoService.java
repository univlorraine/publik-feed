package fr.univlorraine.publikfeed.model.app.services;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fr.univlorraine.publikfeed.model.app.entity.RoleAuto;
import fr.univlorraine.publikfeed.model.app.entity.UserRole;
import fr.univlorraine.publikfeed.model.app.entity.UserRolePK;
import fr.univlorraine.publikfeed.model.app.repository.RoleAutoRepository;
import fr.univlorraine.publikfeed.model.app.repository.UserRoleRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Transactional
@Data
@Slf4j
@SuppressWarnings("serial")
public class RoleAutoService implements Serializable {

	/* Injections */
	@Resource
	private transient RoleAutoRepository roleAutoRepository;
	
	@Resource
	private transient UserRoleRepository userRoleRepository;


	public RoleAuto saveRole(final RoleAuto role) {
		return roleAutoRepository.save(role);
	}
	
	public Optional<RoleAuto> findRole(final String id) {
		return roleAutoRepository.findById(id);
	}

	public Optional<UserRole> findUserRole(String login, String roleId) {
		UserRolePK urpk = new UserRolePK();
		urpk.setLogin(login);
		urpk.setRoleId(roleId);
		return userRoleRepository.findById(urpk);
	}

	public UserRole saveUserRole(UserRole ur) {
		return userRoleRepository.save(ur);
	}

	public List<UserRole> findRolesFromLogin(String login) {
		return userRoleRepository.findByIdLogin(login);
	}
	
	
}
