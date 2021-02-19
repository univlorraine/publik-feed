package fr.univlorraine.publikfeed.publik.entity;

import java.util.List;

import lombok.Data;

@Data
public class UserResponsePublikApi {

	private String next;
	private String previous;
	private List<UserPublikApi> results;
}
