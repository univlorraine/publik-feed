package fr.univlorraine.publikfeed.model.app.entity;

import java.time.LocalDateTime;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;

import lombok.Getter;
import lombok.Setter;

/**
 * Superclass d'Entité suivie.
 * @author Adrien Colson
 */
@MappedSuperclass
@Getter
@SuppressWarnings("serial")
public abstract class AbstractAuditedEntity extends AbstractCreationAuditedEntity {

	/** Date de dernière modification. */
	@Setter
	@LastModifiedDate
	private LocalDateTime lastModifiedDate;

	/** Utilisateur qui a fait la dernière modification. */
	@LastModifiedBy
	@ManyToOne(optional = true)
	@JoinColumn(name = "last_modified_by")
	@NotFound(action = NotFoundAction.IGNORE)
	private Utilisateur lastModifiedBy;

}
