package fr.univlorraine.publikfeed.json.entity;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class ListUuidJson {

	@JsonProperty("data")
	private List<UuidJson> data; 
	
}
