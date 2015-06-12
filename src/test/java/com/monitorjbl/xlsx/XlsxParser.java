package com.monitorjbl.xlsx;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class XlsxParser {
	public static void main(String[] args) throws Exception {
		File file = new File("/home/leiting/Documents/pris-book-category.xlsx");
		OPCPackage pkg = OPCPackage.open(file);
		XSSFReader r = new XSSFReader(pkg);
		SharedStringsTable sst = r.getSharedStringsTable();
		XMLReader parser = XMLReaderFactory
				.createXMLReader("org.apache.xerces.parsers.SAXParser");
		ContentHandler handler = new SheetHandler(sst);
		parser.setContentHandler(handler);

		Iterator<InputStream> sheets = r.getSheetsData();
		while (sheets.hasNext()) {
			InputStream sheet = sheets.next();
			InputSource sheetSource = new InputSource(sheet);
			parser.parse(sheetSource);
			sheet.close();
		}
	}

	static class SheetHandler extends DefaultHandler {
		private SharedStringsTable sst;
		private String lastContents;
		private boolean sharedString;

		public SheetHandler(SharedStringsTable sst) {
			this.sst = sst;
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {
			if (qName.equals("c")) {
				String s = attributes.getValue("s");
				if ("1".equals(s)) {
					sharedString = true;
				} else {
					sharedString = false;
				}
				
				lastContents = "";
			}
			
		}

		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			if (qName.equals("v")) {
				if (sharedString) {
					try {
						int idx = Integer.parseInt(lastContents); // Catch the ID in int
						lastContents = new XSSFRichTextString(sst.getEntryAt(idx))
								.toString(); // Get the value referenced by index ()
					} catch (NumberFormatException e) {
					}
				}
			}
			
			if (qName.equals("c")) {
				System.out.println(lastContents);
			}
		}

		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			lastContents += new String(ch, start, length);
		}
	}
}
