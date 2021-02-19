package fr.univlorraine.publikfeed.model.app.services;

import java.io.Serializable;
import java.util.Optional;

import javax.annotation.Resource;

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
		return userHisRepository.findById(login);
	}
	
	
}
