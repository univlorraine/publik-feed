package fr.univlorraine.publikfeed.controllers;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

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
	public List<RolePublikApi> getAllRoles(String prefix){
		List<RolePublikApi> listRoles = new LinkedList<RolePublikApi> ();
		RoleResponsePublikApi result = rolePublikApiService.getRoles(null);
		listRoles.addAll(result.getResults());
		while(StringUtils.hasText(result.getNext())) {
			result = rolePublikApiService.getRoles(result.getNext());
			listRoles.addAll(result.getResults());
		}
		// Si un prefix est renseigné
		if(StringUtils.hasText(prefix)) {
			//suppression des roles ne remplissant pas la condition du prefix
			Predicate<RolePublikApi> rolePredicate = r -> !r.getName().startsWith(prefix);
			listRoles.removeIf(rolePredicate);
		}
		return listRoles;
	}
}
