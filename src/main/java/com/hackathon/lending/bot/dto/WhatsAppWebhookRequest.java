package com.hackathon.lending.bot.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WhatsAppWebhookRequest {

    @JsonProperty("object")
    private String object;

    @JsonProperty("entry")
    private List<Entry> entry;

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public List<Entry> getEntry() {
        return entry;
    }

    public void setEntry(List<Entry> entry) {
        this.entry = entry;
    }

    public Entry getFirstEntry() {
        return (entry == null || entry.isEmpty()) ? null : entry.get(0);
    }

    public Change getFirstChange() {
        Entry firstEntry = getFirstEntry();
        return (firstEntry == null || firstEntry.getChanges() == null || firstEntry.getChanges().isEmpty())
                ? null
                : firstEntry.getChanges().get(0);
    }

    public Value getFirstValue() {
        Change firstChange = getFirstChange();
        return firstChange == null ? null : firstChange.getValue();
    }

    public List<Contact> getContacts() {
        Value value = getFirstValue();
        return value == null || value.getContacts() == null ? Collections.emptyList() : value.getContacts();
    }

    public List<WhatsAppMessage> getMessages() {
        Value value = getFirstValue();
        return value == null || value.getMessages() == null ? Collections.emptyList() : value.getMessages();
    }

    public Metadata getMetadata() {
        Value value = getFirstValue();
        return value == null ? null : value.getMetadata();
    }

    @Override
    public String toString() {
        WhatsAppMessage firstMessage = (getMessages().isEmpty()) ? null : getMessages().get(0);
        String messageBody = (firstMessage != null && firstMessage.getText() != null)
                ? firstMessage.getText().getBody()
                : "";
        return new StringJoiner(", ", WhatsAppWebhookRequest.class.getSimpleName() + "[", "]")
                .add("object='" + object + "'")
                .add("entryId='" + (getFirstEntry() != null ? getFirstEntry().getId() : "") + "'")
                .add("changeField='" + (getFirstChange() != null ? getFirstChange().getField() : "") + "'")
                .add("from='" + (firstMessage != null ? firstMessage.getFrom() : "") + "'")
                .add("messageId='" + (firstMessage != null ? firstMessage.getId() : "") + "'")
                .add("body='" + messageBody + "'")
                .toString();
    }

    // ------------ Entry / Change / Value Inner Classes -----------

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entry {
        @JsonProperty("id")
        private String id;

        @JsonProperty("changes")
        private List<Change> changes;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public List<Change> getChanges() {
            return changes;
        }

        public void setChanges(List<Change> changes) {
            this.changes = changes;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Change {
        @JsonProperty("field")
        private String field;

        @JsonProperty("value")
        private Value value;

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public Value getValue() {
            return value;
        }

        public void setValue(Value value) {
            this.value = value;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Value {
        @JsonProperty("messaging_product")
        private String messagingProduct;

        @JsonProperty("metadata")
        private Metadata metadata;

        @JsonProperty("contacts")
        private List<Contact> contacts;

        @JsonProperty("messages")
        private List<WhatsAppMessage> messages;

        public String getMessagingProduct() {
            return messagingProduct;
        }

        public void setMessagingProduct(String messagingProduct) {
            this.messagingProduct = messagingProduct;
        }

        public Metadata getMetadata() {
            return metadata;
        }

        public void setMetadata(Metadata metadata) {
            this.metadata = metadata;
        }

        public List<Contact> getContacts() {
            return contacts;
        }

        public void setContacts(List<Contact> contacts) {
            this.contacts = contacts;
        }

        public List<WhatsAppMessage> getMessages() {
            return messages;
        }

        public void setMessages(List<WhatsAppMessage> messages) {
            this.messages = messages;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {
        @JsonProperty("display_phone_number")
        private String displayPhoneNumber;

        @JsonProperty("phone_number_id")
        private String phoneNumberId;

        public String getDisplayPhoneNumber() {
            return displayPhoneNumber;
        }

        public void setDisplayPhoneNumber(String displayPhoneNumber) {
            this.displayPhoneNumber = displayPhoneNumber;
        }

        public String getPhoneNumberId() {
            return phoneNumberId;
        }

        public void setPhoneNumberId(String phoneNumberId) {
            this.phoneNumberId = phoneNumberId;
        }
    }

    // ------------ Contacts & Messages Inner Classes -----------

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Contact {
        @JsonProperty("profile")
        private Profile profile;

        @JsonProperty("wa_id")
        private String waId;

        public Profile getProfile() {
            return profile;
        }

        public void setProfile(Profile profile) {
            this.profile = profile;
        }

        public String getWaId() {
            return waId;
        }

        public void setWaId(String waId) {
            this.waId = waId;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Profile {
        @JsonProperty("name")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WhatsAppMessage {
        @JsonProperty("from")
        private String from;

        @JsonProperty("id")
        private String id;

        @JsonProperty("timestamp")
        private String timestamp;

        @JsonProperty("text")
        private Text text;

        @JsonProperty("type")
        private String type;

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        public Text getText() {
            return text;
        }

        public void setText(Text text) {
            this.text = text;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Text {
        @JsonProperty("body")
        private String body;

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }
    }
}
