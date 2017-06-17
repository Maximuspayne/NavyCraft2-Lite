package com.maximuspayne.navycraft.config;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.maximuspayne.navycraft.NavyCraft;


public class XMLHandler {	
	
	public static void load() {
		File dir = NavyCraft.instance.getDataFolder();
		if (!dir.exists())
			dir.mkdir();
		
		File config = new File(NavyCraft.instance.getDataFolder(), NavyCraft.instance.configFile.filename);
		if (!config.exists()) {
			return;
		}
		Document doc = null;
		try {
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(config.toURI().getPath());
			doc.getDocumentElement().normalize();
			
			NodeList list;
			
			for(Object configLine : NavyCraft.instance.configFile.ConfigSettings.keySet().toArray()) {
				String configKey = (String) configLine;

				list = doc.getElementsByTagName(configKey);
				
				try {
					String value = list.item(0).getChildNodes().item(0).getNodeValue();
					NavyCraft.instance.configFile.ConfigSettings.put(configKey, value);
				} catch (Exception ex){
	
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void save() {		
		File dir = NavyCraft.instance.getDataFolder();
		if (!dir.exists())
			dir.mkdir();

		File configuration = new File(NavyCraft.instance.getDataFolder(), NavyCraft.instance.configFile.filename);
		//test if filename contains ".xml", if not, freak out a little

		Element setting = null;

		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			docBuilder = docFactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();
			doc.setXmlStandalone(true);
			Element rootElement = doc.createElement("NavyCraft-Configuration");
			doc.appendChild(rootElement);

			Object[] obj = NavyCraft.instance.configFile.ConfigSettings.keySet().toArray();
			java.util.Arrays.sort(obj);
			for(Object configLine : obj) {				
				String configKey = (String) configLine;
				setting = doc.createElement(configKey);
				setting.appendChild(doc.createTextNode(NavyCraft.instance.configFile.ConfigSettings.get(configKey)));
				rootElement.appendChild(setting);

				if(NavyCraft.instance.configFile.ConfigComments.containsKey(configKey)) {
					Comment comment = doc.createComment(NavyCraft.instance.configFile.ConfigComments.get(configKey));
					rootElement.insertBefore(comment,setting);
				}
			}
			
			Comment comment = doc.createComment("Do you find this config file strange and confusing? " +
					"This guy made this nifty GUI editor for it. Get it here: http://bit.ly/ewPebA");
			rootElement.appendChild(comment);

			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer;
			try {
				transformer = transformerFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
				DOMSource source = new DOMSource(doc);
				StreamResult result = new StreamResult(configuration);
				transformer.transform(source, result);
			} catch (TransformerConfigurationException e) {
				e.printStackTrace();
			} catch (TransformerException e) {
				e.printStackTrace();
			}
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
	}
}
