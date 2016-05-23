/**
 * Copyright (c) 2000-2016 Liferay, Inc. All rights reserved.
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
package com.liferay.faces.bridge.demos.bean;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import com.liferay.faces.portal.context.LiferayPortletHelperUtil;

import com.liferay.portal.model.Portlet;
import com.liferay.portal.model.RoleConstants;
import com.liferay.portal.service.permission.PortletPermissionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author  Vernon Singleton
 */
@RequestScoped
@ManagedBean
public class PermissionsURLModelBean {

	// Private Data Members
	private String portletResourcePrimaryKey;
	private Map<String, Object> roleConstants;

	public String getPortletResourcePrimaryKey() {

		if (portletResourcePrimaryKey == null) {
			long plid = LiferayPortletHelperUtil.getPlid();
			Portlet portlet = LiferayPortletHelperUtil.getPortlet();
			String portletId = portlet.getPortletId();
			portletResourcePrimaryKey = PortletPermissionUtil.getPrimaryKey(plid, portletId);
		}

		return portletResourcePrimaryKey;
	}

	public Map<String, Object> getRoleConstants() {

		if (roleConstants == null) {

			roleConstants = new HashMap<String, Object>();
			Field[] fields = RoleConstants.class.getFields();
			for (Field field: fields) {
				int modifiers = field.getModifiers();

				if (Modifier.isStatic(modifiers)) {
					try {
						roleConstants.put(field.getName(), field.get(null));
					}
					catch (IllegalAccessException e) {
						// ignore
					}
				}

			}
		}

		return roleConstants;
	}
}