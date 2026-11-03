-- Oracle 26ai only. Applies to CLOB writes from every client, not only the REST service.
CREATE OR REPLACE TRIGGER trg_chat_message_length
BEFORE INSERT OR UPDATE OF message_text ON chat_message
FOR EACH ROW
BEGIN
    IF :NEW.message_text IS NULL OR REGEXP_COUNT(:NEW.message_text, '.', 1, 'n') NOT BETWEEN 1 AND 2000 THEN
        RAISE_APPLICATION_ERROR(-20062, 'CHAT_MESSAGE_LENGTH');
    END IF;
END;
/
