package fr.univlorraine.publikfeed.model.app.services;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fr.univlorraine.publikfeed.model.app.entity.UserErrHis;
import fr.univlorraine.publikfeed.model.app.entity.UserHis;
import fr.univlorraine.publikfeed.model.app.repository.UserErrHisRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Transactional
@Data
@Slf4j
@SuppressWarnings("serial")
public class UserErrHisService implements Serializable {

	/* Injections */
	@Resource
	private transient UserErrHisRepository userErrHisRepository;


	public UserErrHis save(final UserErrHis erreur) {
		return userErrHisRepository.save(erreur);
	}
	
	public List<UserErrHis> find(final String login) {
		return userErrHisRepository.findAllByLogin(login);
	}

	public List<String> getUserToRetry() {
		return userErrHisRepository.getLoginToRetry();
	}

	public List<UserErrHis> findAll() {
		return userErrHisRepository.findAllByOrderByDatErrDesc();
	}
	public List<UserErrHis> findAllWithLimit() {
		Pageable limit = PageRequest.of(0,200);
		Page<UserErrHis> page = userErrHisRepository.findAll(limit);
		return page.toList();
		
	}

	public List<UserErrHis> findFor(String search) {
		return userErrHisRepository.findByLoginContainingIgnoreCaseOrTraceContainingIgnoreCase(search, search);
	}
	
	
	
}
