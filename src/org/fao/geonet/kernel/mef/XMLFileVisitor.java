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
package org.fao.geonet.kernel.mef;

import java.io.File;

import jeeves.exceptions.BadFormatEx;
import jeeves.utils.Xml;

import org.jdom.Element;

public class XMLFileVisitor implements FileVisitor {
	
	// --------------------------------------------------------------------------
	// ---
	// --- API methods
	// ---
	// --------------------------------------------------------------------------

	public void visit(File mefFile, MEFVisitor v) throws Exception {
		Element info = handleXml(mefFile, v);
	}

	// --------------------------------------------------------------------------

	public Element handleXml(File mefFile, MEFVisitor v)
			throws Exception {
				
		Element md = null;
		Element info = null;

		md = Xml.loadFile(mefFile);
		info = new Element("info");

		if (md == null)
			throw new BadFormatEx("Missing xml metadata file .");

		v.handleMetadata(md);
		v.handleInfo(info);

		return info;
	}
	
	public void handleBin(File mefFile, MEFVisitor v, Element info) throws Exception {}
}
