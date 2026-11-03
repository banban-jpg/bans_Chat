-- Oracle 26ai only; applied once after V003 on an empty dedicated schema.
ALTER TABLE document ADD review_assignee_id NUMBER(19);
ALTER TABLE document ADD CONSTRAINT fk_doc_assignee FOREIGN KEY (review_assignee_id) REFERENCES employee (employee_id);
