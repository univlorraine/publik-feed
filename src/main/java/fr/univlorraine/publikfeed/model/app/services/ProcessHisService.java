package fr.univlorraine.publikfeed.model.app.services;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fr.univlorraine.publikfeed.model.app.entity.ProcessHis;
import fr.univlorraine.publikfeed.model.app.entity.ProcessHisPK;
import fr.univlorraine.publikfeed.model.app.repository.ProcessHisRepository;
import fr.univlorraine.publikfeed.utils.JobUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Component
@Transactional
@Data
@Slf4j
@SuppressWarnings("serial")
public class ProcessHisService implements Serializable {

	/* Injections */
	@Resource
	private transient ProcessHisRepository processHisRepository;


	/** @param process
	 *            process to save
	 * @return process saved */
	public ProcessHis update(final ProcessHis process) {
		return processHisRepository.save(process);
	}
	/** @param process
	 *            process to save
	 * @return process saved */
	public ProcessHis end(final ProcessHis process) {
		process.setDatFin(LocalDateTime.now());
		return processHisRepository.saveAndFlush(process);
	}
	
	public ProcessHis getLastSuccessExc(String processName) {
		List<ProcessHis> lph = processHisRepository.findAllByIdCodProcessOrderByDatFinDesc(processName);
		// Si on a récupéré des executions
		if(lph!=null && !lph.isEmpty() ) {
			// On retourne le premier element
			return lph.get(0);
		}
		return null;
	}
	
	public ProcessHis getLastExc(String processName) {
		List<ProcessHis> lph = processHisRepository.findAllByIdCodProcessOrderByIdDatDebDesc(processName);
		// Si on a récupéré des executions
		if(lph!=null && !lph.isEmpty() ) {
			// On retourne le premier element
			return lph.get(0);
		}
		return null;
	}

	public ProcessHis getNewProcess(String processName) {
		ProcessHis rph = new ProcessHis();
		ProcessHisPK rphpk = new ProcessHisPK();
		rphpk.setCodProcess(processName);
		rphpk.setDatDeb(LocalDateTime.now());
		rph.setId(rphpk);
		rph = processHisRepository.save(rph);
		return rph;
	}

	public List<ProcessHis> getListJobs() {
		List<ProcessHis> list = new LinkedList<ProcessHis> ();
		
		// Pour chaque job du projet
		for(String jobName : JobUtils.jobList) {
			//Récupération de la derniere execution
			ProcessHis ph = getLastExc(jobName);
			// Si le job n'a jamais été lancé
			if(ph == null) {
				// Création d'un objet contenant que le nom
				ph = new ProcessHis();
				ProcessHisPK pk = new ProcessHisPK();
				pk.setCodProcess(jobName);
				ph.setId(pk);
			}
			// AJout dans la liste
			list.add(ph);
		}
		
		return list;
	}
}
