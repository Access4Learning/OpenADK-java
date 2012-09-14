//
// Copyright (c)1998-2011 Pearson Education, Inc. or its affiliate(s). 
// All rights reserved.
//

package openadk.library.tools.mapping;

import openadk.util.XMLUtils;
import java.util.ArrayList;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class ListMapping extends Mapping {
    private static final String ATTR_CHILD = "child";
    private static final String ATTR_REFERENCE = "reference";
    private static final String ATTR_XPATH = "xPath";
	private String fChild;
	private String fXPath;
	private ArrayList<Mapping> mappings;
	private String fReference;
	
	/**
	 *  Constructor
	 */
    public ListMapping() {
		this(null, null, null);
    }

	/**
	 *  Constructor
	 */
    public ListMapping(String listObjectName, String child, String xPath) {
    	this.fChild = child;
    	this.fXPath = xPath;
		mappings = new ArrayList<Mapping>();		
    }

	public String getChild() {
		return fChild;
	}

	public void setChild(String child) {
		this.fChild = child;
	}

	public String getXPath() {
		return fXPath;
	}

	public void setXPath(String path) {
		fXPath = path;
	}
	
	/*
	 * Add new Mapping
	 */
	public void addMapping(Mapping mapping) {
		mappings.add(mapping);
	}

	
	/*
	 * Remove FieldMapping
	 */
	public boolean removedMapping(Mapping mapping) {
		return mappings.remove(mapping);
	}

	@Override
	public String getKey() {
		return fXPath;
	}

	@Override
	public Node getNode() {
		return null;
	}

	@Override
	public Mapping copy(ObjectMapping newParent) throws ADKMappingException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MappingsFilter getFilter() {
		// TODO Auto-generated method stub
		return null;
	}

	public ArrayList<Mapping> getMappings() {
		return mappings;
	}

	public void setMappings(ArrayList<Mapping> mappings) {
		this.mappings = mappings;
	}
	
	@Override
	public String toString()
	{
		return "List Field: " + this.getChild() + ": XPath: "+ this.getXPath();
	}

	public void setReference(String reference) {
		fReference = reference;
	}

	public String getReference() {
		return fReference;
	}
	
	/**
	 * Writes the values of this FieldMapping to the specified XML Element
	 * 
	 * @param element The XML Element to write values to
	 */
	public void toXML (Element element) {
		XMLUtils.setOrRemoveAttribute(element,ATTR_CHILD,fChild);
		XMLUtils.setOrRemoveAttribute(element,ATTR_XPATH,fXPath);
		XMLUtils.setOrRemoveAttribute(element,ATTR_REFERENCE,fReference);
        Document doc = element.getOwnerDocument();
        for(Mapping mapping : mappings) {
          if(mapping instanceof FieldMapping) {
                    // Write out each <field>...
            Element fieldNode = doc.createElement(Mappings.XML_FIELD);
            element.appendChild(fieldNode);
            mapping.toXML(fieldNode);
          } else if(mapping instanceof ListMapping) {
              // Write out each <list>...
            Element listNode = doc.createElement(Mappings.XML_LIST);
            element.appendChild(listNode);
            mapping.toXML(listNode);
          }
        }
	}

}
