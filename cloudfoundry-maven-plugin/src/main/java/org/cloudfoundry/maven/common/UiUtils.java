/*
 * Copyright 2009-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.cloudfoundry.maven.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.cloudfoundry.client.lib.CloudFoundryClient;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudInfo;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.cloudfoundry.client.lib.domain.CloudServiceOffering;
import org.cloudfoundry.client.lib.domain.CloudServicePlan;

/**
 * Contains utility methods for rendering data to a formatted console output.
 * E.g. it provides helper methods for rendering ASCII-based data tables.
 *
 * @author Gunnar Hillert
 * @author Scott Frederick
 * @since 1.0.0
 *
 */
public final class UiUtils {
	public static final String HORIZONTAL_LINE = "-------------------------------------------------------------------------------\n";

	public static final int COLUMN_1 = 1;
	public static final int COLUMN_2 = 2;
	public static final int COLUMN_3 = 3;
	public static final int COLUMN_4 = 4;
	public static final int COLUMN_5 = 5;
	public static final int COLUMN_6 = 6;

	/**
	 * Prevent instantiation.
	 *
	 */
	private UiUtils() {
		throw new AssertionError();
	}

	/**
	 * Renders a textual representation of a Application {@link CloudApplication}
	 *
	 * <ul>
	 *     <li>Names of the Applications</li>
	 *     <li>Number of Instances</li>
	 *     <li>Current State (Health)</li>
	 *     <li>Used Memory</li>
	 *     <li>The comma-separated list of Uris</li>
	 *     <li>The comma-separated list of Services</li>
	 * <ul>
	 */
	public static String renderCloudApplicationDataAsTable(CloudApplication application) {
		StringBuilder sb = new StringBuilder("\n");

		sb.append(String.format("%s: %s\n", application.getName(), application.getState()));
		sb.append(String.format("  usage: %sM x %s instance\n", application.getMemory(), application.getInstances()));
		sb.append(String.format("  urls: %s\n", CommonUtils.collectionToCommaDelimitedString(application.getUris())));
		sb.append(String.format("  services: %s\n", CommonUtils.collectionToCommaDelimitedString(application.getServices())));

		return sb.toString();
	}

	/**
	 * Renders a textual representation of the list of provided {@link CloudApplication}
	 *
	 * The following information is shown:
	 *
	 * <ul>
	 *     <li>Names of the Applications</li>
	 *     <li>Number of Instances</li>
	 *     <li>Current State (Health)</li>
	 *     <li>Used Memory</li>
	 *     <li>The comma-separated list of Uris</li>
	 *     <li>The comma-separated list of Services</li>
	 * <ul>
	 *
	 * @param applications List of {@CloudApplication}
	 * @return The rendered table representation as String
	 *
	 */
	public static String renderCloudApplicationsDataAsTable(List<CloudApplication> applications) {

		Table table = new Table();

		table.getHeaders().put(COLUMN_1, new TableHeader("Application"));
		table.getHeaders().put(COLUMN_2, new TableHeader("#"));
		table.getHeaders().put(COLUMN_3, new TableHeader("Health"));
		table.getHeaders().put(COLUMN_4, new TableHeader("Memory"));
		table.getHeaders().put(COLUMN_5, new TableHeader("URLS"));
		table.getHeaders().put(COLUMN_6, new TableHeader("Services"));

		Comparator<CloudApplication> nameComparator = new Comparator<CloudApplication>() {
			public int compare(CloudApplication a, CloudApplication b) {
				return a.getName().compareTo(b.getName());
			}
		};

		Collections.sort(applications, nameComparator);

		for (CloudApplication application : applications) {

			TableRow tableRow = new TableRow();

			table.getHeaders().get(COLUMN_1).updateWidth(application.getName().length());
			tableRow.addValue(COLUMN_1, application.getName());

			table.getHeaders().get(COLUMN_2).updateWidth(String.valueOf(application.getInstances()).length());
			tableRow.addValue(COLUMN_2, String.valueOf(application.getInstances()));

			table.getHeaders().get(COLUMN_3).updateWidth(application.getState().toString().length());
			tableRow.addValue(COLUMN_3, application.getState().toString());

			table.getHeaders().get(COLUMN_4).updateWidth(String.valueOf(application.getMemory()).length());
			tableRow.addValue(COLUMN_4, String.valueOf(application.getMemory()));

			String uris = CommonUtils.collectionToCommaDelimitedString(application.getUris());
			String services = CommonUtils.collectionToCommaDelimitedString(application.getServices());

			table.getHeaders().get(COLUMN_5).updateWidth(uris.length());
			tableRow.addValue(COLUMN_5, uris);

			tableRow.addValue(COLUMN_6, services);

			table.getRows().add(tableRow);
		}

		return renderTextTable(table);
	}

	/**
	 * Renders a sorted textual representation of the list of provided {@link CloudFoundryClient, @link ServiceConfigurations}
	 *
	 * The following information is shown:
	 *
	 * For CC V1
	 * <ul>
	 *	<li>Service Vendor</li>
	 *	<li>Service Version</li>
	 *	<li>Service Description</li>
	 * <ul>
	 *
	 *For CC V2
	 * <ul>
	 *	<li>Service Label</li>
	 *	<li>Service Version</li>
	 *	<li>Service Provider</li>
	 *	<li>Service Plans</li>
	 *	<li>Service Description</li>
	 * <ul>
	 *
	 * @param serviceOfferings
	 * @return The rendered table representation as String
	 *
	 */
	public static String renderServiceConfigurationDataAsTable(List<CloudServiceOffering> serviceOfferings) {
		Comparator<CloudServiceOffering> labelComparator = new Comparator<CloudServiceOffering>() {
			public int compare(CloudServiceOffering a, CloudServiceOffering b) {
				return a.getLabel().compareTo(b.getLabel());
			}
		};
		Collections.sort(serviceOfferings, labelComparator);

		Table table = new Table();
		table.getHeaders().put(COLUMN_1, new TableHeader("Service"));
		table.getHeaders().put(COLUMN_2, new TableHeader("Version"));

		table.getHeaders().put(COLUMN_3, new TableHeader("Provider"));
		table.getHeaders().put(COLUMN_4, new TableHeader("Plans"));
		table.getHeaders().put(COLUMN_5, new TableHeader("Description"));
		List<String> CloudServicePlanNames;

		for (CloudServiceOffering serviceOffering : serviceOfferings) {
			TableRow tableRow = new TableRow();

			table.getHeaders().get(COLUMN_1).updateWidth(serviceOffering.getLabel().length());
			tableRow.addValue(COLUMN_1, serviceOffering.getLabel());

			table.getHeaders().get(COLUMN_2).updateWidth(serviceOffering.getVersion().length());
			tableRow.addValue(COLUMN_2, serviceOffering.getVersion());

			table.getHeaders().get(COLUMN_3).updateWidth(serviceOffering.getProvider().length());
			tableRow.addValue(COLUMN_3, serviceOffering.getProvider());

			CloudServicePlanNames = new ArrayList<String>();
			for (CloudServicePlan servicePlan : serviceOffering.getCloudServicePlans()) {
				CloudServicePlanNames.add(servicePlan.getName());
			}
			table.getHeaders().get(COLUMN_4).updateWidth(CloudServicePlanNames.toString().length() - 1);
			tableRow.addValue(COLUMN_4, CloudServicePlanNames.toString().substring(1, CloudServicePlanNames.toString().length() - 1));

			table.getHeaders().get(COLUMN_5).updateWidth(serviceOffering.getDescription().length());
			tableRow.addValue(COLUMN_5, serviceOffering.getDescription());

			table.getRows().add(tableRow);
		}

		return renderTextTable(table);
	}

	/**
	 * Renders a sorted textual representation of the list of provided {@link CloudService}
	 *
	 * The following information is shown:
	 *
	 *For CC V1
	 * <ul>
	 *	<li>Service Name</li>
	 *	<li>Service Vendor</li>
	 * <ul>
	 *
	 *For CC V2
	 * <ul>
	 *	<li>Service Name</li>
	 *	<li>Service Label</li>
	 *	<li>Service Version</li>
	 *	<li>Service Plan</li>
	 * <ul>
	 *
	 *
	 * @param services
	 * @return The rendered table representation as String
	 *
	 */
	public static String renderServiceDataAsTable(List<CloudService> services) {
		Comparator<CloudService> nameComparator = new Comparator<CloudService>() {
			public int compare(CloudService a, CloudService b) {
				return a.getName().compareTo(b.getName());
			}
		};
		Collections.sort(services, nameComparator);

		Table table = new Table();
		table.getHeaders().put(COLUMN_1, new TableHeader("Name"));
		table.getHeaders().put(COLUMN_2, new TableHeader("Service"));

		table.getHeaders().put(COLUMN_3, new TableHeader("Version"));
		table.getHeaders().put(COLUMN_4, new TableHeader("Plan"));

		for (CloudService service : services) {
			TableRow tableRow = new TableRow();

			table.getHeaders().get(COLUMN_1).updateWidth(service.getName().length());
			tableRow.addValue(COLUMN_1, service.getName());

			table.getHeaders().get(COLUMN_2).updateWidth(service.getLabel().length());
			tableRow.addValue(COLUMN_2, service.getLabel());

			table.getHeaders().get(COLUMN_3).updateWidth(service.getVersion().length());
			tableRow.addValue(COLUMN_3, service.getVersion());

			table.getHeaders().get(COLUMN_4).updateWidth(service.getPlan().length());
			tableRow.addValue(COLUMN_4, service.getPlan());

			table.getRows().add(tableRow);
		}
		return renderTextTable(table);
	}

	/**
	 * Renders a textual representation of provided parameter map.
	 *
	 * @param parameters Map of parameters (key, value)
	 * @return The rendered table representation as String
	 *
	 */
	public static String renderParameterInfoDataAsTable(Map<String, String> parameters) {
		final Table table = new Table();

		table.getHeaders().put(COLUMN_1, new TableHeader("Parameter"));
		table.getHeaders().put(COLUMN_2, new TableHeader("Value (Configured or Default)"));

		for (Entry<String, String> entry : parameters.entrySet()) {

			final TableRow tableRow = new TableRow();

			table.getHeaders().get(COLUMN_1).updateWidth(entry.getKey().length());
			tableRow.addValue(COLUMN_1, entry.getKey());

			table.getHeaders().get(COLUMN_2).updateWidth(entry.getValue() != null ? entry.getValue().length() : 0);
			tableRow.addValue(COLUMN_2, entry.getValue());

			table.getRows().add(tableRow);
		}

		return renderTextTable(table);
	}

	/**
	 * Renders a textual representation of the provided {@link Table}
	 *
	 * @param table Table data {@link Table}
	 * @return The rendered table representation as String
	 */
	public static String renderTextTable(Table table) {
		final String padding = "  ";
		final String headerBorder = getHeaderBorder(table.getHeaders());
		final StringBuilder textTable = new StringBuilder();

		for (TableHeader header : table.getHeaders().values()) {
			textTable.append(padding + CommonUtils.padRight(header.getName(), header.getWidth()));
		}

		textTable.append("\n");

		textTable.append(headerBorder);

		for (TableRow row : table.getRows()) {
			for (Entry<Integer, TableHeader> entry : table.getHeaders().entrySet()) {
				textTable.append(padding + CommonUtils.padRight(row.getValue(entry.getKey()), entry.getValue().getWidth()));
			}
			textTable.append("\n");
		}

		return textTable.toString();
	}

	/**
	 * Renders the help text. If the callers is logged in successfully the full
	 * information is rendered if not only basic Cloud Foundry information is
	 * rendered and returned as String.
	 *
	 * @param cloudInfo Contains the information about the Cloud Foundry environment
	 * @param target The target Url from which the information was obtained
	 *
	 * @return Returns a formatted String for console output
	 */
	public static String renderCloudInfoFormattedAsString(CloudInfo cloudInfo, List<CloudServiceOffering> serviceOfferings, String target) {

		StringBuilder sb = new StringBuilder("\n");

		sb.append(UiUtils.HORIZONTAL_LINE);
		sb.append(String.format("%s (v%s build %s)\n", cloudInfo.getDescription(), cloudInfo.getVersion(), cloudInfo.getBuild()));
		sb.append(String.format("For support visit %s\n\n", cloudInfo.getSupport()));

		sb.append(String.format("Target:          %s  \n"   , target));
		sb.append(String.format("System Services: %s\n\n"   , CommonUtils.serviceOfferingsToCommaDelimitedString(serviceOfferings)));

		if (cloudInfo.getUser() != null) {
			sb.append(String.format("User:        %s\n", cloudInfo.getUser()));

			sb.append("Usage: " + "\n");
			sb.append(String.format("    Memory:       %sM of %sM total \n", cloudInfo.getUsage().getTotalMemory(), cloudInfo.getLimits().getMaxTotalMemory()));
			sb.append(String.format("    Services:     %s of %s total \n" , cloudInfo.getUsage().getServices(), cloudInfo.getLimits().getMaxServices()));
			sb.append(String.format("    Apps:         %s of %s total \n" , cloudInfo.getUsage().getApps(), cloudInfo.getLimits().getMaxApps()));
			sb.append(String.format("    Uris Per App: %s of %s total \n" , cloudInfo.getUsage().getUrisPerApp(), cloudInfo.getLimits().getMaxUrisPerApp()));
		}

		sb.append(UiUtils.HORIZONTAL_LINE);
		return sb.toString();
	}

	/**
	 * Renders the Table header border, based on the map of provided headers.
	 *
	 * @param headers Map of headers containing meta information e.g. name+width of header
	 * @return Returns the rendered header border as String
	 */
	public static String getHeaderBorder(Map<Integer, TableHeader> headers) {

		final StringBuilder headerBorder = new StringBuilder();

		for (TableHeader header : headers.values()) {
			headerBorder.append(CommonUtils.padRight("  ", header.getWidth() + 2, '-'));
		}
		headerBorder.append("\n");

		return headerBorder.toString();
	}
}