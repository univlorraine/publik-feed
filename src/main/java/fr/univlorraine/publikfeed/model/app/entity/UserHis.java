package fr.univlorraine.publikfeed.model.app.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import fr.univlorraine.publikfeed.converters.LocalDateTimePersistenceConverter;
import lombok.Data;


/**
 * The persistent class for the user_his database table.
 * 
 */
@SuppressWarnings("serial")
@Entity
@Table(name="user_his")
@Data
@NamedQuery(name="UserHis.findAll", query="SELECT u FROM UserHis u")
public class UserHis implements Serializable {


	@Id
	@Column(name = "login")
	private String login;

	@Column(name = "uuid")
	private String uuid;

	@Column(name = "data")
	private String data;
	
	@Convert(converter = LocalDateTimePersistenceConverter.class)
	@Column(name = "dat_maj")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss")
	private LocalDateTime datMaj;
	
	@Convert(converter = LocalDateTimePersistenceConverter.class)
	@Column(name = "dat_sup")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd@HH:mm:ss")
	private LocalDateTime datSup;

	/**
     * cf. https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
	@Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof UserHis)) {
            return false;
        }
        UserHis other = (UserHis) obj;
        return login != null && login.equals(other.getLogin());
    }
	
	/**
     * cf. https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash(getLogin());
    }

}