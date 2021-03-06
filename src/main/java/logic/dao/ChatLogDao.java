package logic.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import logic.Database;
import logic.exception.DataAccessException;
import logic.exception.DataLogicException;
import logic.model.ChatLogEntryModel;

public final class ChatLogDao {
	private ChatLogDao() {}

	private static final Connection CONN = 
		Database.getInstance().getConnection();

	private static final String STMT_ADD_CHAT_LOG_ENTRY = 
		"{ call AddChatLogEntry(?,?,?,?) }";
	private static final String STMT_GET_CHAT_LOG = 
		"{ call GetChatLog(?,?,?,?) }";
	private static final String NON_EXISTANT_USER_ERROR = "Non-existant user";

	public static void addLogEntry(ChatLogEntryModel entry) 
			throws DataAccessException, DataLogicException {
		try(CallableStatement stmt = CONN.prepareCall(STMT_ADD_CHAT_LOG_ENTRY)) {
			stmt.setString(1, entry.getSenderEmail());
			stmt.setString(2, entry.getReceiverEmail());
			stmt.setString(3, entry.getText());
			stmt.setTimestamp(4, new Timestamp(entry.getDeliveryRequestTime()));
			stmt.execute();
		} catch(SQLException e) {
			if(e.getErrorCode() == 1452) {
				throw new DataLogicException(NON_EXISTANT_USER_ERROR);
			}

			throw new DataAccessException(e);
		}
	}

	public static List<ChatLogEntryModel> getLog(
			String senderEmail, String receiverEmail, 
			int deliverRequestFromTime, int deliverRequestToTime) 
				throws DataAccessException {
		try (CallableStatement stmt = CONN.prepareCall(STMT_GET_CHAT_LOG)) {
			stmt.setString(1, senderEmail);
			stmt.setString(2, receiverEmail);
			stmt.setTimestamp(3, new Timestamp(deliverRequestFromTime));
			stmt.setTimestamp(4, new Timestamp(deliverRequestToTime));
			stmt.execute();

			try(ResultSet rs = stmt.getResultSet()) {
				List<ChatLogEntryModel> cle = new ArrayList<>();

				ChatLogEntryModel model = new ChatLogEntryModel();
				while(rs.next()) {
					model.setLogEntryId(rs.getLong(1));
					model.setSenderEmail(rs.getString(2));
					model.setReceiverEmail(rs.getString(3));
					model.setText(rs.getString(4));
					model.setDeliveryRequestTime((int)rs.getTimestamp(5).getTime());

					cle.add(model);
				}

				return cle;
			}
		} catch (SQLException e) {
			throw new DataAccessException(e);
		}
	}
}
