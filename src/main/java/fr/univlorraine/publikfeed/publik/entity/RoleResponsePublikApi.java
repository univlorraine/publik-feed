package fr.univlorraine.publikfeed.publik.entity;

import java.util.List;

import lombok.Data;

@Data
public class RoleResponsePublikApi {
	private int count;
	private String next;
	private String previous;
	private List<RolePublikApi> results;
}
