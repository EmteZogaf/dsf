package dev.dsf.fhir.search.parameters.basic;

import java.sql.Array;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hl7.fhir.r4.model.Resource;

import dev.dsf.fhir.function.BiFunctionWithSqlException;
import dev.dsf.fhir.search.SearchQueryParameterError;
import dev.dsf.fhir.search.SearchQueryParameterError.SearchQueryParameterErrorType;

public abstract class AbstractDateTimeParameter<R extends Resource> extends AbstractSearchParameter<R>
{
	public static enum DateTimeSearchType
	{
		EQ("eq", "="), NE("ne", "<>"), GT("gt", ">"), LT("lt", "<"), GE("ge", ">="), LE("le", "<=");

		public final String prefix;
		public final String operator;

		private DateTimeSearchType(String prefix, String operator)
		{
			this.prefix = prefix;
			this.operator = operator;
		}
	}

	protected static enum DateTimeType
	{
		ZONED_DATE_TIME, LOCAL_DATE, YEAR_PERIOD, YEAR_MONTH_PERIOD;
	}

	protected static class DateTimeValueAndTypeAndSearchType
	{
		public final Object value;
		public final DateTimeType type;
		public final DateTimeSearchType searchType;

		public DateTimeValueAndTypeAndSearchType(Object value, DateTimeType type, DateTimeSearchType searchType)
		{
			this.value = value;
			this.type = type;
			this.searchType = searchType;
		}
	}

	protected static class LocalDatePair
	{
		public final LocalDate startInclusive;
		public final LocalDate endExclusive;

		private LocalDatePair(LocalDate startInclusive, LocalDate endExclusive)
		{
			this.startInclusive = startInclusive;
			this.endExclusive = endExclusive;
		}

		@Override
		public String toString()
		{
			return ">= " + startInclusive + " && < " + endExclusive;
		}
	}

	private static final Pattern YEAR_PATTERN = Pattern.compile("[0-9]{4}");
	private static final Pattern YEAR_MONTH_PATTERN = Pattern.compile("([0-9]{4})-([0-9]{2})");
	private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ISO_DATE_TIME;
	private static final DateTimeFormatter DATE_TIME_FORMAT_OUT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
	private static final DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("yyyy");
	private static final DateTimeFormatter YEAR_MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM");

	protected DateTimeValueAndTypeAndSearchType valueAndType;

	private final String timestampColumn;

	public AbstractDateTimeParameter(String parameterName, String timestampColumn)
	{
		super(parameterName);

		this.timestampColumn = timestampColumn;
	}

	@Override
	protected void doConfigure(List<? super SearchQueryParameterError> errors, String queryParameterName,
			String queryParameterValue)
	{
		valueAndType = parse(errors, queryParameterValue);
	}

	private DateTimeValueAndTypeAndSearchType parse(List<? super SearchQueryParameterError> errors,
			String parameterValue)
	{
		final String fixedParameterValue = parameterValue.replace(' ', '+');

		if (Arrays.stream(DateTimeSearchType.values()).map(t -> t.prefix)
				.anyMatch(prefix -> fixedParameterValue.toLowerCase().startsWith(prefix)))
		{
			String prefix = fixedParameterValue.substring(0, 2);
			String value = fixedParameterValue.substring(2, fixedParameterValue.length()).toUpperCase();
			return parseValue(errors, value, DateTimeSearchType.valueOf(prefix.toUpperCase()), fixedParameterValue);
		}
		else
			return parseValue(errors, fixedParameterValue, DateTimeSearchType.EQ, fixedParameterValue);
	}

	// yyyy-mm-ddThh:mm:ss[Z|(+|-)hh:mm]
	private DateTimeValueAndTypeAndSearchType parseValue(List<? super SearchQueryParameterError> errors, String value,
			DateTimeSearchType searchType, String parameterValue)
	{
		try
		{
			// TODO fix control flow by exception
			return new DateTimeValueAndTypeAndSearchType(ZonedDateTime.parse(value, DATE_TIME_FORMAT),
					DateTimeType.ZONED_DATE_TIME, searchType);
		}
		catch (DateTimeParseException e)
		{
			// not a date-time, ignore
		}

		try
		{
			// TODO fix control flow by exception
			return new DateTimeValueAndTypeAndSearchType(
					ZonedDateTime.parse(value, DATE_TIME_FORMAT.withZone(ZoneId.systemDefault())),
					DateTimeType.ZONED_DATE_TIME, searchType);
		}
		catch (DateTimeParseException e)
		{
			// not a date-time, ignore
		}

		try
		{
			// TODO fix control flow by exception
			return new DateTimeValueAndTypeAndSearchType(LocalDate.parse(value, DATE_FORMAT), DateTimeType.LOCAL_DATE,
					searchType);
		}
		catch (DateTimeParseException e)
		{
			// not a date, ignore
		}

		if (DateTimeSearchType.EQ.equals(searchType))
		{
			Matcher yearMonthMatcher = YEAR_MONTH_PATTERN.matcher(value);
			if (yearMonthMatcher.matches())
			{
				int year = Integer.parseInt(yearMonthMatcher.group(1));
				int month = Integer.parseInt(yearMonthMatcher.group(2));
				return new DateTimeValueAndTypeAndSearchType(
						new LocalDatePair(LocalDate.of(year, month, 1), LocalDate.of(year, month, 1).plusMonths(1)),
						DateTimeType.YEAR_MONTH_PERIOD, DateTimeSearchType.EQ);
			}

			Matcher yearMatcher = YEAR_PATTERN.matcher(value);
			if (yearMatcher.matches())
			{
				int year = Integer.parseInt(yearMatcher.group());
				return new DateTimeValueAndTypeAndSearchType(
						new LocalDatePair(LocalDate.of(year, 1, 1), LocalDate.of(year, 1, 1).plusYears(1)),
						DateTimeType.YEAR_PERIOD, DateTimeSearchType.EQ);
			}
		}

		errors.add(new SearchQueryParameterError(SearchQueryParameterErrorType.UNPARSABLE_VALUE, parameterName,
				parameterValue, parameterValue + " not parsable"));
		return null;
	}

	@Override
	public boolean isDefined()
	{
		return valueAndType != null;
	}

	@Override
	public String getBundleUriQueryParameterName()
	{
		return parameterName;
	}

	@Override
	public String getBundleUriQueryParameterValue()
	{
		return valueAndType.searchType.prefix + toUrlValue(valueAndType);
	}

	protected final String toUrlValue(DateTimeValueAndTypeAndSearchType value)
	{
		return switch (value.type)
		{
			case ZONED_DATE_TIME -> ((ZonedDateTime) value.value).format(DATE_TIME_FORMAT_OUT);
			case LOCAL_DATE -> ((LocalDate) value.value).format(DATE_FORMAT);
			case YEAR_PERIOD -> ((LocalDatePair) value.value).startInclusive.format(YEAR_FORMAT);
			case YEAR_MONTH_PERIOD -> ((LocalDatePair) value.value).startInclusive.format(YEAR_MONTH_FORMAT);
			default -> throw new IllegalArgumentException(
					"Unexpected " + DateTimeType.class.getName() + " value: " + value.type);
		};
	}

	@Override
	public String getFilterQuery()
	{
		return switch (valueAndType.type)
		{
			case ZONED_DATE_TIME -> getDateTimeQuery(valueAndType.searchType.operator);
			case LOCAL_DATE -> getDateQuery(valueAndType.searchType.operator);
			case YEAR_MONTH_PERIOD, YEAR_PERIOD -> getDatePairQuery();
			default -> throw new IllegalArgumentException(
					"Unexpected " + DateTimeType.class.getName() + " value: " + valueAndType.type);
		};
	}

	private String getDateTimeQuery(String operator)
	{
		return "(" + timestampColumn + ")::timestamp " + operator + " ?";
	}

	private String getDateQuery(String operator)
	{
		return "(" + timestampColumn + ")::date " + operator + " ?";
	}

	private String getDatePairQuery()
	{
		return getDateQuery(DateTimeSearchType.GE.operator) + " AND " + getDateQuery(DateTimeSearchType.LT.operator);
	}

	@Override
	public int getSqlParameterCount()
	{
		return switch (valueAndType.type)
		{
			case ZONED_DATE_TIME, LOCAL_DATE -> 1;
			case YEAR_MONTH_PERIOD, YEAR_PERIOD -> 2;
			default -> throw new IllegalArgumentException(
					"Unexpected " + DateTimeType.class.getName() + " value: " + valueAndType.type);
		};
	}

	@Override
	public void modifyStatement(int parameterIndex, int subqueryParameterIndex, PreparedStatement statement,
			BiFunctionWithSqlException<String, Object[], Array> arrayCreator) throws SQLException
	{
		switch (valueAndType.type)
		{
			case ZONED_DATE_TIME:
				statement.setTimestamp(parameterIndex, Timestamp.valueOf(((ZonedDateTime) valueAndType.value)
						.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()));
				break;
			case LOCAL_DATE:
				statement.setDate(parameterIndex, Date.valueOf((LocalDate) valueAndType.value));
				break;
			case YEAR_MONTH_PERIOD:
			case YEAR_PERIOD:
				if (subqueryParameterIndex == 1)
					statement.setDate(parameterIndex,
							Date.valueOf(((LocalDatePair) valueAndType.value).startInclusive));
				if (subqueryParameterIndex == 2)
					statement.setDate(parameterIndex, Date.valueOf(((LocalDatePair) valueAndType.value).endExclusive));
				break;
			default:
				throw new IllegalArgumentException(
						"Unexpected " + DateTimeType.class.getName() + " value: " + valueAndType.type);
		}
	}

	@Override
	public boolean matches(Resource resource)
	{
		if (!isDefined())
			throw notDefined();

		ZonedDateTime lastUpdated = toZonedDateTime(resource.getMeta().getLastUpdated());
		return lastUpdated != null && matches(lastUpdated, valueAndType);
	}

	private ZonedDateTime toZonedDateTime(java.util.Date date)
	{
		if (date == null)
			return null;

		return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

	private boolean matches(ZonedDateTime lastUpdated, DateTimeValueAndTypeAndSearchType value)
	{
		switch (value.type)
		{
			case ZONED_DATE_TIME:
				return matches(lastUpdated, (ZonedDateTime) value.value, value.searchType);
			case LOCAL_DATE:
				return matches(lastUpdated.toLocalDate(), (LocalDate) value.value, value.searchType);
			case YEAR_MONTH_PERIOD:
			case YEAR_PERIOD:
				return matches(lastUpdated.toLocalDate(), (LocalDatePair) value.value);
			default:
				throw notDefined();
		}
	}

	private boolean matches(ZonedDateTime lastUpdated, ZonedDateTime value, DateTimeSearchType type)
	{
		switch (type)
		{
			case EQ:
				return lastUpdated.equals(value);
			case GT:
				return lastUpdated.isAfter(value);
			case GE:
				return lastUpdated.isAfter(value) || lastUpdated.equals(value);
			case LT:
				return lastUpdated.isBefore(value);
			case LE:
				return lastUpdated.isBefore(value) || lastUpdated.equals(value);
			case NE:
				return !lastUpdated.isEqual(value);
			default:
				throw notDefined();
		}
	}

	private boolean matches(LocalDate lastUpdated, LocalDate value, DateTimeSearchType type)
	{
		switch (type)
		{
			case EQ:
				return lastUpdated.equals(value);
			case GT:
				return lastUpdated.isAfter(value);
			case GE:
				return lastUpdated.isAfter(value) || lastUpdated.equals(value);
			case LT:
				return lastUpdated.isBefore(value);
			case LE:
				return lastUpdated.isBefore(value) || lastUpdated.equals(value);
			case NE:
				return !lastUpdated.isEqual(value);
			default:
				throw notDefined();
		}
	}

	private boolean matches(LocalDate lastUpdated, LocalDatePair value)
	{
		return (lastUpdated.isAfter(value.startInclusive) || lastUpdated.isEqual(value.startInclusive))
				&& lastUpdated.isBefore(value.endExclusive);
	}

	@Override
	protected String getSortSql(String sortDirectionWithSpacePrefix)
	{
		return "(" + timestampColumn + ")::timestamp" + sortDirectionWithSpacePrefix;
	}
}
