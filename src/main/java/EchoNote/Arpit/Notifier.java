package EchoNote.Arpit;

import EchoNote.Jack.MeetingRecord;

public interface Notifier {

    void emailParticipants(MeetingRecord record, String eventId) throws NotificationException;
}
