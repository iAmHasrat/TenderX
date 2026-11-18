package com.tenderx.model;

public class BidResponse {
    private String id;
    private String bidId;
    private String fieldKey;
    private String fieldLabel;
    private String responseValue;

    public BidResponse() {}

    public BidResponse(String id, String bidId, String fieldKey, String fieldLabel, String responseValue) {
        this.id = id;
        this.bidId = bidId;
        this.fieldKey = fieldKey;
        this.fieldLabel = fieldLabel;
        this.responseValue = responseValue;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBidId() { return bidId; }
    public void setBidId(String bidId) { this.bidId = bidId; }

    public String getFieldKey() { return fieldKey; }
    public void setFieldKey(String fieldKey) { this.fieldKey = fieldKey; }

    public String getFieldLabel() { return fieldLabel; }
    public void setFieldLabel(String fieldLabel) { this.fieldLabel = fieldLabel; }

    public String getResponseValue() { return responseValue; }
    public void setResponseValue(String responseValue) { this.responseValue = responseValue; }
}

