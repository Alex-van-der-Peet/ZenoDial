package chatbot.plugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import chatbot.log.Logger;
import chatbot.log.Logger.Level;

public class WolframAlpha {
	private static Logger log = new Logger("WolframAlpha", Level.NORMAL);
	private static final String URL1 = "http://api.wolframalpha.com/v2/query?input=";
	private static final String URL2 = "&appid=";
	private static final String API_ID = ""; //TODO: Paste your API key here
	
	public static String getReply(String input) {
		String reply = extractTheAnswer(connectToWolframAlpha(URL1 + input.replace(" ", "+") + URL2 + API_ID));
		
		if ("".compareTo(reply.trim()) != 0) log.debug("Wolfram|Alpha Reply = " + reply);
		else log.debug("Wolfram|Alpha Reply = <None>");
		
		return reply;
	}
	
	private static String connectToWolframAlpha(String inputURL) {
		String line = "";
		String htmlSource = "";
		
		try {
			log.debug("Input URL = " + inputURL);
			URL url = new URL(inputURL);
			URLConnection urlConnection = url.openConnection();
			log.debug("Connection built successfully.");
			
			urlConnection.setRequestProperty("User-Agent", "Mozilla/4.0(compatible; MSIE 5.5; Windows NT 5.0; H010818)");
			BufferedReader bufferReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			
			while ((line = bufferReader.readLine()) != null) {
				htmlSource += line;
			}
			
			log.debug("htmlSource = " + htmlSource);
		}
		
		catch (Exception e) {
			log.warning("Cannot connect to Wolfram|Alpha.");
			e.printStackTrace();
		}
		
		return htmlSource;
	}
	
	private static String extractTheAnswer(String htmlSource) {
		String answer = "";
		
		try {
			Document xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(htmlSource)));
			xmlDocument.getDocumentElement().normalize();
			
			// Get a list of <pod>
			NodeList podList = xmlDocument.getElementsByTagName("pod");
			
			for (int i = 0; i < podList.getLength(); i++) {
				Node podNode = podList.item(i);
				
				if (podNode.hasAttributes() && podNode.getAttributes().getNamedItem("title") != null && "Result".equals(podNode.getAttributes().getNamedItem("title").getTextContent())) {
					
					// <pod><subpod>
					for (int j = 0; j < podNode.getChildNodes().getLength(); j++) {
						Node subPodNode = podNode.getChildNodes().item(j);
						
						// <pod><subpod><plaintext>
						for (int k = 0; k < subPodNode.getChildNodes().getLength(); k++) {
							if ("plaintext".equals(subPodNode.getChildNodes().item(k).getNodeName())) {
								answer = subPodNode.getChildNodes().item(k).getTextContent();
							}
						}
					}
				}
			}
		}
		
		catch (Exception e) {
			log.warning("Error occurred when extracting the answer.");
			e.printStackTrace();
		}
		
		return answer;
	}
	
	public static void main(String... args) {
		System.out.println("WolframAlpha:");
		Scanner scanner = new Scanner(System.in);
		String userInput = "";
		
		while ((userInput = scanner.nextLine()) != null) {
			getReply(userInput);
		}
		
		scanner.close();
	}
}