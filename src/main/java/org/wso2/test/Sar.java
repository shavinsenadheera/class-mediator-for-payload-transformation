package org.wso2.test;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Sar extends AbstractMediator{

    private static final Logger logger = LoggerFactory.getLogger(Sar.class);
    @Override
    public boolean mediate(MessageContext messageContext) {
        try {
            // Get the language property from the message context
            String language = (String) messageContext.getProperty("Language");

            // Get the SOAP body content
            OMElement bodyElement = messageContext.getEnvelope().getBody().getFirstElement();

            // Extract the required JSON payload
            OMElement jsonObjectElement = bodyElement.getFirstElement();
            String payload = jsonObjectElement.toString();

            // Transform the JSON payload based on the language
            String transformedPayload = transformPayload(payload, language);

            // Set the transformed payload as a property
            messageContext.setProperty("TRANSFORMED_PAYLOAD", transformedPayload);

        } catch (Exception e) {
            logger.error("Error occurred during mediation.", e);
        }

        return true;
    }

    private String transformPayload(String payload, String language) {
        StringBuilder transformedPayload = new StringBuilder();

        if ("ENGLISH".equals(language)) {
            transformedPayload.append(transformMenuItems(payload, "possible_menu", "possible_menu_id"));
        } else {
            transformedPayload.append(transformMenuItems(payload, "urdu_possible_menu", "possible_menu_id"));
        }

        return transformedPayload.toString();
    }

    private String transformMenuItems(String payload, String titleKey, String idKey) {
        JSONArray jsonArray = new JSONArray();

        if (payload != null) {
            int startIndex = payload.indexOf("<list>");
            int endIndex = payload.indexOf("</list>") + "</list>".length();

            while (startIndex != -1 && endIndex != -1) {
                String menuItem = payload.substring(startIndex, endIndex);
                String title = extractValue(menuItem, titleKey);
                String id = extractValue(menuItem, idKey);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", id);
                jsonObject.put("title", title);
                jsonArray.put(jsonObject);

                startIndex = payload.indexOf("<list>", endIndex);
                endIndex = payload.indexOf("</list>", endIndex) + "</list>".length();
            }
        }

        return jsonArray.toString();
    }

    private String extractValue(String menuItem, String key) {
        int startIndex = menuItem.indexOf("<" + key + ">") + ("<" + key + ">").length();
        int endIndex = menuItem.indexOf("</" + key + ">", startIndex);
        return menuItem.substring(startIndex, endIndex);
    }
}
