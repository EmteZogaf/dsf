package org.highmed.fhir.dao.command;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.IntStream;

import javax.sql.DataSource;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.highmed.fhir.help.ExceptionHandler;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.Bundle.BundleType;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchCommandList implements CommandList
{
	private static final Logger logger = LoggerFactory.getLogger(BatchCommandList.class);

	private final DataSource dataSource;
	private final ExceptionHandler exceptionHandler;

	private final List<Command> commands = new ArrayList<>();

	public BatchCommandList(DataSource dataSource, ExceptionHandler exceptionHandler, List<Command> commands)
	{
		this.dataSource = dataSource;
		this.exceptionHandler = exceptionHandler;

		if (commands != null)
			this.commands.addAll(commands);
	}

	@Override
	public Bundle execute() throws WebApplicationException
	{
		try (Connection connection = dataSource.getConnection())
		{
			if (commands.stream().anyMatch(
					c -> c instanceof CreateCommand || c instanceof UpdateCommand || c instanceof DeleteCommand))
				connection.setReadOnly(false);

			Map<Integer, Exception> caughtExceptions = new HashMap<Integer, Exception>(
					(int) (commands.size() / 0.75) + 1);

			commands.forEach(c -> preExecute(connection, caughtExceptions, c));

			IntStream.range(0, commands.size()).filter(index -> !caughtExceptions.containsKey(index))
					.mapToObj(index -> commands.get(index)).forEach(c -> execute(connection, caughtExceptions, c));

			Map<Integer, BundleEntryComponent> results = new HashMap<>((int) ((commands.size() / 0.75) + 1));

			IntStream.range(0, commands.size()).filter(index -> !caughtExceptions.containsKey(index))
					.mapToObj(index -> commands.get(index))
					.forEach(c -> postExecute(connection, caughtExceptions, c, results));

			Bundle result = new Bundle();
			result.setType(BundleType.BATCHRESPONSE);

			caughtExceptions.forEach((k, v) -> results.put(k, toEntry(v)));
			results.entrySet().stream().sorted(Comparator.comparing(Entry::getKey)).map(Entry::getValue)
					.forEach(result::addEntry);

			return result;
		}
		catch (Exception e)
		{
			throw exceptionHandler.internalServerErrorBundleTransaction(e);
		}
	}

	private BundleEntryComponent toEntry(Exception exception)
	{
		var entry = new BundleEntryComponent();
		var response = entry.getResponse();

		if (!(exception instanceof WebApplicationException)
				|| !(((WebApplicationException) exception).getResponse().getEntity() instanceof OperationOutcome))
		{
			exception = exceptionHandler.internalServerErrorBundleBatch(exception);
		}

		Response httpResponse = ((WebApplicationException) exception).getResponse();
		response.setStatus(
				httpResponse.getStatusInfo().getStatusCode() + " " + httpResponse.getStatusInfo().getReasonPhrase());
		response.setOutcome((OperationOutcome) httpResponse.getEntity());

		return entry;
	}

	private void preExecute(Connection connection, Map<Integer, Exception> caughtExceptions, Command command)
	{
		try
		{
			logger.debug("Running pre-execute of command {}", command.getClass().getName());
			command.preExecute(connection);
		}
		catch (Exception e)
		{
			logger.warn("Error while running pre-execute of command " + command.getClass().getName(), e);
			caughtExceptions.put(command.getIndex(), e);
		}
	}

	private void execute(Connection connection, Map<Integer, Exception> caughtExceptions, Command command)
	{
		try
		{
			logger.debug("Running execute of command {}", command.getClass().getName());
			command.execute(connection);
		}
		catch (Exception e)
		{
			logger.warn("Error while executing command " + command.getClass().getName(), e);
			caughtExceptions.put(command.getIndex(), e);
		}
	}

	private void postExecute(Connection connection, Map<Integer, Exception> caughtExceptions, Command command,
			Map<Integer, BundleEntryComponent> results)
	{
		try
		{
			logger.debug("Running post-execute of command {}", command.getClass().getName());
			results.put(command.getIndex(), command.postExecute(connection));
		}
		catch (Exception e)
		{
			logger.warn("Error while running post-execute of command " + command.getClass().getName(), e);
			caughtExceptions.put(command.getIndex(), e);
		}
	}
}
