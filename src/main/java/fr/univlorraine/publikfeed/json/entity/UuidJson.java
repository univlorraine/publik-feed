package fr.univlorraine.publikfeed.json.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class UuidJson {

	@JsonProperty("uuid")
	private String uuid; 
	
}
