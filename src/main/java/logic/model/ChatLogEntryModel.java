package logic.model;

public final class ChatLogEntryModel {
	private long logEntryId;
	private String senderEmail;
	private String receiverEmail;
	private String text;
	private int deliveryRequestTime;

	public long getLogEntryId() {
		return logEntryId;
	}

	public String getSenderEmail() {
		return senderEmail;
	}

	public String getReceiverEmail() {
		return receiverEmail;
	}

	public int getDeliveryRequestTime() {
		return deliveryRequestTime;
	}

	public String getText() {
		return text;
	}

	public void setLogEntryId(long logEntryId) {
		this.logEntryId = logEntryId;
	}

	public void setSenderEmail(String senderEmail) {
		this.senderEmail = senderEmail;
	}

	public void setReceiverEmail(String receiverEmail) {
		this.receiverEmail = receiverEmail;
	}

	public void setDeliveryRequestTime(int deliveryRequestTime) {
		this.deliveryRequestTime = deliveryRequestTime;
	}

	public void setText(String text) {
		this.text = text;
	}
}
