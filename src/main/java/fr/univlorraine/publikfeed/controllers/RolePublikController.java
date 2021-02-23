package fr.univlorraine.publikfeed.controllers;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.stereotype.Component;

import fr.univlorraine.publikfeed.publik.entity.RolePublikApi;
import fr.univlorraine.publikfeed.publik.entity.RoleResponsePublikApi;
import fr.univlorraine.publikfeed.publik.services.RolePublikApiService;
import lombok.extern.slf4j.Slf4j;

@Component(value="rolePublikController")
@Slf4j
public class RolePublikController {


	@Resource
	private RolePublikApiService rolePublikApiService;
	
	/**
	 * Retourne tous les roles présents dans Publik
	 * @return
	 */
	public List<RolePublikApi> getAllRoles(){
		List<RolePublikApi> listRoles = new LinkedList<RolePublikApi> ();
		RoleResponsePublikApi result = rolePublikApiService.getRoles(null);
		listRoles.addAll(result.getResults());
		while(StringUtils.hasText(result.getNext())) {
			result = rolePublikApiService.getRoles(result.getNext());
			listRoles.addAll(result.getResults());
		}
		
		return listRoles;
	}
}
