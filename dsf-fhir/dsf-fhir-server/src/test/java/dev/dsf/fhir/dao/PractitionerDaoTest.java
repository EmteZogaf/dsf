package dev.dsf.fhir.dao;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.GregorianCalendar;

import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.Test;

import dev.dsf.fhir.dao.jdbc.PractitionerDaoJdbc;

public class PractitionerDaoTest extends AbstractResourceDaoTest<Practitioner, PractitionerDao>
		implements ReadAccessDaoTest<Practitioner>
{
	private final Date birthday = new GregorianCalendar(1980, 0, 2).getTime();
	private final AdministrativeGender gender = AdministrativeGender.FEMALE;

	public PractitionerDaoTest()
	{
		super(Practitioner.class, PractitionerDaoJdbc::new);
	}

	@Override
	public Practitioner createResource()
	{
		Practitioner practitioner = new Practitioner();
		practitioner.setBirthDate(birthday);
		return practitioner;
	}

	@Override
	protected void checkCreated(Practitioner resource)
	{
		assertEquals(birthday, resource.getBirthDate());
	}

	@Override
	protected Practitioner updateResource(Practitioner resource)
	{
		resource.setGender(gender);
		return resource;
	}

	@Override
	protected void checkUpdates(Practitioner resource)
	{
		assertEquals(gender, resource.getGender());
	}

	@Override
	@Test
	public void testReadAccessTriggerAll() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerAll();
	}

	@Override
	@Test
	public void testReadAccessTriggerLocal() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerLocal();
	}

	@Override
	@Test
	public void testReadAccessTriggerOrganization() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerOrganization();
	}

	@Override
	@Test
	public void testReadAccessTriggerOrganizationResourceFirst() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerOrganizationResourceFirst();
	}

	@Override
	@Test
	public void testReadAccessTriggerOrganization2Organizations1Matching() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerOrganization2Organizations1Matching();
	}

	@Override
	@Test
	public void testReadAccessTriggerOrganization2Organizations2Matching() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerOrganization2Organizations2Matching();
	}

	@Override
	@Test
	public void testReadAccessTriggerRole() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRole();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleResourceFirst() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleResourceFirst();
	}

	@Override
	@Test
	public void testReadAccessTriggerRole2Organizations1Matching() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRole2Organizations1Matching();
	}

	@Override
	@Test
	public void testReadAccessTriggerRole2Organizations2Matching() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRole2Organizations2Matching();
	}

	@Override
	@Test
	public void testReadAccessTriggerAllUpdate() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerAllUpdate();
	}

	@Override
	@Test
	public void testReadAccessTriggerLocalUpdate() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerLocalUpdate();
	}

	@Override
	@Test
	public void testReadAccessTriggerOrganizationUpdate() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerOrganizationUpdate();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleUpdate() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleUpdate();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleUpdateMemberOrganizationNonActive() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleUpdateMemberOrganizationNonActive();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleUpdateParentOrganizationNonActive() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleUpdateParentOrganizationNonActive();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleUpdateMemberAndParentOrganizationNonActive() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleUpdateMemberAndParentOrganizationNonActive();
	}

	@Override
	@Test
	public void testReadAccessTriggerAllDelete() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerAllDelete();
	}

	@Override
	@Test
	public void testReadAccessTriggerLocalDelete() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerLocalDelete();
	}

	@Override
	@Test
	public void testReadAccessTriggerOrganizationDelete() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerOrganizationDelete();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleDelete() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleDelete();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleDeleteMember() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleDeleteMember();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleDeleteParent() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleDeleteParent();
	}

	@Override
	@Test
	public void testReadAccessTriggerRoleDeleteMemberAndParent() throws Exception
	{
		ReadAccessDaoTest.super.testReadAccessTriggerRoleDeleteMemberAndParent();
	}

	@Override
	@Test
	public void testSearchWithUserFilterAfterReadAccessTriggerAllWithLocalUser() throws Exception
	{
		ReadAccessDaoTest.super.testSearchWithUserFilterAfterReadAccessTriggerAllWithLocalUser();
	}

	@Override
	@Test
	public void testSearchWithUserFilterAfterReadAccessTriggerLocalwithLocalUser() throws Exception
	{
		ReadAccessDaoTest.super.testSearchWithUserFilterAfterReadAccessTriggerLocalwithLocalUser();
	}

	@Override
	@Test
	public void testSearchWithUserFilterAfterReadAccessTriggerAllWithRemoteUser() throws Exception
	{
		ReadAccessDaoTest.super.testSearchWithUserFilterAfterReadAccessTriggerAllWithRemoteUser();
	}

	@Override
	@Test
	public void testSearchWithUserFilterAfterReadAccessTriggerLocalWithRemoteUser() throws Exception
	{
		ReadAccessDaoTest.super.testSearchWithUserFilterAfterReadAccessTriggerLocalWithRemoteUser();
	}
}
