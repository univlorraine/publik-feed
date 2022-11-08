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
