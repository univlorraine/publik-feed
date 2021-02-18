package fr.univlorraine.publikfeed.converters;

import java.sql.Date;
import java.time.LocalDate;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/** La class converter LocalDate et Date
 * 
 * @author Kevin Hergalant */
@Converter(autoApply = true)
public class LocalDatePersistenceConverter implements AttributeConverter<LocalDate, Date> {

	@Override
	public Date convertToDatabaseColumn(final LocalDate entityValue) {
		if (entityValue == null) {
			return null;
		}
		return Date.valueOf(entityValue);
	}

	@Override
	public LocalDate convertToEntityAttribute(final Date databaseValue) {
		if (databaseValue == null) {
			return null;
		}
		return databaseValue.toLocalDate();
	}
}
