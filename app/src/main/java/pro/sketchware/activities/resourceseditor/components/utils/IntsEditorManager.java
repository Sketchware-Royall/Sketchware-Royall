package pro.sketchware.activities.resourceseditor.components.utils;

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import pro.sketchware.activities.resourceseditor.components.models.IntModel;

public class IntsEditorManager {

    public boolean isDataLoadingFailed;
    public LinkedHashMap<Integer, String> notesMap = new LinkedHashMap<>();

    public String convertIntsToXML(ArrayList<IntModel> intsList, HashMap<Integer, String> notesMap) {
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<resources>\n");

        for (int i = 0; i < intsList.size(); i++) {
            if (notesMap.containsKey(i)) {
                xmlBuilder.append("    <!--").append(notesMap.get(i)).append("-->\n");
            }

            IntModel intModel = intsList.get(i);
            xmlBuilder.append("    <integer name=\"").append(intModel.getIntName()).append("\">")
                    .append(intModel.getIntValue())
                    .append("</integer>\n");
        }

        xmlBuilder.append("</resources>");
        return xmlBuilder.toString();
    }

    public ArrayList<IntModel> parseIntsFile(String content) {
        isDataLoadingFailed = false;
        ArrayList<IntModel> ints = new ArrayList<>();
        notesMap.clear();

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource inputSource = new InputSource(new StringReader(content));
            Document document = builder.parse(inputSource);
            document.getDocumentElement().normalize();

            NodeList allNodes = document.getDocumentElement().getChildNodes();
            for (int i = 0; i < allNodes.getLength(); i++) {
                Node node = allNodes.item(i);

                if (node.getNodeType() == Node.COMMENT_NODE) {
                    Comment comment = (Comment) node;
                    notesMap.put(ints.size(), comment.getTextContent().trim());
                } else if (node.getNodeType() == Node.ELEMENT_NODE && "integer".equals(node.getNodeName())) {
                    Element element = (Element) node;
                    ints.add(new IntModel(
                            element.getAttribute("name"),
                            element.getTextContent().trim()
                    ));
                }
            }
        } catch (Exception ignored) {
            isDataLoadingFailed = !content.trim().isEmpty();
        }

        return ints;
    }

    public boolean isIntExist(ArrayList<IntModel> intsList, String intName) {
        for (IntModel intModel : intsList) {
            if (intModel.getIntName().equals(intName)) {
                return true;
            }
        }
        return false;
    }
}