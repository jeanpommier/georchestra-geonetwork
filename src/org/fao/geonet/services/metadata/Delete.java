//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.services.metadata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import jeeves.constants.Jeeves;
import jeeves.exceptions.OperationNotAllowedEx;
import jeeves.interfaces.Service;
import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import jeeves.utils.BinaryFile;
import jeeves.utils.Util;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.mef.MEFLib;
import org.fao.geonet.kernel.search.MetaSearcher;
import org.fao.geonet.lib.Lib;
import org.jdom.Element;

//=============================================================================

/** Removes a metadata from the system
  */

public class Delete implements Service
{
	public void init(String appPath, ServiceConfig params) throws Exception {}

	//--------------------------------------------------------------------------
	//---
	//--- Service
	//---
	//--------------------------------------------------------------------------

	public Element exec(Element params, ServiceContext context) throws Exception
	{
		GeonetContext gc = (GeonetContext) context.getHandlerContext(Geonet.CONTEXT_NAME);
		DataManager   dataMan   = gc.getDataManager();
		AccessManager accessMan = gc.getAccessManager();
		UserSession   session   = context.getUserSession();

		Dbms dbms = (Dbms) context.getResourceManager().open(Geonet.Res.MAIN_DB);

		String id = Util.getParam(params, Params.ID);

		//-----------------------------------------------------------------------
		//--- check access

		String uuid = dataMan.getMetadataUuid(dbms, id);

		if (uuid == null)
			throw new IllegalArgumentException("Metadata not found --> " + id);

		HashSet hsOper = accessMan.getOperations(context, id, context.getIpAddress());

		if (!hsOper.contains(AccessManager.OPER_EDIT))
			throw new OperationNotAllowedEx();

		//-----------------------------------------------------------------------
		//--- backup metadata in 'removed' folder

		backupFile(context, id, uuid, MEFLib.doExport(context, uuid, "full", false));

		//-----------------------------------------------------------------------
		//--- delete metadata and return status

		dataMan.deleteMetadata(dbms, id);

		Element elResp = new Element(Jeeves.Elem.RESPONSE);
		elResp.addContent(new Element(Geonet.Elem.ID).setText(id));

		// invalidate current result set set
		MetaSearcher searcher = (MetaSearcher)context.getUserSession().getProperty(Geonet.Session.SEARCH_RESULT);

		if (searcher != null)
			searcher.setValid(false);

		return elResp;
	}

	//--------------------------------------------------------------------------
	//---
	//--- Private methods
	//---
	//--------------------------------------------------------------------------

	private void backupFile(ServiceContext context, String id, String uuid, String file)
	{
		String outDir = Lib.resource.getRemovedDir(context, id);
		String outFile= outDir + uuid +".mef";

		new File(outDir).mkdirs();

		try
		{
			FileInputStream  is = new FileInputStream(file);
			FileOutputStream os = new FileOutputStream(outFile);

			BinaryFile.copy(is, os, true, true);
		}
		catch(Exception e)
		{
			context.warning("Cannot backup mef file : "+e.getMessage());
			e.printStackTrace();
		}

		new File(file).delete();
	}
}

//=============================================================================

