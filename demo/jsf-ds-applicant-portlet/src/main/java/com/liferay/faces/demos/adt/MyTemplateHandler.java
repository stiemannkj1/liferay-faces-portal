/**
 * Copyright (c) 2000-2017 Liferay, Inc. All rights reserved.
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
package com.liferay.faces.demos.adt;

import java.util.Locale;

import javax.faces.context.FacesContext;
import javax.portlet.PortletRequest;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.liferay.portal.kernel.portletdisplaytemplate.BasePortletDisplayTemplateHandler;
import com.liferay.portal.kernel.service.ClassNameLocalServiceUtil;
import com.liferay.portal.kernel.template.TemplateHandler;
import com.liferay.portal.kernel.util.Portal;


/**
 * @author  kylestiemann
 */
@Component(immediate = true, property = { "javax.portlet.name=jsf_ds_applicant" }, service = TemplateHandler.class)
public class MyTemplateHandler extends BasePortletDisplayTemplateHandler {

	@Reference
	private Portal portal;

	@Override
	public String getClassName() {
		return ClassNameLocalServiceUtil.getClassName(MyTemplateHandler.class.getName()).getClassName();
	}

	@Override
	public String getName(Locale locale) {
		PortletRequest portletRequest = (PortletRequest) FacesContext.getCurrentInstance().getExternalContext()
			.getRequest();

		return portal.getPortletTitle(portletRequest) + " template";
	}

	@Override
	public String getResourceName() {
		PortletRequest portletRequest = (PortletRequest) FacesContext.getCurrentInstance().getExternalContext()
			.getRequest();

		return portal.getPortletId(portletRequest); // "_jsf_ds_applicant_INSTANCE_DFcrdQ4tvp9a_"
	}
}
