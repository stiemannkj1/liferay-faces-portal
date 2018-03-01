/**
 * Copyright (c) 2000-2018 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package com.liferay.faces.aether;

import java.io.File;

import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.version.Version;

import org.junit.Assert;
import org.junit.Test;

import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;


/**
 * @author  Vernon Singleton
 */
public class AetherClientTest {

	// Logger
	private static final Logger logger = LoggerFactory.getLogger(AetherClientTest.class);

	//J-
	// If you would like to test the use of another local repository in your file system, just
	// copy the repository into the root of this project and name it "test-localrepo".
	//
	// If it contains an artifact with a GAV:
	// com.liferay:com.liferay.gradle.plugins.workspace:1.0.1:jar:sources
	// then this test should pass.
	//
	// This project does not need this functionality, so I am commenting this test out.
	//
	// @Test
	//J+
	public void testCheckLatestArchetypeVersionOffline() throws Exception {
		AetherClient client = new AetherClient(null, "test-localrepo");

		File artifact = client.getLatestAvailableArtifact(
				"com.liferay:com.liferay.gradle.plugins.workspace:jar:sources");

		Assert.assertNotNull(artifact);
		Assert.assertTrue(artifact.exists());
		Assert.assertTrue(artifact.getName().startsWith("com.liferay.gradle.plugins.workspace"));
		Assert.assertTrue(artifact.getName().endsWith("sources.jar"));
		Assert.assertTrue(artifact.getPath().contains("test-localrepo"));
		Assert.assertTrue(artifact.getName().contains("1.0.1"));
	}

	@Test
	public void testGetArtifactUsingGAV() throws Exception {

		String groupId = "com.liferay.faces.archetype";
		String suite = "primefaces";
		String version = "5.0.0";
		String groupIdArtifactIdVersion = groupId + ":" + groupId + "." + suite + ".portlet:jar:" + version;

		File artifact = new AetherClient().getArtifact(groupIdArtifactIdVersion);

		logger.info("artifact.getCanonicalPath() =" + artifact.getCanonicalPath());

		Assert.assertNotNull(artifact);
		Assert.assertTrue(artifact.exists());
		Assert.assertTrue(artifact.getName().startsWith("com.liferay.faces.archetype"));
		Assert.assertTrue(artifact.getName().endsWith(".jar"));
		Assert.assertTrue(artifact.getName().contains("5.0.0"));
	}

	@Test
	public void testGetVersionOfLatestMinorFromCentral() throws Exception {
		AetherClient client = new AetherClient(new String[] { "https://repo1.maven.org/maven2/" });

		Version version = client.getVersionOfLatestMinor(
				"com.liferay.faces.archetype:com.liferay.faces.archetype.primefaces.portlet:jar", 5L);

		Assert.assertNotNull(version);
		logger.info("testGetVersionOfLatestMinorFromCentral: version = " + version);
	}

	@Test
	public void testGetVersionOfLatestMinorWithMajorFromString() throws Exception {

		String extVersion = "5.0.0";
		String major = extVersion.replaceAll("\\...*", "");
		logger.info("testGetVersionOfLatestMinorWithMajorFromString: major = " + major);

		String groupId = "com.liferay.faces.archetype";
		String suite = "primefaces";
		String groupIdArtifactId = groupId + ":" + groupId + "." + suite + ".portlet:jar";

		AetherClient client = new AetherClient();

		Version version = client.getVersionOfLatestMinor(groupIdArtifactId, new Long(major));

		Assert.assertNotNull(version);
		logger.info("testGetVersionOfLatestMinorWithMajorFromString: version = " + version);
	}

	@Test
	public void testMissingArtifact() throws Exception {

		String version = "0.0.0";

		String groupId = "com.liferay.faces.archetype";
		String suite = "primefaces";
		String groupIdArtifactIdVersion = groupId + ":" + groupId + "." + suite + ".portlet:jar:" + version;

		AetherClient client = new AetherClient();

		File artifact = null;

		try {
			artifact = client.getArtifact(groupIdArtifactIdVersion);
		}
		catch (ArtifactResolutionException e) {
			logger.info("testMissingArtifact: e.getMessage() = " + e.getMessage());
		}

		Assert.assertTrue(artifact == null);
		logger.info("testMissingArtifact: artifact = " + artifact);
	}

}
