package dev.dsf.common.auth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RoleConfig
{
	public class Mapping
	{
		private final String name;
		private final List<String> thumbprints = new ArrayList<>();
		private final List<String> emails = new ArrayList<>();
		private final List<String> claims = new ArrayList<>();
		private final List<Role> roles = new ArrayList<>();

		private Mapping(String name, List<String> thumbprints, List<String> emails, List<String> claims,
				List<Role> roles)
		{
			this.name = name;
			if (thumbprints != null)
				this.thumbprints.addAll(thumbprints);
			if (emails != null)
				this.emails.addAll(emails);
			if (claims != null)
				this.claims.addAll(claims);
			if (roles != null)
				this.roles.addAll(roles);
		}

		public String getName()
		{
			return name;
		}

		public List<String> getThumbprints()
		{
			return Collections.unmodifiableList(thumbprints);
		}

		public List<String> getEmails()
		{
			return Collections.unmodifiableList(emails);
		}

		public List<String> getClaims()
		{
			return Collections.unmodifiableList(claims);
		}

		public List<Role> getRoles()
		{
			return Collections.unmodifiableList(roles);
		}

		@Override
		public String toString()
		{
			return "[name=" + name + ", thumbprints=" + thumbprints + ", emails=" + emails + ", claims=" + claims
					+ ", roles=" + roles + "]";
		}
	}

	private final List<Mapping> entries = new ArrayList<>();

	/**
	 * @param config
	 *            parsed yaml
	 * @param roleFactory
	 *            factory should return <code>null</code> if the given string does not represent a valid role
	 */
	public RoleConfig(Object config, Function<String, Role> roleFactory)
	{
		if (config != null && config instanceof List)
		{
			@SuppressWarnings("unchecked")
			List<Object> cList = (List<Object>) config;
			cList.forEach(mapping ->
			{
				if (mapping != null && mapping instanceof Map)
				{
					@SuppressWarnings("unchecked")
					Map<Object, Object> m = (Map<Object, Object>) mapping;
					m.forEach((mappingKey, mappingValues) ->
					{
						if (mappingKey != null && mappingKey instanceof String && mappingValues != null
								&& mappingValues instanceof Map)
						{
							@SuppressWarnings("unchecked")
							Map<Object, Object> properties = (Map<Object, Object>) mappingValues;

							// Map<String, Object>
							List<String> thumbprints = null, emails = null, claims = null;
							List<Role> roles = null;
							for (Entry<Object, Object> p : properties.entrySet())
							{
								if (p.getKey() != null && p.getKey() instanceof String)
								{
									switch ((String) p.getKey())
									{
										case "thumbprint":
											thumbprints = getValues(p.getValue());
											break;
										case "email":
											emails = getValues(p.getValue());
											break;
										case "claim":
											claims = getValues(p.getValue());
											break;
										case "role":
											roles = getValues(p.getValue()).stream().map(roleFactory)
													.filter(r -> r != null).toList();
											break;
									}
								}
							}
							entries.add(new Mapping((String) mappingKey, thumbprints, emails, claims, roles));
						}
					});
				}
			});
		}
	}

	@SuppressWarnings("unchecked")
	private static List<String> getValues(Object o)
	{
		if (o instanceof String)
			return Collections.singletonList((String) o);
		else if (o instanceof List)
			return ((List<String>) o);
		else
			return Collections.emptyList();
	}

	public List<Mapping> getEntries()
	{
		return Collections.unmodifiableList(entries);
	}

	public List<Role> getRolesForThumbprint(String thumbprint)
	{
		return getRoleFor(Mapping::getThumbprints, thumbprint);
	}

	public List<Role> getRolesForEmail(String email)
	{
		return getRoleFor(Mapping::getEmails, email);
	}

	public List<Role> getRolesForClaim(String claim)
	{
		return getRoleFor(Mapping::getClaims, claim);
	}

	private List<Role> getRoleFor(Function<Mapping, List<String>> values, String value)
	{
		return getEntries().stream().filter(m -> values.apply(m).contains(value)).flatMap(m -> m.getRoles().stream())
				.toList();
	}

	@Override
	public String toString()
	{
		return getEntries().stream().map(Mapping::toString).collect(Collectors.joining(", ", "{", "}"));
	}
}
