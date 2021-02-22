package fr.univlorraine.publikfeed.model.app.services;

import java.io.Serializable;
import java.util.Optional;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fr.univlorraine.publikfeed.model.app.entity.Role;
import fr.univlorraine.publikfeed.model.app.entity.UserRole;
import fr.univlorraine.publikfeed.model.app.entity.UserRolePK;
import fr.univlorraine.publikfeed.model.app.repository.RoleRepository;
import fr.univlorraine.publikfeed.model.app.repository.UserRoleRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Transactional
@Data
@Slf4j
@SuppressWarnings("serial")
public class RoleService implements Serializable {

	/* Injections */
	@Resource
	private transient RoleRepository roleRepository;
	
	@Resource
	private transient UserRoleRepository userRoleRepository;


	public Role saveRole(final Role role) {
		return roleRepository.save(role);
	}
	
	public Optional<Role> findRole(final String id) {
		return roleRepository.findById(id);
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
	
	
}
