package dev.dsf.fhir.webservice.impl;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.apache.commons.codec.binary.Hex;

import ca.uhn.fhir.rest.api.Constants;
import dev.dsf.fhir.webservice.base.AbstractBasicService;
import dev.dsf.fhir.webservice.specification.StaticResourcesService;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.UriInfo;

public class StaticResourcesServiceImpl extends AbstractBasicService implements StaticResourcesService
{
	private static CacheControl NO_TRANSFORM = new CacheControl();
	private static CacheControl NO_CACHE_NO_TRANSFORM = new CacheControl();
	static
	{
		// no-transform set by default
		NO_CACHE_NO_TRANSFORM.setNoCache(true);
	}

	private static final class CacheEntry
	{
		private final byte[] data;
		private final byte[] hash;
		private final String mimeType;

		CacheEntry(byte[] data, byte[] hash, String mimeType)
		{
			this.data = data;
			this.hash = hash;
			this.mimeType = mimeType;
		}

		byte[] getData()
		{
			return data;
		}

		EntityTag getTag()
		{
			return new EntityTag(Hex.encodeHexString(hash));
		}

		String getMimeType()
		{
			return mimeType;
		}
	}

	private static abstract class AbstractCache
	{
		abstract Optional<CacheEntry> get(String fileName);

		protected CacheEntry read(InputStream stream, String fileName) throws IOException
		{
			byte[] data = stream.readAllBytes();
			byte[] hash = hash(data);
			String mimeType = mimeType(fileName);

			return new CacheEntry(data, hash, mimeType);
		}

		private byte[] hash(byte[] data)
		{
			try
			{
				MessageDigest digest = MessageDigest.getInstance("MD5");
				return digest.digest(data);
			}
			catch (NoSuchAlgorithmException e)
			{
				throw new RuntimeException(e);
			}
		}

		private String mimeType(String fileName)
		{
			String[] parts = fileName.split("\\.");
			return MIME_TYPE_BY_SUFFIX.get(parts[parts.length - 1]);
		}
	}

	private static final class Cache extends AbstractCache
	{
		private final Map<String, SoftReference<CacheEntry>> entries = new HashMap<>();

		@Override
		Optional<CacheEntry> get(String fileName)
		{
			SoftReference<CacheEntry> entry = entries.get(fileName);
			if (entry == null || entry.get() == null)
				return read(fileName);
			else
				return Optional.of(entry.get());
		}

		Optional<CacheEntry> read(String fileName)
		{
			try (InputStream stream = StaticResourcesServiceImpl.class.getResourceAsStream("/static/" + fileName))
			{
				if (stream == null)
					return Optional.empty();
				else
				{
					CacheEntry entry = read(stream, fileName);
					entries.put(fileName, new SoftReference<>(entry));
					return Optional.of(entry);
				}
			}
			catch (IOException e)
			{
				throw new WebApplicationException(e);
			}
		}
	}

	private static final class NoCache extends AbstractCache
	{
		@Override
		Optional<CacheEntry> get(String fileName)
		{
			try (InputStream stream = StaticResourcesServiceImpl.class.getResourceAsStream("/static/" + fileName))
			{
				if (stream == null)
					return Optional.empty();
				else
					return Optional.of(read(stream, fileName));
			}
			catch (IOException e)
			{
				throw new WebApplicationException(e);
			}
		}
	}

	private static final Map<String, String> MIME_TYPE_BY_SUFFIX = Map.of("css", "text/css", "js", "text/javascript",
			"html", "text/html", "pdf", "application/pdf", "png", "image/png", "svg", "image/svg+xml", "jpg",
			"image/jpeg");

	private final AbstractCache cache;
	private final CacheControl cacheControl;

	public StaticResourcesServiceImpl(boolean cacheEnabled)
	{
		cache = cacheEnabled ? new Cache() : new NoCache();
		cacheControl = cacheEnabled ? NO_TRANSFORM : NO_CACHE_NO_TRANSFORM;
	}

	@Override
	public Response getFile(String fileName, UriInfo uri, HttpHeaders headers)
	{
		if (fileName == null || fileName.isBlank())
			return Response.status(Status.NOT_FOUND).build();
		else if (!MIME_TYPE_BY_SUFFIX.keySet().stream().anyMatch(key -> fileName.endsWith(key)))
			return Response.status(Status.NOT_FOUND).build();
		else
		{
			Optional<CacheEntry> entry = cache.get(fileName);
			Optional<String> matchTag = Arrays.asList(Constants.HEADER_IF_NONE_MATCH, Constants.HEADER_IF_NONE_MATCH_LC)
					.stream().map(name -> headers.getHeaderString(name)).filter(h -> h != null).findFirst();

			return entry.map(toNotModifiedOrOkResponse(matchTag.orElse(""))).orElse(Response.status(Status.NOT_FOUND))
					.build();
		}
	}

	private Function<CacheEntry, ResponseBuilder> toNotModifiedOrOkResponse(String matchTag)
	{
		return entry ->
		{
			if (entry.getTag().getValue().equals(matchTag.replace("\"", "")))
				return Response.status(Status.NOT_MODIFIED);
			else
				return Response.ok(entry.getData(), MediaType.valueOf(entry.getMimeType())).tag(entry.getTag())
						.cacheControl(cacheControl);
		};
	}
}
