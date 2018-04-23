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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilder;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingRequest;

import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.version.Version;

import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;


/**
 * @author  Gregory Amerson
 * @author  Vernon Singleton
 */
public class AetherClient {

	// Logger
	private static final Logger logger = LoggerFactory.getLogger(AetherClient.class);

	// Public Constants
	public static final String MAVEN_CENTRAL_URL = "https://repo1.maven.org/maven2";
	public static final String SONATYPE_SNAPSHOT_URL = "https://oss.sonatype.org/content/repositories/snapshots";
	public static final String PUBLIC_LIFERAY_URL = "http://repository.liferay.com/nexus/content/groups/public/";

	// Private Constants
	private static final File DEFAULT_GLOBAL_SETTINGS_FILE = new File(System.getProperty("maven.home",
				System.getProperty("user.dir", "")), "conf/settings.xml");
	private static final String USER_HOME = System.getProperty("user.home");
	private static final File USER_MAVEN_CONFIGURATION_HOME = new File(USER_HOME, ".m2");
	private static final File USER_MAVEN_DEFAULT_USER_SETTINGS_FILE = new File(USER_MAVEN_CONFIGURATION_HOME,
			"settings.xml");
	private static final String[] DEFAULT_REPO_URLS = { MAVEN_CENTRAL_URL };

	// Private Final Data Members
	private final String localRepositoryPath;
	private final String[] repoUrls;

	public AetherClient() {
		this(DEFAULT_REPO_URLS);
	}

	public AetherClient(String[] repoUrls) {
		this(repoUrls, lookupLocalRepoDir().getPath());
	}

	public AetherClient(String[] repoUrls, String localRepositoryPath) {

		this.repoUrls = (repoUrls == null) ? new String[0] : repoUrls;
		this.localRepositoryPath = localRepositoryPath;
	}

	private static Settings buildSettings() {
		SettingsBuildingRequest request = new DefaultSettingsBuildingRequest();

		request.setGlobalSettingsFile(DEFAULT_GLOBAL_SETTINGS_FILE);
		request.setUserSettingsFile(USER_MAVEN_DEFAULT_USER_SETTINGS_FILE);

		try {
			DefaultSettingsBuilder builder = new DefaultSettingsBuilderFactory().newInstance();

			return builder.build(request).getEffectiveSettings();
		}
		catch (SettingsBuildingException e) {
			e.printStackTrace();

			return null;
		}
	}

	private static File lookupLocalRepoDir() {
		String localRepoPathSetting = buildSettings().getLocalRepository();

		return (localRepoPathSetting == null) ? new File(USER_MAVEN_CONFIGURATION_HOME, "repository")
											  : new File(localRepoPathSetting);
	}

	private static RemoteRepository newRemoteRepository(String url) {
		return new RemoteRepository.Builder("blade", "default", url).build();
	}

	private static RepositorySystem newRepositorySystem() {
		DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
		locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
		locator.addService(TransporterFactory.class, FileTransporterFactory.class);
		locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

		DefaultServiceLocator.ErrorHandler handler = new DefaultServiceLocator.ErrorHandler() {

				@Override
				public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {

					exception.printStackTrace();
				}

			};

		locator.setErrorHandler(handler);

		RepositorySystem system = locator.getService(RepositorySystem.class);

		return system;
	}

	private static DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system,
		String localRepositoryPath) {

		final DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

		final LocalRepository localRepo = new LocalRepository(localRepositoryPath);

		session.setUpdatePolicy(RepositoryPolicy.UPDATE_POLICY_ALWAYS);
		session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo));
		session.setTransferListener(new NoopTransferListener());
		session.setRepositoryListener(new NoopRepositoryListener());

		return session;
	}

	public File getArtifact(String groupIdArtifactIdVersion) throws ArtifactResolutionException {

		final RepositorySystem system = newRepositorySystem();
		final List<RemoteRepository> repos = repos();

		final RepositorySystemSession session = newRepositorySystemSession(system, localRepositoryPath);

		Artifact artifact = new DefaultArtifact(groupIdArtifactIdVersion);
		ArtifactRequest artifactRequest = new ArtifactRequest();
		artifactRequest.setArtifact(artifact);
		artifactRequest.setRepositories(repos);

		ArtifactResult artifactResult = system.resolveArtifact(session, artifactRequest);
		artifact = artifactResult.getArtifact();

		return artifact.getFile();
	}

	public File getLatestAvailableArtifact(String groupIdArtifactId) throws ArtifactResolutionException {

		final RepositorySystem system = newRepositorySystem();
		final List<RemoteRepository> repos = repos();
		final String range = "[0,)";
		final Artifact artifactRange = new DefaultArtifact(groupIdArtifactId + ":" + range);

		final VersionRangeRequest rangeRequest = new VersionRangeRequest();
		rangeRequest.setArtifact(artifactRange);
		rangeRequest.setRepositories(repos);

		final RepositorySystemSession session = newRepositorySystemSession(system, localRepositoryPath);

		Version version = null;

		try {
			version = system.resolveVersionRange(session, rangeRequest).getHighestVersion();
		}
		catch (Exception e) {
			System.err.println("getLatestAvailableArtifact: e.getMessage() + " + e.getMessage());
		}

		if (version == null) {
			return null;
		}

		Artifact artifact = new DefaultArtifact(groupIdArtifactId + ":" + version);
		ArtifactRequest artifactRequest = new ArtifactRequest();
		artifactRequest.setArtifact(artifact);
		artifactRequest.setRepositories(repos);

		ArtifactResult artifactResult = system.resolveArtifact(session, artifactRequest);
		artifact = artifactResult.getArtifact();

		return artifact.getFile();
	}

	public Version getVersionOfLatestMinor(String groupIdArtifactId, long major) throws ArtifactResolutionException {

		final RepositorySystem system = newRepositorySystem();
		final List<RemoteRepository> repos = repos();
		final String range = "[" + major + ".*]";
		final Artifact artifactRange = new DefaultArtifact(groupIdArtifactId + ":" + range);

		final VersionRangeRequest rangeRequest = new VersionRangeRequest();
		rangeRequest.setArtifact(artifactRange);
		rangeRequest.setRepositories(repos);

		final RepositorySystemSession session = newRepositorySystemSession(system, localRepositoryPath);

		Version version = null;

		try {
			version = system.resolveVersionRange(session, rangeRequest).getHighestVersion();
		}
		catch (Exception e) {
			logger.error("getVersionOfLatestMinorAvailable: e.getMessage() + " + e.getMessage());
		}

		logger.info(groupIdArtifactId + ":" + range + " => " + version);

		return version;
	}

	private List<RemoteRepository> repos() {

		final List<RemoteRepository> repos = new ArrayList<RemoteRepository>();

		for (String repoUrl : repoUrls) {
			repos.add(newRemoteRepository(repoUrl));
		}

		return Collections.unmodifiableList(repos);
	}

	private static class NoopRepositoryListener extends AbstractRepositoryListener {
	}

	private static class NoopTransferListener extends AbstractTransferListener {
	}

}
