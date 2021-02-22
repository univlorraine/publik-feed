package fr.univlorraine.publikfeed.json.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;


import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class RoleJson {

	@JsonProperty("name")
	private String name;
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("slug")
	private String slug;
	
	@JsonInclude(JsonInclude.Include.NON_NULL)
	@JsonProperty("ou")
	private String ou;
	
}
