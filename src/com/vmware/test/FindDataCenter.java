package com.vmware.test;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.soap.SOAPFaultException;

import com.vmware.vim25.DynamicProperty;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.ObjectContent;
import com.vmware.vim25.ObjectSpec;
import com.vmware.vim25.PropertyFilterSpec;
import com.vmware.vim25.PropertySpec;
import com.vmware.vim25.RetrieveOptions;
import com.vmware.vim25.RetrieveResult;
import com.vmware.vim25.SelectionSpec;
import com.vmware.vim25.ServiceContent;
import com.vmware.vim25.TraversalSpec;
import com.vmware.vim25.VimPortType;

public class FindDataCenter {
	
	private static ServiceContent serviceContent;
	private static VimPortType vimPort = null;
	private static ManagedObjectReference propCollectorRef;

	private static TraversalSpec getDatacenterTraversalSpec() {
		SelectionSpec sSpec = new SelectionSpec();
		sSpec.setName("VisitFolders");

		TraversalSpec traversalSpec = new TraversalSpec();
		traversalSpec.setName("VisitFolders");
		traversalSpec.setType("Folder");
		traversalSpec.setPath("childEntity");
		traversalSpec.setSkip(false);
		traversalSpec.getSelectSet().add(sSpec);
		return traversalSpec;
	}

	private static ManagedObjectReference getDatacenterByName(String datacenterName) {
		ManagedObjectReference retVal = null;
		ManagedObjectReference rootFolder = serviceContent.getRootFolder();
		try {
			TraversalSpec tSpec = getDatacenterTraversalSpec();
			PropertySpec propertySpec = new PropertySpec();
			propertySpec.setAll(Boolean.FALSE);
			propertySpec.getPathSet().add("name");
			propertySpec.setType("Datacenter");

			ObjectSpec objectSpec = new ObjectSpec();
			objectSpec.setObj(rootFolder);
			objectSpec.setSkip(Boolean.TRUE);
			objectSpec.getSelectSet().add(tSpec);

			PropertyFilterSpec propertyFilterSpec = new PropertyFilterSpec();
			propertyFilterSpec.getPropSet().add(propertySpec);
			propertyFilterSpec.getObjectSet().add(objectSpec);

			List<PropertyFilterSpec> listfps = new ArrayList<PropertyFilterSpec>(1);
			listfps.add(propertyFilterSpec);
			List<ObjectContent> listobcont = retrievePropertiesAllObjects(listfps);

			if (listobcont != null) {
				for (ObjectContent oc : listobcont) {
					ManagedObjectReference mr = oc.getObj();
					String dcnm = null;
					List<DynamicProperty> dps = oc.getPropSet();
					if (dps != null) {
						for (DynamicProperty dp : dps) {
							dcnm = (String) dp.getVal();
						}
					}

					if (dcnm != null && dcnm.equals(datacenterName)) {
						retVal = mr;
						break;
					}
				}
			}
		} catch (SOAPFaultException sfe) {
			// printSoapFaultException(sfe);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retVal;
	}

	private static List<ObjectContent> retrievePropertiesAllObjects(List<PropertyFilterSpec> listpfs) throws Exception {
		RetrieveOptions propObjectRetrieveOpts = new RetrieveOptions();
		List<ObjectContent> listobjcontent = new ArrayList<ObjectContent>();

		try {
			RetrieveResult rslts = vimPort.retrievePropertiesEx(propCollectorRef, listpfs, propObjectRetrieveOpts);
			if (rslts != null && rslts.getObjects() != null && !rslts.getObjects().isEmpty()) {
				listobjcontent.addAll(rslts.getObjects());
			}
			String token = null;
			if (rslts != null && rslts.getToken() != null) {
				token = rslts.getToken();
			}
			while (token != null && !token.isEmpty()) {
				rslts = vimPort.continueRetrievePropertiesEx(propCollectorRef, token);
				token = null;
				if (rslts != null) {
					token = rslts.getToken();
					if (rslts.getObjects() != null && !rslts.getObjects().isEmpty()) {
						listobjcontent.addAll(rslts.getObjects());
					}
				}
			}
		} catch (SOAPFaultException sfe) {
//			printSoapFaultException(sfe);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return listobjcontent;
	}

}
