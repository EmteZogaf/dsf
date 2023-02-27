package dev.dsf.fhir.dao.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import javax.sql.DataSource;

import org.hl7.fhir.r4.model.Library;

import ca.uhn.fhir.context.FhirContext;
import dev.dsf.fhir.dao.LibraryDao;
import dev.dsf.fhir.search.parameters.LibraryDate;
import dev.dsf.fhir.search.parameters.LibraryIdentifier;
import dev.dsf.fhir.search.parameters.LibraryStatus;
import dev.dsf.fhir.search.parameters.LibraryUrl;
import dev.dsf.fhir.search.parameters.LibraryVersion;
import dev.dsf.fhir.search.parameters.user.LibraryUserFilter;

public class LibraryDaoJdbc extends AbstractResourceDaoJdbc<Library> implements LibraryDao
{
	private final ReadByUrlDaoJdbc<Library> readByUrl;

	public LibraryDaoJdbc(DataSource dataSource, DataSource permanentDeleteDataSource, FhirContext fhirContext)
	{
		super(dataSource, permanentDeleteDataSource, fhirContext, Library.class, "libraries", "library", "library_id",
				LibraryUserFilter::new, with(LibraryDate::new, LibraryIdentifier::new, LibraryStatus::new,
						LibraryUrl::new, LibraryVersion::new),
				with());

		readByUrl = new ReadByUrlDaoJdbc<>(this::getDataSource, this::getResource, getResourceTable(),
				getResourceColumn());
	}

	@Override
	protected Library copy(Library resource)
	{
		return resource.copy();
	}

	@Override
	public Optional<Library> readByUrlAndVersion(String urlAndVersion) throws SQLException
	{
		return readByUrl.readByUrlAndVersion(urlAndVersion);
	}

	@Override
	public Optional<Library> readByUrlAndVersionWithTransaction(Connection connection, String urlAndVersion)
			throws SQLException
	{
		return readByUrl.readByUrlAndVersionWithTransaction(connection, urlAndVersion);
	}

	@Override
	public Optional<Library> readByUrlAndVersion(String url, String version) throws SQLException
	{
		return readByUrl.readByUrlAndVersion(url, version);
	}

	@Override
	public Optional<Library> readByUrlAndVersionWithTransaction(Connection connection, String url, String version)
			throws SQLException
	{
		return readByUrl.readByUrlAndVersionWithTransaction(connection, url, version);
	}
}
